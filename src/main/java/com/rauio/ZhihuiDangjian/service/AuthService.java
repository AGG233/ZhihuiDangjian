package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjian.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
    ApiResponse register(RegisterRequest registerRequest);

    ApiResponse refresh(String refreshToken, String accessToken);

    ApiResponse logout(String refreshToken);
}