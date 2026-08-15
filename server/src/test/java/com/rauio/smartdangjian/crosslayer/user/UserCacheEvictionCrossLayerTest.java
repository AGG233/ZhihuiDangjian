package com.rauio.smartdangjian.crosslayer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import com.rauio.smartdangjian.server.user.service.UserService;

/**
 * 用户缓存一致性跨层回归：真实 UserService（含 @Cacheable/@CacheEvict 代理）+ 真实 Redis 缓存，
 * 验证修改密码后 user:data:* 缓存被整体驱逐（P0 修复 #2）。
 */
@SpringBootTest(classes = UserCacheEvictionCrossLayerTest.TestConfig.class)
class UserCacheEvictionCrossLayerTest extends CrossLayerTestBase {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserConvertor userConvertor;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String uniquePassport;

    @SpringBootConfiguration
    @Import(UserService.class)
    static class TestConfig extends CrossLayerTestConfig {}

    @BeforeEach
    void setUpUniqueKey() {
        uniquePassport =
                "evict-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @AfterEach
    void cleanUpRedisKeys() {
        Set<String> keys = redisTemplate.keys("user:data:::" + uniquePassport);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("getByPassport 写入缓存，updatePassword 后缓存被整体驱逐")
    void passwordUpdateEvictsUserCache() {
        User user = User.builder()
                .id(1L)
                .username(uniquePassport)
                .password("old-hash")
                .build();
        // BaseMapper.selectOne 是 default 方法（Mockito 不执行其真实实现），
        // 必须用 doReturn 语法 stub（when 语法会先执行 default 方法拿到 null）
        doReturn(user).when(userMapper).selectOne(any(), anyBoolean());
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 首次查询写入缓存
        userService.getByPassport(uniquePassport);
        assertThat(redisTemplate.hasKey("user:data:::" + uniquePassport)).isTrue();

        // 修改密码触发 @CacheEvict(allEntries = true)
        userService.updatePassword(1L, "new-raw-password");
        assertThat(redisTemplate.hasKey("user:data:::" + uniquePassport)).isFalse();

        // 驱逐后再次查询重新走 DB
        userService.getByPassport(uniquePassport);
        verify(userMapper, times(2)).selectOne(any(), anyBoolean());
    }
}
