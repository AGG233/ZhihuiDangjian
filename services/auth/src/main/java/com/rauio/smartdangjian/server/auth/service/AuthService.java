package com.rauio.smartdangjian.server.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
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

    /** 登录失败锁定阈值 */
    private static final long MAX_LOGIN_FAILS = 5;

    /** 登录锁定时长 */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final CaptchaService captchaService;
    private final UserMapper userMapper;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

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

        String platform = loginRequest.getPlatform() != null ? loginRequest.getPlatform() : "web";
        long timeout = "app".equals(platform) ? 2592000L : 7200L;

        StpUtil.login(user.getId(), SaLoginModel.create().setDevice(platform).setTimeout(timeout));

        StpUtil.getSession().set("user", user);

        return LoginResponse.builder().accessToken(StpUtil.getTokenValue()).build();
    }

    public void logout() {
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

        userMapper.insert(user);
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

        User updated = userMapper.selectById(userId);
        StpUtil.getSession().set("user", updated);
    }

    /**
     * 记录一次登录失败；达到阈值时设置锁定 key。
     */
    private void recordLoginFail(String lockKey) {
        Long count = redisTemplate.opsForValue().increment(lockKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(lockKey, LOCK_DURATION);
        }
        if (count != null && count >= MAX_LOGIN_FAILS) {
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
