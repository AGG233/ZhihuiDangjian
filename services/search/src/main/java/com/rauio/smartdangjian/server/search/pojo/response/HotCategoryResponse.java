package com.rauio.smartdangjian.server.search.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "热门分类视图对象")
public class HotCategoryResponse {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "关联已发布课程数")
    private Integer courseCount;

    @Schema(description = "关联课程报名人数合计")
    private Integer enrollmentSum;
}
