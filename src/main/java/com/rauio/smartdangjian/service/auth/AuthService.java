package com.rauio.smartdangjian.service.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rauio.smartdangjian.constants.SecurityConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.request.LoginRequest;
import com.rauio.smartdangjian.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.pojo.response.LoginResponse;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.service.common.UniversitiesService;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Map;

import static com.rauio.smartdangjian.constants.ErrorConstants.ARGS_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager         authenticationManager;
    private final UserService                   userService;
    private final JwtService                    jwtService;
    private final CaptchaService                captchaService;

    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        if (!captchaService.validate(loginRequest.getCaptchaUUID(), loginRequest.getCaptchaCode())) {
            throw new BusinessException(ARGS_ERROR,"验证码错误");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getPassport(),
                        loginRequest.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String accessToken  = jwtService.generateAccessToken(user,loginRequest.getPlatform());

        return LoginResponse.builder().accessToken(accessToken).build();
    }


    public Result<Object> register(RegisterRequest registerRequest) {
        if (!captchaService.validate(registerRequest.getCaptchaUUID(), registerRequest.getCaptchaCode())) {
            throw new BusinessException(ARGS_ERROR,"验证码错误");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .realName(registerRequest.getRealName())
                .idCard(registerRequest.getIdCard())
                .partyMemberId(registerRequest.getPartyMemberId())
                .partyStatus(registerRequest.getPartyStatus())
                .branchName(registerRequest.getBranchName())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .universityId(registerRequest.getUniversityId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userService.register(user);
        return Result.ok("注册成功！");
    }

    public Result logout(String token) {
        jwtService.logout(token);

        return Result.ok("");
    }

}
