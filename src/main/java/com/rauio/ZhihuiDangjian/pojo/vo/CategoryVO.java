package com.rauio.ZhihuiDangjian.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    private String      id;
    private String      name;
    private String      description;
    private String      parentId;
    private Integer     sortOrder;
    private List<CategoryVO> children;
}
