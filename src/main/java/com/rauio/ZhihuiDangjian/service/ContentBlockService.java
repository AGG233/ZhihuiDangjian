package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.ContentBlock;
import com.rauio.ZhihuiDangjian.pojo.vo.ContentBlockVO;

import java.util.List;

public interface ContentBlockService {
    Boolean save(ContentBlock dto);

    Boolean saveBatch(List<ContentBlock> dtos);

    Boolean delete(String id);

    Boolean update(ContentBlock dto);

    ContentBlockVO get(String id);

    List<ContentBlockVO> getAllByParentId(String parentId);

    List<ContentBlockVO> getByResourceId(List<String> resourceId);
}
