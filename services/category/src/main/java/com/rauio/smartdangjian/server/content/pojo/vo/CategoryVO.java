package com.rauio.smartdangjian.server.content.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分类视图对象，包含递归子分类树")
public class CategoryVO {

    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "所属学校ID")
    private String universityId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "父分类ID")
    private String parentId;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children;
}
