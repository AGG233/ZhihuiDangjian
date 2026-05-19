package com.rauio.smartdangjian.server.resource.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.Constant.ResourceConstant;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private FileService fileService;

    @Mock
    private ResourceMetaService resourceMetaService;

    @SuppressWarnings("rawtypes")
    @Mock
    private ListOperations listOperations;

    @InjectMocks
    private BannerService bannerService;

    private static final String HASH = "abc123";
    private static final String HASH2 = "def456";
    private static final String DOWNLOAD_URL = "https://example.com/download";

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getList 返回轮播图列表")
    void getList() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of(HASH, HASH2));

        ResourceMeta meta1 = ResourceMeta.builder()
                .id("r-1")
                .hash(HASH)
                .originalName("b1.png")
                .build();
        ResourceMeta meta2 = ResourceMeta.builder()
                .id("r-2")
                .hash(HASH2)
                .originalName("b2.png")
                .build();
        when(resourceMetaService.getByHash(HASH)).thenReturn(meta1);
        when(resourceMetaService.getByHash(HASH2)).thenReturn(meta2);

        List<ResourceMeta> result = bannerService.getList();

        assertThat(result).hasSize(2);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getList 跳过异常的轮播项")
    void getListSkipsErrors() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of(HASH, "invalid"));

        when(resourceMetaService.getByHash(HASH))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());
        when(resourceMetaService.getByHash("invalid")).thenThrow(new BusinessException(5002, "资源不存在"));

        List<ResourceMeta> result = bannerService.getList();

        assertThat(result).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getList Redis 返回空时返回空列表")
    void getListEmpty() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(anyString(), anyLong(), anyLong())).thenReturn(List.of());

        List<ResourceMeta> result = bannerService.getList();

        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("get 获取指定顺序轮播图")
    void getByOrder() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(5L);
        when(listOperations.index(ResourceConstant.BANNER_PREFIX, 0)).thenReturn(HASH);
        when(resourceMetaService.getByHash(HASH))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());

        ResourceMeta result = bannerService.get(0);

        assertThat(result).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("get 顺序无效抛出异常")
    void getInvalidOrder() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(3L);

        assertThatThrownBy(() -> bannerService.get(5))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("轮播图不存在");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("get 根据哈希获取")
    void getByHash() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of(HASH));
        when(resourceMetaService.getByHash(HASH))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());

        ResourceMeta result = bannerService.get(HASH);

        assertThat(result).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("get 哈希不在轮播中返回null")
    void getByHashNotFound() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of(HASH));

        ResourceMeta result = bannerService.get("nonexistent");

        assertThat(result).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getUserList 返回用户侧轮播列表")
    void getUserList() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of(HASH));
        when(resourceMetaService.getByHash(HASH))
                .thenReturn(ResourceMeta.builder()
                        .id("r-1")
                        .hash(HASH)
                        .originalName("b.png")
                        .objectKey("image/b.png")
                        .resourceType(0)
                        .status(1)
                        .build());
        when(fileService.getDownloadUrl("r-1")).thenReturn(DOWNLOAD_URL);

        List<BannerResourceResponse> result = bannerService.getUserList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).downloadUrl()).isEqualTo(DOWNLOAD_URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getUser 获取单个用户侧轮播")
    void getUser() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(5L);
        when(listOperations.index(ResourceConstant.BANNER_PREFIX, 0)).thenReturn(HASH);
        when(resourceMetaService.getByHash(HASH))
                .thenReturn(ResourceMeta.builder()
                        .id("r-1")
                        .hash(HASH)
                        .originalName("b.png")
                        .build());
        when(fileService.getDownloadUrl("r-1")).thenReturn(DOWNLOAD_URL);

        BannerResourceResponse result = bannerService.getUser(0);

        assertThat(result).isNotNull();
        assertThat(result.downloadUrl()).isEqualTo(DOWNLOAD_URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("create 基于资源ID添加轮播图")
    void createByResourceId() {
        ResourceMeta meta = ResourceMeta.builder().id("r-1").hash(HASH).build();
        when(resourceMetaService.get("r-1")).thenReturn(meta);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(0L);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of());
        when(listOperations.rightPush(ResourceConstant.BANNER_PREFIX, HASH)).thenReturn(1L);

        ResourceMeta result = bannerService.create("r-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("r-1");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("create 基于哈希添加轮播图")
    void createByHash() {
        when(resourceMetaService.getByHash(HASH))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(0L);
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of());
        when(listOperations.rightPush(ResourceConstant.BANNER_PREFIX, HASH)).thenReturn(1L);

        ResourceMeta result = bannerService.createByHash(HASH);

        assertThat(result).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("create 轮播图已达上限抛出异常")
    void createMaxSize() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(100L);
        when(resourceMetaService.get("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());

        assertThatThrownBy(() -> bannerService.create("r-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("数量已达上限");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("create 资源已存在抛出异常")
    void createAlreadyExists() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(5L);
        when(resourceMetaService.get("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());

        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of("other", HASH));

        assertThatThrownBy(() -> bannerService.create("r-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该资源已存在于轮播图中");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("update 更新轮播图")
    void update() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(5L);

        when(resourceMetaService.get("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());
        when(listOperations.index(ResourceConstant.BANNER_PREFIX, 2)).thenReturn("old-hash");
        when(listOperations.range(ResourceConstant.BANNER_PREFIX, 0, -1)).thenReturn(List.of("h1", "h2"));

        ResourceMeta result = bannerService.update(2, "r-1");

        assertThat(result).isNotNull();
        verify(listOperations).set(ResourceConstant.BANNER_PREFIX, 2, HASH);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("update 相同哈希不重复设置")
    void updateSameHash() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(5L);
        when(resourceMetaService.get("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").hash(HASH).build());
        when(listOperations.index(ResourceConstant.BANNER_PREFIX, 0)).thenReturn(HASH);

        bannerService.update(0, "r-1");

        verify(listOperations, never()).set(anyString(), anyLong(), anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("delete 删除轮播图")
    void delete() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(ResourceConstant.BANNER_PREFIX)).thenReturn(5L);
        when(listOperations.index(ResourceConstant.BANNER_PREFIX, 3)).thenReturn("hash-to-remove");

        Boolean result = bannerService.delete(3);

        assertThat(result).isTrue();
        verify(listOperations).remove(ResourceConstant.BANNER_PREFIX, 1, "hash-to-remove");
    }
}
