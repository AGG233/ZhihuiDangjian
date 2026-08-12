package com.rauio.smartdangjian.server.content.pojo.request;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文章请求体")
public class ArticleRequest {

    @Schema(description = "文章ID，默认留空", hidden = true)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "作者的用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章所属分类ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    @Schema(description = "文章摘要，可留空")
    private String summary;

    @Schema(description = "原文链接，可留空")
    private String sourceUrl;

    @Schema(description = "文章状态，有'draft','published','archived'，分别对应草稿,公开，归档不公开")
    private ArticleStatus status;

    @Schema(description = "文章内容块列表，按顺序渲染")
    private List<ContentBlockDto> contentBlocks;
}
