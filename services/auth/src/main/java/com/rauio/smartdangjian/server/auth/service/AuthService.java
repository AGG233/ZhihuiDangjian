package com.rauio.smartdangjian.server.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.auth.pojo.response.LoginResponse;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.rauio.smartdangjian.constants.ErrorConstants.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CaptchaService captchaService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        if (!captchaService.validate(loginRequest.getCaptchaUUID(), loginRequest.getCaptchaCode())) {
            throw new BusinessException(ARGS_ERROR, "验证码错误");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getPassport(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(user, loginRequest.getPlatform());

            return LoginResponse.builder().accessToken(accessToken).build();
        }catch (Exception e){
            throw new BusinessException("密码错误");
        }



    }

    public void logout(String token) {
        jwtService.logout(token);
    }

    public Result<Object> register(RegisterRequest registerRequest) {
        if (!captchaService.validate(registerRequest.getCaptchaUUID(), registerRequest.getCaptchaCode())) {
            throw new BusinessException(ARGS_ERROR, "验证码错误");
        }

        checkEmailRegistered(registerRequest.getEmail());
        checkPhoneRegistered(registerRequest.getPhone());
        checkUsernameOccupied(registerRequest.getUsername());
        checkPartyMemberId(registerRequest.getPartyMemberId());

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        return Result.ok("注册成功！");
    }

    public Boolean changePassword(ChangePasswordRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ARGS_ERROR, "旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        jwtService.clearUserCache(userId);
        return userMapper.updateById(user) > 0;
    }

    private void checkEmailRegistered(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (exists) {
            throw new BusinessException(EMAIL_EXISTS, "该邮箱已被注册");
        }
    }

    private void checkPhoneRegistered(String phone) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exists) {
            throw new BusinessException(PHONE_EXISTS, "该手机号已被注册");
        }
    }

    private void checkUsernameOccupied(String username) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists) {
            throw new BusinessException(USERNAME_EXISTS, "该昵称已被占用");
        }
    }

    private void checkPartyMemberId(String partyMemberId) {
        if (partyMemberId == null || partyMemberId.isBlank()) {
            return;
        }
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId));
        if (exists) {
            throw new BusinessException(PARTY_MEMBER_ID_EXISTS, "党员编号已存在");
        }
    }
}
