package com.rauio.ZhihuiDangjian.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "分类/目录请求体")
public class CategoryDto {

    @Schema(description = "分类层级，最大不超过三级")
    private final int level = 0;
    @Schema(description = "分类名称")
    private String  name;
    @Schema(description = "分类描述")
    private String  description;
    @Schema(description = "分类父级ID")
    private String  parentId;

    @Schema(description = "分类排序")
    private String  sortOrder;

    @Schema(description = "分类状态")
    private Integer status;

    @Schema(description = "子节点")
    private List<CategoryDto> childrenNode;

}
