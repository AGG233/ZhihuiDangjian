package com.rauio.ZhihuiDangjian.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.rauio.ZhihuiDangjian.pojo.User;

import java.security.KeyPair;
import java.util.Map;

public interface JwtService {
    String      generateAccessToken(User user);
    String      generateRefreshToken(User user);

    String      createToken(User user, long expirationTime);
    Boolean     validateToken(String token, User user);
    String      getIdFromToken(String token);
    Boolean     isTokenExpired(String token);
    DecodedJWT  getDecodedJWT(String token);

    Map<String, String> refreshAccessToken(String refreshToken);

    void        invalidateToken(String username);
    long        getAccessExpirationTime();
    long        getRefreshExpirationTime();
    
    // RSA加密相关方法
    KeyPair     generateKeyPair();
    String      encryptSecretKey(String secretKey);
    String      decryptSecretKey(String encryptedSecretKey);
    void        storeEncryptedSecretKey(String encryptedSecretKey);
    String      getEncryptedSecretKey();
}