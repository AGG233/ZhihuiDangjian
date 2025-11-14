package com.rauio.ZhihuiDangjiang.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjiang.mapper.ResourceMetaMapper;
import com.rauio.ZhihuiDangjiang.pojo.ResourceMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceMetaDao {

    private final ResourceMetaMapper resourceMetaMapper;

    @Autowired
    public ResourceMetaDao(ResourceMetaMapper resourceMetaMapper) {
        this.resourceMetaMapper = resourceMetaMapper;
    }

    public ResourceMeta findByHash(String hash) {
        QueryWrapper<ResourceMeta> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("hash", hash);

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