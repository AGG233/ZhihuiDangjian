package com.rauio.ZhihuiDangjian.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseCategoryDto {
    private String  name;
    private String  description;
    private final int level = 0;
    private String  parentId;
    private String  sortOrder;
    private Integer status;
    private List<CourseCategoryDto> childrenNode;

}
