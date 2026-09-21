package com.rauio.smartdangjian.crosslayer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

/**
 * 用户缓存一致性跨层回归：真实 UserService（含 @Cacheable/@CacheEvict 代理）+ 真实 Redis 缓存，
 * 验证修改密码后 user:data:* 缓存被整体驱逐。
 *
 * <p>注意：凭据查询（{@code getByPassport}/{@code getByUsername} 等）自 0.10.1 起不再缓存
 * User 实体——缓存会因 {@code @JsonProperty(WRITE_ONLY)} 丢失密码哈希，导致同一账号二次登录失败。
 * 因此本测试改用仍会缓存的 {@link UserService#get(Long)}（返回不含密码的视图对象）建立缓存样本，
 * 凭据查询不缓存的行为由 {@code UserAuthCacheCrossLayerTest} 覆盖。
 */
@SpringBootTest(classes = UserCacheEvictionCrossLayerTest.TestConfig.class)
class UserCacheEvictionCrossLayerTest extends CrossLayerTestBase {

    private static final long TEST_USER_ID = 987654L;
    private static final String CACHE_KEY = "user:data:::" + TEST_USER_ID;

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserConvertor userConvertor;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @SpringBootConfiguration
    @Import(UserService.class)
    static class TestConfig extends CrossLayerTestConfig {}

    @AfterEach
    void cleanUpRedisKeys() {
        redisTemplate.delete(CACHE_KEY);
    }

    @Test
    @DisplayName("get 写入缓存，updatePassword 后缓存被整体驱逐")
    void passwordUpdateEvictsUserCache() {
        User user = User.builder()
                .id(TEST_USER_ID)
                .username("evict-probe")
                .password("old-hash")
                .build();
        when(userMapper.selectById(TEST_USER_ID)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userConvertor.toResponse(any(User.class))).thenReturn(new UserResponse());

        // 首次查询写入缓存
        userService.get(TEST_USER_ID);
        assertThat(redisTemplate.hasKey(CACHE_KEY)).isTrue();

        // 修改密码触发 @CacheEvict(allEntries = true)
        userService.updatePassword(TEST_USER_ID, "new-raw-password");
        assertThat(redisTemplate.hasKey(CACHE_KEY)).isFalse();

        // 驱逐后再次查询重新走 DB；updatePassword 内部也会回查一次用户，故 selectById 共 3 次。
        // 若缓存未被驱逐，第二次 get 会命中缓存，此处只会是 2 次。
        userService.get(TEST_USER_ID);
        verify(userMapper, times(3)).selectById(TEST_USER_ID);
    }
}
