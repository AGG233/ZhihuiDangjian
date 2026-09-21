package com.rauio.smartdangjian.crosslayer.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.constants.RedisConstants;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

/**
 * 认证路径用户查询缓存回归。
 *
 * <p>凭据查询（{@code getByPassport}/{@code getByUsername} 等）返回的 {@link User} 携带密码哈希，
 * 而 {@code User.password} 以 {@code @JsonProperty(WRITE_ONLY)} 序列化：一旦被 {@code @Cacheable}
 * 写入 Redis 缓存，缓存命中时 password 为 null，{@code BCrypt.checkpw} 必然失败——表现为同一账号
 * 在缓存有效期（30 分钟）内二次登录被误判为「用户名或密码错误」。
 *
 * <p>本测试连接真实 Redis 缓存与真实 UserService：既断言连续两次查询都拿到密码哈希，
 * 也断言凭据查询不会在 {@code user:data:} 缓存空间留下任何条目。
 */
@SpringBootTest(classes = UserAuthCacheCrossLayerTest.TestConfig.class)
@DisplayName("认证路径用户查询缓存回归")
class UserAuthCacheCrossLayerTest extends CrossLayerTestBase {

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserConvertor userConvertor() {
            // 本测试只关注凭据查询是否被缓存截断，转换器行为无关，用 mock 避免依赖生成的实现类
            return mock(UserConvertor.class);
        }

        @Bean
        UserService userService(UserConvertor userConvertor) {
            return new UserService(userConvertor);
        }
    }

    @MockitoBean
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("同一凭证连续查询两次都必须拿到带密码哈希的实体，且不写入用户缓存")
    void credentialLookupKeepsPasswordHashOnRepeat() {
        String username = "cache-probe-" + UUID.randomUUID().toString().substring(0, 8);
        User stored = User.builder()
                .id(9001L)
                .username(username)
                .password("$2a$10$0123456789012345678901234567890123456789012345678901")
                .userType(UserType.STUDENT)
                .universityId("1001")
                .build();
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(stored);

        User first = userService.getByPassport(username);
        User second = userService.getByPassport(username);

        assertThat(first).isNotNull();
        assertThat(first.getPassword()).as("首次查询应返回密码哈希").isNotNull();
        assertThat(second).as("二次查询必须仍返回带密码哈希的实体").isNotNull();
        assertThat(second.getPassword())
                .as("二次查询若命中缓存，password 会因 WRITE_ONLY 序列化丢失，导致登录误判密码错误")
                .isNotNull();
        verify(userMapper, times(2)).selectOne(any(), anyBoolean());
        assertThat(redisTemplate.hasKey(RedisConstants.USER_VO_CACHE_PREFIX + username))
                .as("凭据查询不得把用户实体写入缓存")
                .isFalse();
    }
}
