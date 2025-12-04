package com.rauio.ZhihuiDangjian.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    private Long      id;
    private String      name;
    private String      description;
    private Long      parentId;
    private Integer     sortOrder;
    private List<CategoryVO> children;
}
