package com.rauio.smartdangjian.server.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.constants.JwtClaims;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    /** 登录失败计数 Redis 键前缀 */
    private static final String LOGIN_FAIL_KEY_PREFIX = "login:fail:";

    /** web 端访问令牌有效期（秒） */
    private static final long WEB_ACCESS_TIMEOUT_SECONDS = 7200L;

    /** app 端访问令牌有效期（秒） */
    private static final long APP_ACCESS_TIMEOUT_SECONDS = 86400L;

    /** 默认访问令牌有效期（秒） */
    private static final long DEFAULT_ACCESS_TIMEOUT_SECONDS = WEB_ACCESS_TIMEOUT_SECONDS;

    /** 各平台访问令牌有效期 */
    private static final java.util.Map<String, Long> ACCESS_TIMEOUT_BY_PLATFORM =
            java.util.Map.of("app", APP_ACCESS_TIMEOUT_SECONDS, "web", WEB_ACCESS_TIMEOUT_SECONDS);

    /** 登录失败锁定阈值 */
    private static final long MAX_LOGIN_FAILS = 5;

    /** 登录锁定时长 */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final CaptchaService captchaService;
    private final UserMapper userMapper;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenService refreshTokenService;
    private final TokenVersionService tokenVersionService;

    public LoginResponse login(LoginRequest loginRequest) {
        if (!captchaService.validate(loginRequest.getCaptchaUUID(), loginRequest.getCaptchaCode())) {
            throw new BusinessException(AuthErrorConstants.CAPTCHA_ERROR, "验证码错误");
        }

        String passport = loginRequest.getPassport();
        String lockKey = LOGIN_FAIL_KEY_PREFIX + passport;
        // 失败计数达到阈值才视为锁定（计数 key 在首次失败时即存在，不能仅凭 key 存在判断）
        Object failCount = redisTemplate.opsForValue().get(lockKey);
        if (failCount instanceof Number n && n.longValue() >= MAX_LOGIN_FAILS) {
            throw new BusinessException(AuthErrorConstants.ACCOUNT_LOCKED, "登录失败次数过多，账号已临时锁定，请15分钟后再试");
        }

        User user = userService.getByPassport(passport);
        if (user == null) {
            recordLoginFail(lockKey);
            throw new BusinessException(AuthErrorConstants.LOGIN_FAILED, "用户名或密码错误");
        }

        if (user.getStatus() == AccountStatus.BANNED) {
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "账号已被封禁");
        }
        if (user.getStatus() == AccountStatus.INACTIVE) {
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "账号未激活");
        }

        if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            recordLoginFail(lockKey);
            throw new BusinessException(AuthErrorConstants.LOGIN_FAILED, "用户名或密码错误");
        }

        redisTemplate.delete(lockKey);

        return issueTokenPair(user, resolvePlatform(loginRequest.getPlatform()));
    }

    /**
     * 使用刷新令牌换取新的访问/刷新令牌对（旋转语义：旧刷新令牌作废）。
     *
     * @param refreshToken 客户端提交的刷新令牌
     * @return 新的令牌对
     */
    public LoginResponse refresh(String refreshToken) {
        // 原子消费刷新令牌：校验与作废在同一 Redis 脚本内完成，并发重复提交仅一个请求能通过
        RefreshTokenService.TokenIdentity identity = refreshTokenService.consume(refreshToken);
        User user = userMapper.selectById(identity.userId());
        // 与 login 口径一致：BANNED/INACTIVE 均终止会话，防止停用账号借刷新无限续期
        if (user == null || user.getStatus() == AccountStatus.BANNED || user.getStatus() == AccountStatus.INACTIVE) {
            refreshTokenService.revokeAllForUser(identity.userId());
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "账号状态异常，会话已终止");
        }
        // 按令牌签发平台续签，app 端不因刷新丢失长效有效期
        long expiresIn = issueAccessToken(user, identity.platform());
        String newRefreshToken = refreshTokenService.issue(user.getId(), identity.platform());
        return buildLoginResponse(StpUtil.getTokenValue(), newRefreshToken, expiresIn);
    }

    public void logout() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId != null) {
            refreshTokenService.revokeAllForUser(Long.valueOf(String.valueOf(loginId)));
        }
        StpUtil.logout();
    }

    public void register(RegisterRequest registerRequest) {
        if (!captchaService.validate(registerRequest.getCaptchaUUID(), registerRequest.getCaptchaCode())) {
            throw new BusinessException(AuthErrorConstants.CAPTCHA_ERROR, "验证码错误");
        }

        // 公开注册仅允许学生角色，防止匿名提权为 SCHOOL/MANAGER
        UserType type = registerRequest.getType();
        if (type != null && type != UserType.STUDENT) {
            throw new BusinessException(AuthErrorConstants.REGISTER_TYPE_FORBIDDEN, "仅支持学生注册");
        }

        checkEmailRegistered(registerRequest.getEmail());
        checkPhoneRegistered(registerRequest.getPhone());
        checkUsernameOccupied(registerRequest.getUsername());
        checkPartyMemberId(registerRequest.getPartyMemberId());

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(BCrypt.hashpw(registerRequest.getPassword()))
                .realName(registerRequest.getRealName())
                .idCard(registerRequest.getIdCard())
                .partyMemberId(registerRequest.getPartyMemberId())
                .partyStatus(registerRequest.getPartyStatus())
                .branchName(registerRequest.getBranchName())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .universityId(registerRequest.getUniversityId())
                .joinPartyDate(registerRequest.getJoinPartyDate())
                .userType(UserType.STUDENT)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (userMapper.insert(user) <= 0) {
            throw new BusinessException(AuthErrorConstants.REGISTER_FAILED, "注册失败");
        }
    }

    public void changePassword(ChangePasswordRequest request) {
        String userId = StpUtil.getLoginIdAsString();
        if (userId == null) {
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "未登录或登录已过期");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(AuthErrorConstants.USER_NOT_FOUND, "用户不存在");
        }

        if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorConstants.OLD_PASSWORD_ERROR, "旧密码错误");
        }

        // 走 UserService 以触发用户缓存整体驱逐，避免旧密码哈希仍被缓存命中
        userService.updatePassword(user.getId(), request.getNewPassword());

        // 无状态 JWT 无法单独吊销：递增版本号使用户全部存量访问令牌立即失效，
        // 并吊销全部刷新令牌强制重新登录
        tokenVersionService.bump(user.getId());
        refreshTokenService.revokeAllForUser(user.getId());
    }

    /**
     * 签发访问令牌（无状态 JWT，身份要素编入 extra claims）并配套签发刷新令牌。
     *
     * @param user     登录用户
     * @param platform 登录平台（决定访问令牌有效期）
     * @return 令牌对响应
     */
    private LoginResponse issueTokenPair(User user, String platform) {
        long expiresIn = issueAccessToken(user, platform);
        String refreshToken = refreshTokenService.issue(user.getId(), platform);
        return buildLoginResponse(StpUtil.getTokenValue(), refreshToken, expiresIn);
    }

    /**
     * 执行 Sa-Token 登录并返回访问令牌有效期（秒）。
     *
     * <p>claims 自包含 {@code role}/{@code uni}/{@code ver} 三项身份要素：
     * Stateless 模式下服务端不保存会话，RBAC 角色与数据范围隔离所需的
     * 用户类型、所属高校均从 JWT claims 读取；{@code ver} 为令牌版本号，
     * 改密/封禁时递增即可使存量令牌全端失效。
     *
     * @param user     登录用户
     * @param platform 登录平台
     * @return 有效期（秒）
     */
    private long issueAccessToken(User user, String platform) {
        long timeout = ACCESS_TIMEOUT_BY_PLATFORM.getOrDefault(platform, DEFAULT_ACCESS_TIMEOUT_SECONDS);
        StpUtil.login(
                user.getId(),
                SaLoginModel.create()
                        .setDevice(platform)
                        .setTimeout(timeout)
                        .setExtra(
                                JwtClaims.ROLE,
                                user.getUserType() == null
                                        ? null
                                        : user.getUserType().name())
                        .setExtra(JwtClaims.UNIVERSITY_ID, user.getUniversityId())
                        .setExtra(JwtClaims.TOKEN_VERSION, tokenVersionService.current(user.getId())));
        return timeout;
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken, long expiresIn) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }

    private String resolvePlatform(String platform) {
        return platform != null ? platform : "web";
    }

    /**
     * 记录一次登录失败；达到阈值时设置锁定 key。
     */
    private void recordLoginFail(String lockKey) {
        Long count = redisTemplate.opsForValue().increment(lockKey);
        // 每次失败都刷新锁定窗口，避免攻击者分散失败时间绕过锁定
        if (count != null) {
            redisTemplate.expire(lockKey, LOCK_DURATION);
        }
    }

    private void checkEmailRegistered(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (exists) {
            throw new BusinessException(UserErrorConstants.EMAIL_EXISTS, "该邮箱已被注册");
        }
    }

    private void checkPhoneRegistered(String phone) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exists) {
            throw new BusinessException(UserErrorConstants.PHONE_EXISTS, "该手机号已被注册");
        }
    }

    private void checkUsernameOccupied(String username) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists) {
            throw new BusinessException(UserErrorConstants.USERNAME_EXISTS, "该昵称已被占用");
        }
    }

    private void checkPartyMemberId(String partyMemberId) {
        if (partyMemberId == null || partyMemberId.isBlank()) {
            return;
        }
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId));
        if (exists) {
            throw new BusinessException(UserErrorConstants.PARTY_MEMBER_ID_EXISTS, "党员编号已存在");
        }
    }
}
