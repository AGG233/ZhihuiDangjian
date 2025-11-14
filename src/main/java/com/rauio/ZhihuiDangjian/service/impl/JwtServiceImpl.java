package com.rauio.ZhihuiDangjian.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rauio.ZhihuiDangjian.constants.SecurityConstants;
import com.rauio.ZhihuiDangjian.mapper.UserMapper;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.service.JwtService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.RsaUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.rauio.ZhihuiDangjian.constants.SecurityConstants.*;

@Slf4j
@Getter
@RequiredArgsConstructor
@Service
public class JwtServiceImpl implements JwtService {

    private final StringRedisTemplate   stringRedisTemplate;
    private final UserService           userService;
    private final UserMapper userMapper;

    // 系统级RSA密钥对的Redis键名
    private static final String SYSTEM_RSA_PUBLIC_KEY = "system:rsa:public";
    private static final String SYSTEM_RSA_PRIVATE_KEY = "system:rsa:private";
    private static final String ENCRYPTED_HMAC_KEY = "system:hmac:encrypted";

    /**
     * 生成访问令牌 (Access Token)
     * @param user 用户信息
     * @return Access Token
     */
    @Override
    public String generateAccessToken(User user) {
        String token = createToken(user, ACCESS_TOKEN_EXPIRATION);
        String redisKey = SecurityConstants.ACCESS_TOKEN_PREFIX + user.getId();

        stringRedisTemplate.opsForValue().set(
                redisKey,
                token,
                ACCESS_TOKEN_EXPIRATION,
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    /**
     * 生成刷新令牌 (Refresh Token)
     * @param user 用户信息
     * @return Refresh Token
     */

    @Override
    public String generateRefreshToken(User user) {
        String token = createToken(user, REFRESH_TOKEN_EXPIRATION);
        String redisKey = SecurityConstants.REFRESH_TOKEN_PREFIX + user.getId();

        stringRedisTemplate.opsForValue().set(
                redisKey,
                token,
                REFRESH_TOKEN_EXPIRATION,
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    /**
     * 统一的令牌创建方法
     * @param user 用户
     * @param expirationTime 过期时长 (毫秒)
     * @return JWT
     */
    @Override
    public String createToken(User user, long expirationTime) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        String actualSecretKey = getHmacSecretKey();

        return JWT.create()
                .withSubject(user.getId())
                .withClaim("role", user.getUserType().toString())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(actualSecretKey));
    }

    /**
     * 从 Token 中解析出用户名 (Subject)
     * @param token JWT
     * @return 用户名
     */
    @Override
    public String getIdFromToken(String token) {
        return getDecodedJWT(token).getSubject();
    }

    /**
     * 校验 Token 是否有效
     * 1. 验证签名是否正确
     * 2. 验证时间是否过期
     * 3. 验证 subject 是否与 userDetails 中的用户名匹配
     *
     * @param token         JWT
     * @param user          用户
     * @return 是否有效
     */
    @Override
    public Boolean validateToken(String token, User user) {
        String id = getIdFromToken(token);
        if (id == null || !id.equals(user.getId())) {
            return false;
        }

        String redisKey = SecurityConstants.ACCESS_TOKEN_PREFIX + id;
        String redisToken = stringRedisTemplate.opsForValue().get(redisKey);

        if (redisToken == null || !redisToken.equals(token)) {
            return false;
        }
        return !isTokenExpired(token);
    }
    /**
     * 检查 Token 是否已过期 (此方法在 `validateToken` 中已隐式包含，可单独用于特定场景)
     *
     * @param token JWT
     * @return 是否已过期
     */
    @Override
    public Boolean isTokenExpired(String token) {
        final Date expiration = getDecodedJWT(token).getExpiresAt();
        return expiration.before(new Date());
    }

    @Override
    public void invalidateToken(String username) {
        String accessKey = SecurityConstants.ACCESS_TOKEN_PREFIX + username;
        String refreshKey = SecurityConstants.REFRESH_TOKEN_PREFIX + username;
        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.delete(refreshKey);
    }

    /**
     * @return 访问令牌过期时长 (毫秒)
     */
    @Override
    public long getAccessExpirationTime() {
        return ACCESS_TOKEN_EXPIRATION;
    }

    /**
     * @return 刷新令牌过期时长 (毫秒)
     */
    @Override
    public long getRefreshExpirationTime() {
        return REFRESH_TOKEN_EXPIRATION;
    }

    /**
     * 获取解码后的 JWT 对象，包含了所有的 claims
     * 这个方法会处理所有的验证 (签名, 过期时间等)
     * @param token JWT
     * @return DecodedJWT
     * @throws JWTVerificationException 如果验证失败
     */
    @Override
    public DecodedJWT getDecodedJWT(String token) {

        String actualSecretKey = getHmacSecretKey();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(actualSecretKey)).build();
        return verifier.verify(token);
    }

    /**
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌和刷新令牌
     */
    @Override
    public Map<String, String> refreshAccessToken(String refreshToken) {
       DecodedJWT   decodedJWT  = getDecodedJWT(refreshToken);
        String id = decodedJWT.getSubject();
        if (id == null) {
            return null;
        }

        // 验证 refreshToken
        String redisKey = SecurityConstants.REFRESH_TOKEN_PREFIX + id;
        String redisToken   = stringRedisTemplate.opsForValue().get(redisKey);
        if(!StringUtils.hasText(redisToken) || !redisToken.equals(refreshToken)){
            return null;
        }

        User user = userMapper.selectById(id);

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        Map<String, String> result = new HashMap<>();
        result.put("access_token", newAccessToken);
        result.put("refresh_token", newRefreshToken);

        return result;
    }

    /**
     * 生成RSA密钥对
     * @return RSA密钥对
     */
    @Override
    public KeyPair generateKeyPair() {
        RsaUtil rsaUtil = new RsaUtil();
        try {
            return rsaUtil.generateRsaKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 获取或生成系统级RSA密钥对（持久化到Redis）
     * @return RSA密钥对
     */
    private KeyPair getOrCreateSystemKeyPair() {
        String publicKeyStr = stringRedisTemplate.opsForValue().get(SYSTEM_RSA_PUBLIC_KEY);
        String privateKeyStr = stringRedisTemplate.opsForValue().get(SYSTEM_RSA_PRIVATE_KEY);

        if (publicKeyStr != null && privateKeyStr != null) {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);

            X509EncodedKeySpec  publicKeySpec   = new X509EncodedKeySpec(publicKeyBytes);
            PKCS8EncodedKeySpec privateKeySpec  = new PKCS8EncodedKeySpec(privateKeyBytes);

            try{
                KeyFactory keyFactory = KeyFactory.getInstance(SecurityConstants.RSA_KEY_ALGORITHM);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

                return new KeyPair(publicKey, privateKey);
            }catch (InvalidKeySpecException | NoSuchAlgorithmException e){
                throw new RuntimeException(e);
            }

        }
        KeyPair keyPair = generateKeyPair();
        if (keyPair != null) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

            String encodedPublicKey = Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());
            String encodedPrivateKey = Base64.getEncoder().encodeToString(rsaPrivateKey.getEncoded());

            stringRedisTemplate.opsForValue().set(SYSTEM_RSA_PUBLIC_KEY, encodedPublicKey);
            stringRedisTemplate.opsForValue().set(SYSTEM_RSA_PRIVATE_KEY, encodedPrivateKey);

            return keyPair;
        }
        return null;
    }
    
    /**
     * 使用RSA公钥加密HMAC密钥
     * @param secretKey 原始密钥
     * @return 加密后的密钥
     */
    @Override
    public String encryptSecretKey(String secretKey) {
        try {
            KeyPair keyPair = getOrCreateSystemKeyPair();
            if (keyPair == null) {
                return null;
            }
            
            PublicKey publicKey = keyPair.getPublic();
            Cipher cipher = Cipher.getInstance(SecurityConstants.RSA_KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] encryptedBytes = cipher.doFinal(secretKey.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 使用RSA私钥解密HMAC密钥
     * @param encryptedSecretKey 加密的密钥
     * @return 解密后的密钥
     */
    @Override
    public String decryptSecretKey(String encryptedSecretKey) {
        try {
            KeyPair keyPair = getOrCreateSystemKeyPair();
            if (keyPair == null) {
                return null;
            }
            PrivateKey privateKey = keyPair.getPrivate();
            Cipher cipher = Cipher.getInstance(SecurityConstants.RSA_KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedSecretKey));
            return new String(decryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
    /**
     * 将加密后的HMAC密钥存储到Redis中
     * @param encryptedSecretKey 加密后的密钥
     */
    @Override
    public void storeEncryptedSecretKey(String encryptedSecretKey) {
        stringRedisTemplate.opsForValue().set(ENCRYPTED_HMAC_KEY, encryptedSecretKey);
    }
    
    /**
     * 从Redis中获取加密后的HMAC密钥
     * @return 加密后的密钥
     */
    @Override
    public String getEncryptedSecretKey() {
        return stringRedisTemplate.opsForValue().get(ENCRYPTED_HMAC_KEY);
    }
    
    /**
     * 获取HMAC密钥（解密后的）
     * @return HMAC密钥
     */
    private String getHmacSecretKey() {
        String encryptedKey = getEncryptedSecretKey();
        if (encryptedKey != null) {
            return decryptSecretKey(encryptedKey);
        }
        return DEFAULT_SECRET_KEY;
    }
}