package com.rauio.ZhihuiDangjiang.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjiang.constants.SecurityConstants;
import com.rauio.ZhihuiDangjiang.exception.BusinessException;
import com.rauio.ZhihuiDangjiang.pojo.User;
import com.rauio.ZhihuiDangjiang.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjiang.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjiang.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjiang.pojo.response.LoginResponse;
import com.rauio.ZhihuiDangjiang.service.*;
import com.rauio.ZhihuiDangjiang.utils.Spec.UserType;
import jakarta.servlet.http.HttpServlet;
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

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rauio.ZhihuiDangjiang.constants.ErrorConstants.ARGS_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends HttpServlet implements AuthService {

    private final RedisTemplate<String,Object>  redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthenticationManager         authenticationManager;
    private final UserService                   userService;
    private final JwtService                    jwtService;
    private final CaptchaService                captchaService;
    private final UniversitiesService           universitiesService;
    private final ObjectMapper                  objectMapper;

    @Override
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
//        if (!captchaService.validate(loginRequest.getCaptchaUUID(), loginRequest.getCaptchaCode())) {
//            throw new BusinessException(ARGS_ERROR,"验证码错误");
//        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getPassport(),
                        loginRequest.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        String refreshToken = jwtService.generateRefreshToken(user);
        String accessToken  = jwtService.generateAccessToken(user);

        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    public ApiResponse register(RegisterRequest registerRequest) {
        if (!captchaService.validate(registerRequest.getCaptchaUUID(), registerRequest.getCaptchaCode())) {
            throw new BusinessException(ARGS_ERROR,"验证码错误");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .realName(registerRequest.getReal_name())
                .idCard(registerRequest.getId_card())
                .partyMemberId(registerRequest.getParty_member_id())
                .partyStatus(registerRequest.getParty_status())
                .branchName(registerRequest.getBranch_name())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .universityId(universitiesService.getIdByName(registerRequest.getUniversityName()))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        // todo 创建用户时传入类型可以创建指定的用户类型
        if(Objects.equals(registerRequest.getType(), "admin")){
            user.setUserType(UserType.MANAGER);
        } else if (Objects.equals(registerRequest.getType(), "teacher")) {
            user.setUserType(UserType.TEACHER);
        }else{
            user.setUserType(UserType.STUDENT);
        }

        userService.register(user);
        return new ApiResponse();
    }

    /**
     * @return 新的刷新令牌和访问令牌
     */
    @Override
    public ApiResponse refresh(String refreshToken, String accessToken) {
        try {
            String userId = jwtService.getDecodedJWT(accessToken).getSubject();

            String redisKey = SecurityConstants.REFRESH_TOKEN_PREFIX + userId;
            String refreshKey = stringRedisTemplate.opsForValue().get(redisKey);
            if (refreshKey == null) {
                return new ApiResponse("非法请求");
            }

            Map<String, String> result = jwtService.refreshAccessToken(refreshToken);

            return new ApiResponse(result);
        } catch (JWTVerificationException e) {
            // JWT验证失败，返回非法请求
            return new ApiResponse("非法请求");
        } catch (Exception e) {
            // 其他异常，也返回非法请求
            log.error("JWT验证失败", e);
            return new ApiResponse("非法请求");
        }
    }

    @Override
    public ApiResponse logout(String refreshToken) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        String redisKey = SecurityConstants.REFRESH_TOKEN_PREFIX + id;
        Object refreshKey = redisTemplate.opsForValue().get(redisKey);

        if (refreshKey == null) {
            return new ApiResponse("非法请求");
        }

        redisTemplate.delete(SecurityConstants.ACCESS_TOKEN_PREFIX + id + ":*");
        redisTemplate.delete(SecurityConstants.REFRESH_TOKEN_PREFIX + id + ":*");

        return new ApiResponse();
    }

}