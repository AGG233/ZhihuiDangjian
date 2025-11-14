package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.ContentBlockMapper;
import com.rauio.ZhihuiDangjian.pojo.ContentBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContentBlockDao {

    private final ContentBlockMapper contentBlockMapper;

    public ContentBlock get(String id) {
        return contentBlockMapper.selectById(id);
    }

    public ContentBlock getByResourceId(String resourceId) {
        QueryWrapper<ContentBlock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        return contentBlockMapper.selectOne(queryWrapper);
    }

    public List<ContentBlock> getAllByParentId(String parentId) {
        QueryWrapper<ContentBlock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        return contentBlockMapper.selectList(queryWrapper);
    }

    public Boolean update(ContentBlock contentBlock) {
        return contentBlockMapper.updateById(contentBlock) > 0;
    }

    public Boolean insert(ContentBlock contentBlock) {
        return contentBlockMapper.insert(contentBlock) > 0;
    }

    public Boolean delete(String id) {
        return contentBlockMapper.deleteById(id) > 0;
    }
}