package com.rauio.ZhihuiDangjiang.service;

import com.rauio.ZhihuiDangjiang.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjiang.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjiang.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjiang.pojo.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
    ApiResponse register(RegisterRequest registerRequest);

    ApiResponse refresh(String refreshToken, String accessToken);

    ApiResponse logout(String refreshToken);
}