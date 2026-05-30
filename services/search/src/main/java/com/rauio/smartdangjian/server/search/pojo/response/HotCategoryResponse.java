package com.rauio.smartdangjian.server.search.pojo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "热门分类")
public class HotCategoryResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "学习人数")
    private Integer learnerCount;
}
