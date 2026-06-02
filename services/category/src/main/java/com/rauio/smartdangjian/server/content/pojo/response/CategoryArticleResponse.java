package com.rauio.smartdangjian.server.content.pojo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分类-文章关联响应")
public record CategoryArticleResponse(
        @Schema(description = "分类ID") Long categoryId,
        @JsonSerialize(using = ToStringSerializer.class) @Schema(description = "文章ID") Long articleId) {

    public static CategoryArticleResponse from(CategoryArticle relation) {
        if (relation == null) {
            return null;
        }
        return new CategoryArticleResponse(relation.getCategoryId(), relation.getArticleId());
    }
}
