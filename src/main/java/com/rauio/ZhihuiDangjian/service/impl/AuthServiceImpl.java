package com.rauio.ZhihuiDangjian.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rauio.ZhihuiDangjian.constants.SecurityConstants;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjian.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjian.pojo.response.LoginResponse;
import com.rauio.ZhihuiDangjian.pojo.response.Result;
import com.rauio.ZhihuiDangjian.service.*;
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

import java.time.LocalDateTime;
import java.util.Map;

import static com.rauio.ZhihuiDangjian.constants.ErrorConstants.ARGS_ERROR;

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

        // todo 创建用户时传入类型可以创建指定的用户类型
//        if(Objects.equals(registerRequest.getType(), "admin")){
//            user.setUserType(UserType.MANAGER);
//        } else if (Objects.equals(registerRequest.getType(), "teacher")) {
//            user.setUserType(UserType.SCHOOL);
//        }else{
//            user.setUserType(UserType.STUDENT);
//        }

        userService.register(user);
        return new Result<Object>();
    }

    /**
     * @return 新的刷新令牌和访问令牌
     */
    @Override
    public Result<Map<String,String>> refresh(String refreshToken) {
        try {
            String userId = jwtService.getDecodedJWT(refreshToken).getSubject();

            String redisKey = SecurityConstants.REFRESH_TOKEN_PREFIX + userId;
            String refreshKey = stringRedisTemplate.opsForValue().get(redisKey);
            if (refreshKey == null) {
                new Result<>();
                return Result.error("非法请求","400");
            }

            Map<String, String> result = jwtService.refreshAccessToken(refreshToken);


            return new Result<>(result);
        } catch (JWTVerificationException e) {
            // JWT验证失败，返回非法请求
            new Result<>();
            return Result.error("非法请求","400");
        } catch (Exception e) {
            // 其他异常，也返回非法请求
            log.error("JWT验证失败", e);
            new Result<>();
            return Result.error("非法请求","400");
        }
    }

    @Override
    public Result<Object> logout(String refreshToken) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        String redisKey = SecurityConstants.REFRESH_TOKEN_PREFIX + id;
        Object refreshKey = redisTemplate.opsForValue().get(redisKey);

        if (refreshKey == null) {
            new Result<>();
            return Result.error("非法请求","400");
        }

        redisTemplate.delete(SecurityConstants.ACCESS_TOKEN_PREFIX + id + ":*");
        redisTemplate.delete(SecurityConstants.REFRESH_TOKEN_PREFIX + id + ":*");

        return new Result<>();
    }

}