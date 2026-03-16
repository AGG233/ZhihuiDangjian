package com.rauio.smartdangjian.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.constants.SecurityConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.mapper.UserMapper;
import com.rauio.smartdangjian.pojo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.rauio.smartdangjian.constants.RedisConstants.USER_CACHE_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private static final String SECRET_KEY = "smartdangjian-jwt-scrert_key@test^&*";
    private static final long DEFAULT_ACCESS_EXPIRATION = 3600000;  //  1 小时
    private static final long THRESHOLD = 600000;                   //  15 分钟

    public static final String PLATFORM_WEB = "web";
    public static final String PLATFORM_APP = "app";

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    /**
     * 1. 生成支持多平台的 Access Token
     * @param user 用户对象
     * @param platform 平台标识 (web, app)
     */
    public String generateAccessToken(User user, String platform) {

        long expiration = getExpirationByPlatform(platform);
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("platform", platform)
                .withClaim("role", user.getUserType().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);
    }

    /**
     * 验证签名和过期，并从 Redis 或数据库获取用户信息
     * @param token JWT Token
     * @return 如果验证成功返回用户对象，否则抛出异常
     */
    public User validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            String userIdStr = decodedJWT.getSubject();
            if (!StringUtils.hasText(userIdStr)) {
                throw new BusinessException(401, "令牌格式错误：Subject为空");
            }

            Long userId = Long.valueOf(userIdStr);

            User user = getUserFromCacheOrDb(userId);
            if (user == null) {
                throw new BusinessException(401, "用户不存在或已被删除");
            }

            return user;

        } catch (TokenExpiredException e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        } catch (JWTVerificationException e) {
            log.error("JWT验证失败: {}", e.getMessage());
            throw new BusinessException(401, "身份验证失败，请重新登录");
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "令牌负载数据异常");
        } catch (Exception e) {
            log.error("Token处理异常", e);
            throw new BusinessException(500, "服务器验证身份时出错");
        }
    }

    /**
     * 内部私有方法：处理缓存读取与回写逻辑
     */
    private User getUserFromCacheOrDb(Long userId) throws JsonProcessingException {
        String redisKey = USER_CACHE_PREFIX + userId;

        String userJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.hasText(userJson)) {
            return objectMapper.readValue(userJson, User.class);
        }

        User user = userMapper.selectById(userId);

        if (user != null) {
            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    objectMapper.writeValueAsString(user),
                    DEFAULT_ACCESS_EXPIRATION,
                    TimeUnit.MINUTES
            );
        }

        return user;
    }

    /**
     * 额外方法：当用户信息更新时，可以调用此方法清除缓存
     */
    public void clearUserCache(Long userId) {
        stringRedisTemplate.delete(USER_CACHE_PREFIX + userId);
    }
    
    /**
     * 从用户的 Token 中获取剩余过期时间
     * @param userId 用户 ID
     * @return 剩余毫秒数
     */
    private long getRemainingTtlFromToken(Long userId) {
        String cacheKey = SecurityConstants.ACCESS_TOKEN_PREFIX + userId;
        Long expire = stringRedisTemplate.getExpire(cacheKey, TimeUnit.MILLISECONDS);
        return expire > 0 ? expire : DEFAULT_ACCESS_EXPIRATION;
    }

    /**
     * 3. 验证 Token 是否属于特定平台
     * 场景：某些接口只允许 App 端调用
     */
    public boolean validateTokenForPlatform(String token, String requiredPlatform) {
        User user = validateToken(token);
        String tokenPlatform = getPlatformFromToken(token);
        return requiredPlatform.equalsIgnoreCase(tokenPlatform);
    }

    /**
     * 4. 从 Token 中提取平台信息
     */
    public String getPlatformFromToken(String token) {
        try {
            return JWT.decode(token).getClaim("platform").asString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 5. 从 Token 中提取用户 ID
     */
    public String getIdFromToken(String token) {
        try {
            return JWT.decode(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 6. 检查是否即将过期 (用于无感刷新续期判断)
     */
    public boolean isStrictlyNearExpiry(String token) {
        try {
            Date expiresAt = JWT.decode(token).getExpiresAt();
            return (expiresAt.getTime() - System.currentTimeMillis()) < THRESHOLD;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 根据平台返回对应的过期时间
     */
    private long getExpirationByPlatform(String platform) {
        return switch (platform.toLowerCase()) {
            case PLATFORM_WEB -> 3600000L * 2;       // Web 2小时
            case PLATFORM_APP -> 3600000L * 24 * 30; // App 30天
            default -> DEFAULT_ACCESS_EXPIRATION;
        };
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        }catch (Exception e){
            throw new BusinessException(404,"令牌错误，请重新登录");
        }
    }

    public void logout(String token) {
        String id = getIdFromToken(token);
        long expireTime = JWT.decode(token).getExpiresAt().getTime() - System.currentTimeMillis();
        if (expireTime > 0) {
            stringRedisTemplate.opsForValue().set("blacklist:" + token, "1", expireTime, TimeUnit.MILLISECONDS);
        }
    }
}