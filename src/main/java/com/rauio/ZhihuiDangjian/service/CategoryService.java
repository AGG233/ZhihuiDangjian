package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.dto.CategoryDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    public static final int MAX_LEVEL = 3;
    Boolean add(CategoryDto dto);

    Boolean addChildren(List<CategoryDto> list, String parentId);
    Boolean delete(String categoryId);
    Boolean deleteAll(String categoryId);

    Boolean update(CategoryDto dto, String id);

    CategoryVO getById(String id);
    List<CategoryVO>    getRootNodes();
    List<CategoryVO>    getChildren(String categoryId);
}
