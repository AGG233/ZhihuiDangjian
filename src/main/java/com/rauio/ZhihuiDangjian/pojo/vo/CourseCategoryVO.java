package com.rauio.ZhihuiDangjian.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class CourseCategoryVO {
    private String      id;
    private String      name;
    private String      description;
    private String      parentId;
    private Integer     sortOrder;
    private List<CourseCategoryVO> children;
}
