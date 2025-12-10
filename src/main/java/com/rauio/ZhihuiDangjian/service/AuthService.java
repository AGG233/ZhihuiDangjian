package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.request.LoginRequest;
import com.rauio.ZhihuiDangjian.pojo.request.RegisterRequest;
import com.rauio.ZhihuiDangjian.pojo.response.LoginResponse;
import com.rauio.ZhihuiDangjian.pojo.response.Result;

import java.util.Map;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
    Result register(RegisterRequest registerRequest);

    Result<Map<String,String>> refresh(String refreshToken);

    Result logout(String refreshToken);
}