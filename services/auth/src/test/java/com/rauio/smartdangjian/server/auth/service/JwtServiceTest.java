package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtService jwtService;

    private ValueOperations<String, String> valueOps;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private User createUser(String id, String username, UserType userType) {
        return User.builder()
                .id(id)
                .username(username)
                .email("test@example.com")
                .phone("13800138000")
                .userType(userType)
                .build();
    }

    // ================================================================
    // generateAccessToken
    // ================================================================

    @Test
    @DisplayName("generateAccessToken 生成非空 JWT Token")
    void generateAccessTokenReturnsNonBlankToken() {
        User user = createUser("u1", "testuser", UserType.STUDENT);

        String token = jwtService.generateAccessToken(user, "web");

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateAccessToken Web 平台 Token 有效期为 2 小时")
    void generateAccessTokenWebPlatformHas2HourExpiry() {
        User user = createUser("u1", "testuser", UserType.STUDENT);

        String token = jwtService.generateAccessToken(user, "web");
        DecodedJWT decoded = jwtService.decodeToken(token);

        long expiry = decoded.getExpiresAt().getTime() - decoded.getIssuedAt().getTime();
        assertThat(expiry).isEqualTo(3600000L * 2);
    }

    @Test
    @DisplayName("generateAccessToken App 平台 Token 有效期为 30 天")
    void generateAccessTokenAppPlatformHas30DayExpiry() {
        User user = createUser("u1", "testuser", UserType.STUDENT);

        String token = jwtService.generateAccessToken(user, "app");
        DecodedJWT decoded = jwtService.decodeToken(token);

        long expiry = decoded.getExpiresAt().getTime() - decoded.getIssuedAt().getTime();
        assertThat(expiry).isEqualTo(3600000L * 24 * 30);
    }

    @Test
    @DisplayName("generateAccessToken 未知平台使用默认过期时间")
    void generateAccessTokenUnknownPlatformUsesDefaultExpiry() {
        User user = createUser("u1", "testuser", UserType.STUDENT);

        String token = jwtService.generateAccessToken(user, "unknown");
        DecodedJWT decoded = jwtService.decodeToken(token);

        long expiry = decoded.getExpiresAt().getTime() - decoded.getIssuedAt().getTime();
        assertThat(expiry).isEqualTo(3600000L);
    }

    // ================================================================
    // getIdFromToken
    // ================================================================

    @Test
    @DisplayName("getIdFromToken 从 Token 中提取用户 ID")
    void getIdFromTokenExtractsUserId() {
        User user = createUser("user-id-123", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        String id = jwtService.getIdFromToken(token);

        assertThat(id).isEqualTo("user-id-123");
    }

    @Test
    @DisplayName("getIdFromToken 无效 Token 返回 null")
    void getIdFromTokenReturnsNullForInvalidToken() {
        String id = jwtService.getIdFromToken("invalid-token");

        assertThat(id).isNull();
    }

    // ================================================================
    // getPlatformFromToken
    // ================================================================

    @Test
    @DisplayName("getPlatformFromToken 从 Token 中提取平台信息")
    void getPlatformFromTokenExtractsPlatform() {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        String platform = jwtService.getPlatformFromToken(token);

        assertThat(platform).isEqualTo("web");
    }

    @Test
    @DisplayName("getPlatformFromToken 无效 Token 返回 null")
    void getPlatformFromTokenReturnsNullForInvalidToken() {
        String platform = jwtService.getPlatformFromToken("invalid-token");

        assertThat(platform).isNull();
    }

    // ================================================================
    // decodeToken
    // ================================================================

    @Test
    @DisplayName("decodeToken 解码有效 Token 返回 DecodedJWT")
    void decodeTokenReturnsDecodedJwt() {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        DecodedJWT decoded = jwtService.decodeToken(token);

        assertThat(decoded).isNotNull();
        assertThat(decoded.getSubject()).isEqualTo("u1");
    }

    @Test
    @DisplayName("decodeToken 无效 Token 抛出 BusinessException")
    void decodeTokenThrowsForInvalidToken() {
        assertThatThrownBy(() -> jwtService.decodeToken("invalid-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.TOKEN_DECODE_ERROR);
    }

    // ================================================================
    // isStrictlyNearExpiry
    // ================================================================

    @Test
    @DisplayName("isStrictlyNearExpiry 新生成的 Token 未临近过期返回 false")
    void isStrictlyNearExpiryReturnsFalseForFreshToken() {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        boolean result = jwtService.isStrictlyNearExpiry(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isStrictlyNearExpiry 无效 Token 返回 true")
    void isStrictlyNearExpiryReturnsTrueForInvalidToken() {
        boolean result = jwtService.isStrictlyNearExpiry("invalid-token");

        assertThat(result).isTrue();
    }

    // ================================================================
    // validateToken
    // ================================================================

    @Test
    @DisplayName("validateToken 有效 Token 且用户存在于数据库时返回 User")
    void validateTokenReturnsUserWhenValidAndUserInDb() throws JsonProcessingException {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        when(stringRedisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(valueOps.get(anyString())).thenReturn(null);
        when(userMapper.selectById("u1")).thenReturn(user);
        when(objectMapper.writeValueAsString(any(User.class))).thenReturn("{}");

        User result = jwtService.validateToken(token);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("u1");
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("validateToken Token 在黑名单中时抛出 BusinessException(TOKEN_VERIFICATION_FAILED)")
    void validateTokenThrowsWhenBlacklisted() {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        when(stringRedisTemplate.hasKey("blacklist:" + token)).thenReturn(true);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.TOKEN_VERIFICATION_FAILED);
    }

    @Test
    @DisplayName("validateToken 用户不在数据库中时抛出 BusinessException(TOKEN_USER_NOT_FOUND)")
    void validateTokenThrowsWhenUserNotFound() throws JsonProcessingException {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        when(stringRedisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(valueOps.get(anyString())).thenReturn(null);
        when(userMapper.selectById("u1")).thenReturn(null);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(AuthErrorConstants.TOKEN_USER_NOT_FOUND);
    }

    @Test
    @DisplayName("validateToken 用户存在于缓存中时直接从缓存返回")
    void validateTokenReturnsUserFromCache() throws JsonProcessingException {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        when(stringRedisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(valueOps.get(anyString())).thenReturn("{\"id\":\"u1\",\"username\":\"cacheduser\"}");
        when(objectMapper.readValue(anyString(), eq(User.class))).thenReturn(user);

        User result = jwtService.validateToken(token);

        assertThat(result).isEqualTo(user);
        verify(userMapper, never()).selectById(anyString());
    }

    @Test
    @DisplayName("validateToken 无效 Token 抛出 BusinessException(Token_Verification_Failed)")
    void validateTokenThrowsForInvalidToken() {
        assertThatThrownBy(() -> jwtService.validateToken("invalid-token")).isInstanceOf(BusinessException.class);
    }

    // ================================================================
    // validateTokenForPlatform
    // ================================================================

    @Test
    @DisplayName("validateTokenForPlatform 平台匹配时返回 true")
    void validateTokenForPlatformReturnsTrueWhenMatch() throws JsonProcessingException {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        when(stringRedisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(valueOps.get(anyString())).thenReturn(null);
        when(userMapper.selectById("u1")).thenReturn(user);
        when(objectMapper.writeValueAsString(any(User.class))).thenReturn("{}");

        boolean result = jwtService.validateTokenForPlatform(token, "web");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateTokenForPlatform 平台不匹配时返回 false")
    void validateTokenForPlatformReturnsFalseWhenMismatch() throws JsonProcessingException {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        when(stringRedisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(valueOps.get(anyString())).thenReturn(null);
        when(userMapper.selectById("u1")).thenReturn(user);
        when(objectMapper.writeValueAsString(any(User.class))).thenReturn("{}");

        boolean result = jwtService.validateTokenForPlatform(token, "app");

        assertThat(result).isFalse();
    }

    // ================================================================
    // logout
    // ================================================================

    @Test
    @DisplayName("logout 将 Token 加入黑名单并设置过期时间")
    void logoutAddsTokenToBlacklist() {
        User user = createUser("u1", "testuser", UserType.STUDENT);
        String token = jwtService.generateAccessToken(user, "web");

        jwtService.logout(token);

        verify(valueOps).set(eq("blacklist:" + token), eq("1"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    // ================================================================
    // clearUserCache
    // ================================================================

    @Test
    @DisplayName("clearUserCache 删除指定用户的缓存")
    void clearUserCacheDeletesCache() {
        jwtService.clearUserCache("u1");

        verify(stringRedisTemplate).delete("user:data:u1");
    }
}
