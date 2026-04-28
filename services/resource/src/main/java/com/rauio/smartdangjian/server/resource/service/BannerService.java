package com.rauio.smartdangjian.server.resource.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.rauio.smartdangjian.server.resource.Constant.ResourceConstant.BANNER_MAX_SIZE;
import static com.rauio.smartdangjian.server.resource.Constant.ResourceConstant.BANNER_PREFIX;

@Service
@RequiredArgsConstructor
public class BannerService {

    private static final int BANNER_LIST_START = 0;
    private static final int BANNER_LIST_END = -1;

    private final RedisTemplate<String, Object> redisTemplate;
    private final FileService fileService;
    private final ResourceMetaService resourceMetaService;

    public List<ResourceMeta> getList() {
        List<Object> hashList = redisTemplate.opsForList().range(BANNER_PREFIX, BANNER_LIST_START, BANNER_LIST_END);
        if (hashList == null || hashList.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResourceMeta> banners = new ArrayList<>(hashList.size());
        for (Object item : hashList) {
            if (!(item instanceof String hash) || !StringUtils.isNotBlank(hash)) {
                continue;
            }
            try {
                banners.add(resourceMetaService.getByHash(hash));
            } catch (BusinessException ignored) {
                // Skip stale banner items whose resource metadata has been removed.
            }
        }
        return banners;
    }

    public ResourceMeta get(int order) {
        validateOrder(order);
        Object hash = redisTemplate.opsForList().index(BANNER_PREFIX, order);
        if (!(hash instanceof String bannerHash) || !StringUtils.isNotBlank(bannerHash)) {
            throw new BusinessException(4000, "轮播图不存在");
        }
        return resourceMetaService.getByHash(bannerHash);
    }

    public ResourceMeta get(String hash) {
        if (!contains(hash)) {
            return null;
        }
        return resourceMetaService.getByHash(hash);
    }

    public List<BannerResourceResponse> getUserList() {
        List<ResourceMeta> banners = getList();
        if (banners.isEmpty()) {
            return Collections.emptyList();
        }
        List<BannerResourceResponse> result = new ArrayList<>(banners.size());
        for (int i = 0; i < banners.size(); i++) {
            result.add(toUserResponse(i, banners.get(i)));
        }
        return result;
    }

    public BannerResourceResponse getUser(int order) {
        return toUserResponse(order, get(order));
    }

    public ResourceMeta createByResourceId(String resourceId) {
        return create(resourceId);
    }

    public ResourceMeta create(String resourceId) {
        ResourceMeta meta = resourceMetaService.get(resourceId);
        appendBanner(meta.getHash());
        return meta;
    }

    public ResourceMeta createByHash(String hash) {
        ResourceMeta meta = resourceMetaService.getByHash(hash);
        appendBanner(meta.getHash());
        return meta;
    }

    public ResourceMeta update(int order, String resourceId) {
        validateOrder(order);
        ResourceMeta meta = resourceMetaService.get(resourceId);
        replaceBanner(order, meta.getHash());
        return meta;
    }

    public ResourceMeta updateByHash(int order, String hash) {
        validateOrder(order);
        ResourceMeta meta = resourceMetaService.getByHash(hash);
        replaceBanner(order, meta.getHash());
        return meta;
    }

    public boolean delete(int order) {
        validateOrder(order);
        redisTemplate.opsForList().remove(BANNER_PREFIX, 1, redisTemplate.opsForList().index(BANNER_PREFIX, order));
        return true;
    }

    private void appendBanner(String hash) {
        if (!StringUtils.isNotBlank(hash)) {
            throw new BusinessException(4000, "轮播图资源不能为空");
        }
        Long size = redisTemplate.opsForList().size(BANNER_PREFIX);
        if (size != null && size >= BANNER_MAX_SIZE) {
            throw new BusinessException(4000, "轮播图数量已达上限");
        }
        if (contains(hash)) {
            throw new BusinessException(4000, "该资源已存在于轮播图中");
        }
        Long result = redisTemplate.opsForList().rightPush(BANNER_PREFIX, hash);
        if (result == null) {
            throw new BusinessException(4000, "轮播图创建失败");
        }
    }

    private void replaceBanner(int order, String hash) {
        if (!StringUtils.isNotBlank(hash)) {
            throw new BusinessException(4000, "轮播图资源不能为空");
        }
        Object current = redisTemplate.opsForList().index(BANNER_PREFIX, order);
        if (hash.equals(current)) {
            return;
        }
        if (contains(hash)) {
            throw new BusinessException(4000, "该资源已存在于轮播图中");
        }
        redisTemplate.opsForList().set(BANNER_PREFIX, order, hash);
    }

    private boolean contains(String hash) {
        List<Object> hashList = redisTemplate.opsForList().range(BANNER_PREFIX, BANNER_LIST_START, BANNER_LIST_END);
        if (hashList == null || hashList.isEmpty()) {
            return false;
        }
        return hashList.stream().anyMatch(item -> hash.equals(item));
    }

    private void validateOrder(int order) {
        Long size = redisTemplate.opsForList().size(BANNER_PREFIX);
        if (size == null || order < 0 || order >= size) {
            throw new BusinessException(4000, "轮播图不存在");
        }
    }

    private BannerResourceResponse toUserResponse(int order, ResourceMeta meta) {
        String downloadUrl = fileService.getDownloadUrl(meta.getId());
        return new BannerResourceResponse(
                order,
                meta.getId(),
                meta.getOriginalName(),
                meta.getHash(),
                meta.getObjectKey(),
                meta.getResourceType(),
                meta.getStatus(),
                downloadUrl
        );
    }
}
