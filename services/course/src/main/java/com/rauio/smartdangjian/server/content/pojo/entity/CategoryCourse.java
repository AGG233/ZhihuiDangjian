package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("category_couse")
@Schema(description = "分类-课程关联")
public class CategoryCourse {

    @Schema(description = "分类ID")
    private String categoryId;

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "课程ID")
    private String courseId;
}
