package com.rauio.smartdangjian.server.resource.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.constants.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;

@ExtendWith(MockitoExtension.class)
class ResourceMetaServiceTest {

    @Mock
    private ResourceMetaMapper mapper;

    @Spy
    @InjectMocks
    private ResourceMetaService resourceMetaService;

    private static final Long RESOURCE_ID = 1L;
    private static final String HASH = "abc123";
    private static final String OBJECT_KEY = "image/abc123.png";

    // ==================== create ====================

    @Test
    @DisplayName("create 创建资源成功")
    void createSuccess() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("1");
        request.setOriginalName("test.png");
        request.setHash(HASH);
        request.setObjectKey(OBJECT_KEY);
        request.setResourceType(0);

        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(resourceMetaService).save(any(ResourceMeta.class));

        ResourceMeta result = resourceMetaService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getHash()).isEqualTo(HASH);
        assertThat(result.getStatus()).isEqualTo(ResourceStatusConstants.PUBLIC);
    }

    @Test
    @DisplayName("create 指定状态时不覆盖")
    void createWithStatus() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("1");
        request.setOriginalName("test.png");
        request.setHash(HASH);
        request.setObjectKey(OBJECT_KEY);
        request.setResourceType(0);
        request.setStatus(ResourceStatusConstants.UPLOADING);

        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(resourceMetaService).save(any(ResourceMeta.class));

        ResourceMeta result = resourceMetaService.create(request);

        assertThat(result.getStatus()).isEqualTo(ResourceStatusConstants.UPLOADING);
    }

    @Test
    @DisplayName("create 哈希重复抛出异常")
    void createDuplicateHash() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("1");
        request.setOriginalName("test.png");
        request.setHash(HASH);
        request.setObjectKey(OBJECT_KEY);
        request.setResourceType(0);

        doReturn(ResourceMeta.builder().id(1L).hash(HASH).build())
                .when(resourceMetaService)
                .getOne(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> resourceMetaService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源哈希已存在");
    }

    @Test
    @DisplayName("create 保存失败抛出异常")
    void createSaveFailed() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("1");
        request.setOriginalName("test.png");
        request.setHash(HASH);
        request.setObjectKey(OBJECT_KEY);
        request.setResourceType(0);

        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));
        doReturn(false).when(resourceMetaService).save(any(ResourceMeta.class));

        assertThatThrownBy(() -> resourceMetaService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("创建资源失败");
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 根据ID获取资源成功")
    void getSuccess() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).hash(HASH).build())
                .when(resourceMetaService)
                .getById(RESOURCE_ID);

        ResourceMeta result = resourceMetaService.get(RESOURCE_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(RESOURCE_ID);
    }

    @Test
    @DisplayName("get 资源不存在抛出异常")
    void getNotFound() {
        doReturn(null).when(resourceMetaService).getById(RESOURCE_ID);

        assertThatThrownBy(() -> resourceMetaService.get(RESOURCE_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源不存在");
    }

    // ==================== getByHash ====================

    @Test
    @DisplayName("getByHash 根据哈希获取资源成功")
    void getByHashSuccess() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).hash(HASH).build())
                .when(resourceMetaService)
                .getOne(any(LambdaQueryWrapper.class));

        ResourceMeta result = resourceMetaService.getByHash(HASH);

        assertThat(result).isNotNull();
        assertThat(result.getHash()).isEqualTo(HASH);
    }

    @Test
    @DisplayName("getByHash 资源不存在抛出异常")
    void getByHashNotFound() {
        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> resourceMetaService.getByHash(HASH))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源不存在");
    }

    // ==================== existsByHash ====================

    @Test
    @DisplayName("existsByHash 返回是否存在")
    void existsByHash() {
        doReturn(true).when(resourceMetaService).exists(any(LambdaQueryWrapper.class));
        assertThat(resourceMetaService.existsByHash(HASH)).isTrue();

        doReturn(false).when(resourceMetaService).exists(any(LambdaQueryWrapper.class));
        assertThat(resourceMetaService.existsByHash(HASH)).isFalse();
    }

    // ==================== list ====================

    @Test
    @DisplayName("list 按条件查询资源列表")
    void listWithFilters() {
        List<ResourceMeta> list = List.of(ResourceMeta.builder().id(RESOURCE_ID).build());
        doReturn(list).when(resourceMetaService).list(any(LambdaQueryWrapper.class));

        List<ResourceMeta> result = resourceMetaService.list(1L, null, null, null, null);

        assertThat(result).hasSize(1);
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新资源成功")
    void updateSuccess() {
        ResourceMeta existing = ResourceMeta.builder()
                .id(RESOURCE_ID)
                .uploaderId(1L)
                .hash(HASH)
                .objectKey(OBJECT_KEY)
                .originalName("old.png")
                .resourceType(0)
                .status(1)
                .build();
        doReturn(existing).when(resourceMetaService).getById(RESOURCE_ID);
        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));

        ResourceMetaUpdateRequest request = new ResourceMetaUpdateRequest();
        request.setOriginalName("new.png");
        request.setStatus(2);

        doReturn(true).when(resourceMetaService).updateById(any(ResourceMeta.class));

        resourceMetaService.update(RESOURCE_ID, request);
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除资源成功")
    void deleteSuccess() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).build())
                .when(resourceMetaService)
                .getById(RESOURCE_ID);
        doReturn(true).when(resourceMetaService).removeById(RESOURCE_ID);

        resourceMetaService.delete(RESOURCE_ID);
    }

    @Test
    @DisplayName("delete 删除失败抛出异常")
    void deleteFailed() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).build())
                .when(resourceMetaService)
                .getById(RESOURCE_ID);
        doReturn(false).when(resourceMetaService).removeById(RESOURCE_ID);

        assertThatThrownBy(() -> resourceMetaService.delete(RESOURCE_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("删除资源失败");
    }

    // ==================== deleteByHash ====================

    @Test
    @DisplayName("deleteByHash 按哈希删除资源成功")
    void deleteByHashSuccess() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).hash(HASH).build())
                .when(resourceMetaService)
                .getOne(any(LambdaQueryWrapper.class));
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).build())
                .when(resourceMetaService)
                .getById(RESOURCE_ID);
        doReturn(true).when(resourceMetaService).removeById(RESOURCE_ID);

        resourceMetaService.deleteByHash(HASH);
    }

    // ==================== deleteByHashes ====================

    @Test
    @DisplayName("deleteByHashes 批量按哈希删除")
    void deleteByHashes() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).hash(HASH).build())
                .when(resourceMetaService)
                .getOne(any(LambdaQueryWrapper.class));
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).build())
                .when(resourceMetaService)
                .getById(RESOURCE_ID);
        doReturn(true).when(resourceMetaService).removeById(RESOURCE_ID);

        resourceMetaService.deleteByHashes(List.of(HASH));
    }

    // ==================== create - objectKey duplicate ====================

    @Test
    @DisplayName("create 对象存储键重复时抛出异常")
    void createDuplicateObjectKey() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("1");
        request.setOriginalName("test.png");
        request.setHash("unique-hash");
        request.setObjectKey(OBJECT_KEY);
        request.setResourceType(0);

        doReturn(null, ResourceMeta.builder().id(99L).objectKey(OBJECT_KEY).build())
                .when(resourceMetaService)
                .getOne(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> resourceMetaService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("对象存储键已存在");
    }

    // ==================== update - all fields ====================

    @Test
    @DisplayName("update 更新所有字段成功")
    void updateWithAllFields() {
        ResourceMeta existing = ResourceMeta.builder()
                .id(RESOURCE_ID)
                .uploaderId(1L)
                .hash(HASH)
                .objectKey("old/key.png")
                .originalName("old.png")
                .resourceType(0)
                .status(1)
                .build();
        doReturn(existing).when(resourceMetaService).getById(RESOURCE_ID);
        doReturn(existing).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));

        ResourceMetaUpdateRequest request = new ResourceMetaUpdateRequest();
        request.setObjectKey("new/key.png");
        request.setOriginalName("new.png");
        request.setResourceType(1);
        request.setStatus(2);

        doReturn(true).when(resourceMetaService).updateById(any(ResourceMeta.class));

        resourceMetaService.update(RESOURCE_ID, request);
    }

    @Test
    @DisplayName("update 将 objectKey 改为已存在值时抛出异常")
    void updateDuplicateObjectKeyThrows() {
        ResourceMeta existing = ResourceMeta.builder()
                .id(RESOURCE_ID)
                .uploaderId(1L)
                .hash(HASH)
                .objectKey(OBJECT_KEY)
                .originalName("old.png")
                .resourceType(0)
                .status(1)
                .build();
        doReturn(existing).when(resourceMetaService).getById(RESOURCE_ID);
        ResourceMeta other =
                ResourceMeta.builder().id(99L).objectKey("dup/key.png").build();
        // 第一次 getOne 查 hash 无冲突，第二次查新 objectKey 命中其他资源
        doReturn(null, other).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));

        ResourceMetaUpdateRequest request = new ResourceMetaUpdateRequest();
        request.setObjectKey("dup/key.png");

        assertThatThrownBy(() -> resourceMetaService.update(RESOURCE_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("对象存储键已存在");
    }

    // ==================== update - failed ====================

    @Test
    @DisplayName("update 更新数据库失败时抛出异常")
    void updateFailed() {
        ResourceMeta existing = ResourceMeta.builder()
                .id(RESOURCE_ID)
                .uploaderId(1L)
                .hash(HASH)
                .objectKey(OBJECT_KEY)
                .originalName("old.png")
                .resourceType(0)
                .status(1)
                .build();
        doReturn(existing).when(resourceMetaService).getById(RESOURCE_ID);
        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));

        ResourceMetaUpdateRequest request = new ResourceMetaUpdateRequest();
        request.setOriginalName("new.png");

        doReturn(false).when(resourceMetaService).updateById(any(ResourceMeta.class));

        assertThatThrownBy(() -> resourceMetaService.update(RESOURCE_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("更新资源失败");
    }

    // ==================== deleteByHash - not found ====================

    @Test
    @DisplayName("deleteByHash 资源不存在抛出异常")
    void deleteByHashNotFound() {
        doReturn(null).when(resourceMetaService).getOne(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> resourceMetaService.deleteByHash(HASH))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源不存在");
    }

    // ==================== deleteByHash - failed ====================

    @Test
    @DisplayName("deleteByHash 删除数据库失败时抛出异常")
    void deleteByHashFailed() {
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).hash(HASH).build())
                .when(resourceMetaService)
                .getOne(any(LambdaQueryWrapper.class));
        doReturn(ResourceMeta.builder().id(RESOURCE_ID).build())
                .when(resourceMetaService)
                .getById(RESOURCE_ID);
        doReturn(false).when(resourceMetaService).removeById(RESOURCE_ID);

        assertThatThrownBy(() -> resourceMetaService.deleteByHash(HASH))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("删除资源失败");
    }
}
