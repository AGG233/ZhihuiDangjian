package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("category_article")
@Schema(description = "分类-文章关联")
public class CategoryArticle {

    @Schema(description = "分类ID")
    private String categoryId;

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "文章ID")
    private String articleId;
}
