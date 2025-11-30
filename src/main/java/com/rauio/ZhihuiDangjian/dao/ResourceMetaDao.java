package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.ResourceMetaMapper;
import com.rauio.ZhihuiDangjian.pojo.ResourceMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ResourceMetaDao {

    private final ResourceMetaMapper resourceMetaMapper;

    public ResourceMeta findByHash(String hash) {
        QueryWrapper<ResourceMeta> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("hash", hash);

        return resourceMetaMapper.selectOne(queryWrapper);
    }

    public ResourceMeta findByResourceId(String resourceId) {
        QueryWrapper<ResourceMeta> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        return resourceMetaMapper.selectOne(queryWrapper);
    }

    public Boolean update(ResourceMeta resourceMeta) {
        return resourceMetaMapper.updateById(resourceMeta) > 0;
    }

    public Boolean save(ResourceMeta resourceMeta) {
        QueryWrapper<ResourceMeta> wrapper = new QueryWrapper<>();
        wrapper.eq("hash", resourceMeta.getHash());
        if (resourceMetaMapper.selectOne(wrapper) != null) {
            return true;
        }
        return resourceMetaMapper.insert(resourceMeta) > 0;
    }

    public Boolean delete(String resourceId) {
        return resourceMetaMapper.deleteById(resourceId) > 0;
    }
}