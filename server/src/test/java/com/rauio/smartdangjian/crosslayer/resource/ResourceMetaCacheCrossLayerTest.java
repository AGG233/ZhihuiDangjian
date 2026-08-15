package com.rauio.smartdangjian.crosslayer.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.resource.constants.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;

/**
 * 资源元数据缓存一致性跨层回归：真实 ResourceMetaService（含 @Cacheable/@CacheEvict 代理）+ 真实 Redis 缓存，
 * 验证写操作（updateStatus/delete）后 resourceMeta 缓存被驱逐（P0 修复 #6）。
 */
@SpringBootTest(classes = ResourceMetaCacheCrossLayerTest.TestConfig.class)
class ResourceMetaCacheCrossLayerTest extends CrossLayerTestBase {

    @Autowired
    private ResourceMetaService resourceMetaService;

    @Autowired
    private ResourceMetaMapper resourceMetaMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String uniqueHash =
            "hash-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ResourceMetaMapper resourceMetaMapper() {
            return mock(ResourceMetaMapper.class);
        }

        @Bean
        ResourceMetaService resourceMetaService(ResourceMetaMapper resourceMetaMapper) {
            ResourceMetaService service = new ResourceMetaService();
            try {
                Field field = findBaseMapperField(ResourceMetaService.class);
                field.setAccessible(true);
                field.set(service, resourceMetaMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ResourceMetaService", e);
            }
            return service;
        }

        private static Field findBaseMapperField(Class<?> clazz) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField("baseMapper");
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException("baseMapper");
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }

    @AfterEach
    void cleanUpRedisKeys() {
        Set<String> keys = redisTemplate.keys("resourceMeta::" + uniqueHash);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("getByHash 写入缓存，updateStatus 后缓存被驱逐")
    void updateStatusEvictsHashCache() {
        ResourceMeta meta = ResourceMeta.builder()
                .id(1L)
                .hash(uniqueHash)
                .objectKey("image/a.png")
                .status(ResourceStatusConstants.UPLOADING)
                .build();
        // BaseMapper.selectOne 是 default 方法（Mockito 不执行其真实实现），
        // 必须用 doReturn 语法 stub（when 语法会先执行 default 方法拿到 null）
        doReturn(meta).when(resourceMetaMapper).selectOne(any(), anyBoolean());
        when(resourceMetaMapper.selectById(1L)).thenReturn(meta);
        when(resourceMetaMapper.updateById(any(ResourceMeta.class))).thenReturn(1);

        // 首次按 hash 查询写入缓存
        resourceMetaService.getByHash(uniqueHash);
        assertThat(redisTemplate.hasKey("resourceMeta::" + uniqueHash)).isTrue();

        // 状态变更触发 @CacheEvict(allEntries = true)
        resourceMetaService.updateStatus(1L, ResourceStatusConstants.PUBLIC);
        assertThat(redisTemplate.hasKey("resourceMeta::" + uniqueHash)).isFalse();
    }

    @Test
    @DisplayName("getByHash 写入缓存，delete 后缓存被驱逐")
    void deleteEvictsHashCache() {
        ResourceMeta meta = ResourceMeta.builder()
                .id(1L)
                .hash(uniqueHash)
                .objectKey("image/a.png")
                .status(ResourceStatusConstants.PUBLIC)
                .build();
        // BaseMapper.selectOne 是 default 方法（Mockito 不执行其真实实现），
        // 必须用 doReturn 语法 stub（when 语法会先执行 default 方法拿到 null）
        doReturn(meta).when(resourceMetaMapper).selectOne(any(), anyBoolean());
        when(resourceMetaMapper.selectById(1L)).thenReturn(meta);
        when(resourceMetaMapper.deleteById(1L)).thenReturn(1);

        resourceMetaService.getByHash(uniqueHash);
        assertThat(redisTemplate.hasKey("resourceMeta::" + uniqueHash)).isTrue();

        resourceMetaService.delete(1L);
        assertThat(redisTemplate.hasKey("resourceMeta::" + uniqueHash)).isFalse();
    }
}
