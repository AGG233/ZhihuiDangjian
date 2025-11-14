package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.dto.CourseCategoryDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseCategoryVO;

import java.util.List;

public interface CourseCategoryService {
    public static final int MAX_LEVEL = 3;
    Boolean add(CourseCategoryDto dto);

    Boolean addChildren(List<CourseCategoryDto> list, String parentId);
    Boolean delete(String categoryId);
    Boolean deleteAll(String categoryId);

    Boolean update(CourseCategoryDto dto, String id);

    CourseCategoryVO        getById(String id);
    List<CourseCategoryVO>    getRootNodes();
    List<CourseCategoryVO>    getChildren(String categoryId);
}
