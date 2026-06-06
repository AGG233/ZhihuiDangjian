package com.rauio.smartdangjian.server.resource.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.constants.ResourceErrorConstants;
import com.rauio.smartdangjian.server.resource.constants.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;
import com.rauio.smartdangjian.service.PermissionValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceMetaService extends ServiceImpl<ResourceMetaMapper, ResourceMeta> {

    private final PermissionValidator permissionValidator;

    @Transactional(rollbackFor = Exception.class)
    public ResourceMeta create(ResourceMetaCreateRequest request) {
        validateDuplicate(null, request.getHash(), request.getObjectKey());
        ResourceMeta meta = ResourceMeta.builder()
                .uploaderId(IdUtil.parse(request.getUploaderId()))
                .originalName(request.getOriginalName())
                .hash(request.getHash())
                .objectKey(request.getObjectKey())
                .resourceType(request.getResourceType())
                .status(request.getStatus() != null ? request.getStatus() : ResourceStatusConstants.PUBLIC)
                .build();
        if (!this.save(meta)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_CREATE_FAILED, "创建资源失败");
        }
        return meta;
    }

    @Transactional(readOnly = true)
    public ResourceMeta get(Long id) {
        ResourceMeta meta = this.getById(id);
        if (meta == null) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_NOT_FOUND, "资源不存在");
        }
        return meta;
    }

    @Transactional(readOnly = true)
    public ResourceMeta getByHash(String hash) {
        ResourceMeta meta = this.getOne(new LambdaQueryWrapper<ResourceMeta>().eq(ResourceMeta::getHash, hash));
        if (meta == null) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_NOT_FOUND, "资源不存在");
        }
        return meta;
    }

    @Transactional(readOnly = true)
    public boolean existsByHash(String hash) {
        return this.exists(new LambdaQueryWrapper<ResourceMeta>().eq(ResourceMeta::getHash, hash));
    }

    @Transactional(readOnly = true)
    public List<ResourceMeta> list(
            Long uploaderId, String originalName, String hash, Integer resourceType, Integer status) {
        LambdaQueryWrapper<ResourceMeta> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(uploaderId != null, ResourceMeta::getUploaderId, uploaderId)
                .like(StringUtils.isNotBlank(originalName), ResourceMeta::getOriginalName, originalName)
                .eq(StringUtils.isNotBlank(hash), ResourceMeta::getHash, hash)
                .eq(resourceType != null, ResourceMeta::getResourceType, resourceType)
                .eq(status != null, ResourceMeta::getStatus, status)
                .orderByDesc(ResourceMeta::getId);
        return this.list(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long id, ResourceMetaUpdateRequest request) {
        ResourceMeta existing = this.get(id);
        permissionValidator.requireResourceAccess(existing.getUploaderId());
        validateDuplicate(id, existing.getHash(), existing.getObjectKey());

        ResourceMeta meta = ResourceMeta.builder()
                .id(id)
                .uploaderId(existing.getUploaderId())
                .hash(existing.getHash())
                .objectKey(
                        StringUtils.isNotBlank(request.getObjectKey())
                                ? request.getObjectKey()
                                : existing.getObjectKey())
                .originalName(
                        StringUtils.isNotBlank(request.getOriginalName())
                                ? request.getOriginalName()
                                : existing.getOriginalName())
                .resourceType(
                        request.getResourceType() != null ? request.getResourceType() : existing.getResourceType())
                .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                .build();

        if (!this.updateById(meta)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_UPDATE_FAILED, "更新资源失败");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        ResourceMeta meta = this.get(id);
        permissionValidator.requireResourceAccess(meta.getUploaderId());

        if (!this.removeById(id)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_DELETE_FAILED, "删除资源失败");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByHash(String hash) {
        ResourceMeta meta = this.getOne(new LambdaQueryWrapper<ResourceMeta>().eq(ResourceMeta::getHash, hash));
        if (meta == null) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_NOT_FOUND, "资源不存在");
        }
        permissionValidator.requireResourceAccess(meta.getUploaderId());
        if (!this.removeById(meta.getId())) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_DELETE_FAILED, "删除资源失败");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByHashes(List<String> hashes) {
        for (String hash : hashes) {
            deleteByHash(hash);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean markPublic(Long id) {
        ResourceMeta existing = this.get(id);
        existing.setStatus(ResourceStatusConstants.PUBLIC);
        if (!this.updateById(existing)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_UPDATE_FAILED, "更新资源失败");
        }
        return true;
    }

    private void validateDuplicate(Long currentId, String hash, String objectKey) {
        ResourceMeta sameHash = this.getOne(
                new LambdaQueryWrapper<ResourceMeta>().eq(StringUtils.isNotBlank(hash), ResourceMeta::getHash, hash));
        if (sameHash != null && !sameHash.getId().equals(currentId)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_HASH_EXISTS, "资源哈希已存在");
        }

        ResourceMeta sameObjectKey = this.getOne(new LambdaQueryWrapper<ResourceMeta>()
                .eq(StringUtils.isNotBlank(objectKey), ResourceMeta::getObjectKey, objectKey));
        if (sameObjectKey != null && !sameObjectKey.getId().equals(currentId)) {
            throw new BusinessException(ResourceErrorConstants.RESOURCE_OBJECT_KEY_EXISTS, "对象存储键已存在");
        }
    }
}
