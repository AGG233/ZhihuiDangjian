package com.rauio.smartdangjian.server.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceMetaService extends ServiceImpl<ResourceMetaMapper, ResourceMeta> {

    public ResourceMeta create(ResourceMetaCreateRequest request) {
        validateDuplicate(null, request.getHash(), request.getObjectKey());
        ResourceMeta meta = ResourceMeta.builder()
                .uploaderId(request.getUploaderId())
                .originalName(request.getOriginalName())
                .hash(request.getHash())
                .objectKey(request.getObjectKey())
                .resourceType(request.getResourceType())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .build();
        if (!this.save(meta)) {
            throw new BusinessException(4000, "创建资源失败");
        }
        return meta;
    }

    public ResourceMeta get(String id) {
        ResourceMeta meta = this.getById(id);
        if (meta == null) {
            throw new BusinessException(4000, "资源不存在");
        }
        return meta;
    }

    @Cacheable(value = "resourceMeta",key = "#hash")
    public ResourceMeta getByHash(String hash) {
        ResourceMeta meta = this.getOne(new LambdaQueryWrapper<ResourceMeta>()
                .eq(ResourceMeta::getHash, hash)
                .last("limit 1"));
        if (meta == null) {
            throw new BusinessException(4000, "资源不存在");
        }
        return meta;
    }

    public boolean existsByHash(String hash) {
        return this.exists(new LambdaQueryWrapper<ResourceMeta>()
                .eq(ResourceMeta::getHash, hash));
    }

    public List<ResourceMeta> list(String uploaderId, String originalName, String hash, String resourceType, Integer status) {
        LambdaQueryWrapper<ResourceMeta> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(uploaderId), ResourceMeta::getUploaderId, uploaderId)
                .like(StringUtils.isNotBlank(originalName), ResourceMeta::getOriginalName, originalName)
                .eq(StringUtils.isNotBlank(hash), ResourceMeta::getHash, hash)
                .eq(StringUtils.isNotBlank(resourceType), ResourceMeta::getResourceType, resourceType)
                .eq(status != null, ResourceMeta::getStatus, status)
                .orderByDesc(ResourceMeta::getId);
        return this.list(wrapper);
    }

    public Boolean update(String id, ResourceMetaUpdateRequest request) {
        ResourceMeta existing = this.get(id);
        validateDuplicate(id, existing.getHash(), existing.getObjectKey());

        ResourceMeta meta = ResourceMeta.builder()
                .id(id)
                .uploaderId(existing.getUploaderId())
                .hash(existing.getHash())
                .objectKey(StringUtils.isNotBlank(request.getObjectKey()) ? request.getObjectKey() : existing.getObjectKey())
                .originalName(StringUtils.isNotBlank(request.getOriginalName()) ? request.getOriginalName() : existing.getOriginalName())
                .resourceType(StringUtils.isNotBlank(request.getResourceType()) ? request.getResourceType() : existing.getResourceType())
                .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                .build();

        if (!this.updateById(meta)) {
            throw new BusinessException(4000, "更新资源失败");
        }
        return true;
    }

    public Boolean delete(String id) {
        this.get(id);

        if (!this.removeById(id)) {
            throw new BusinessException(4000, "删除资源失败");
        }
        return true;
    }

    public Boolean deleteByHash(String hash) {
        ResourceMeta meta = this.getOne(new LambdaQueryWrapper<ResourceMeta>()
                .eq(ResourceMeta::getHash, hash)
                .last("limit 1"));
        if (meta == null) {
            throw new BusinessException(4000, "资源不存在");
        }
        return delete(meta.getId());
    }

    public Boolean deleteByHashes(List<String> hashes) {
        for (String hash : hashes) {
            deleteByHash(hash);
        }
        return true;
    }

    private void validateDuplicate(String currentId, String hash, String objectKey) {
        ResourceMeta sameHash = this.getOne(new LambdaQueryWrapper<ResourceMeta>()
                .eq(StringUtils.isNotBlank(hash), ResourceMeta::getHash, hash)
                .last("limit 1"));
        if (sameHash != null && !sameHash.getId().equals(currentId)) {
            throw new BusinessException(4000, "资源哈希已存在");
        }

        ResourceMeta sameObjectKey = this.getOne(new LambdaQueryWrapper<ResourceMeta>()
                .eq(StringUtils.isNotBlank(objectKey), ResourceMeta::getObjectKey, objectKey)
                .last("limit 1"));
        if (sameObjectKey != null && !sameObjectKey.getId().equals(currentId)) {
            throw new BusinessException(4000, "对象存储键已存在");
        }
    }
}
