package com.rauio.smartdangjian.server.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import cn.hutool.crypto.digest.BCrypt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
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

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CaptchaService captchaService;
    private final UserMapper userMapper;
    private final UserService userService;

    public LoginResponse login(LoginRequest loginRequest) {
        if (!captchaService.validate(loginRequest.getCaptchaUUID(), loginRequest.getCaptchaCode())) {
            throw new BusinessException(AuthErrorConstants.CAPTCHA_ERROR, "验证码错误");
        }

        User user = userService.getByPassport(loginRequest.getPassport());
        if (user == null) {
            throw new BusinessException(AuthErrorConstants.USER_NOT_FOUND, "用户不存在");
        }

        if (user.getStatus() == AccountStatus.BANNED) {
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "账号已被封禁");
        }
        if (user.getStatus() == AccountStatus.INACTIVE) {
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "账号未激活");
        }

        if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorConstants.PASSWORD_ERROR, "密码错误");
        }

        String platform = loginRequest.getPlatform() != null ? loginRequest.getPlatform() : "web";
        long timeout = "app".equals(platform) ? 2592000L : 7200L;

        StpUtil.login(user.getId(), SaLoginModel.create()
                .setDevice(platform)
                .setTimeout(timeout));

        StpUtil.getSession().set("user", user);

        return LoginResponse.builder().accessToken(StpUtil.getTokenValue()).build();
    }

    public void logout() {
        StpUtil.logout();
    }

    public Result<Object> register(RegisterRequest registerRequest) {
        if (!captchaService.validate(registerRequest.getCaptchaUUID(), registerRequest.getCaptchaCode())) {
            throw new BusinessException(AuthErrorConstants.CAPTCHA_ERROR, "验证码错误");
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
                .userType(registerRequest.getType())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        return Result.ok("注册成功！");
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

        user.setPassword(BCrypt.hashpw(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        if (userMapper.updateById(user) <= 0) {
            throw new BusinessException(AuthErrorConstants.PASSWORD_CHANGE_ERROR, "密码修改失败");
        }
        StpUtil.getSession().set("user", user);
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
