package com.rauio.smartdangjian.server.content.pojo.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文章响应体")
public class ArticleResponse {

    @Schema(description = "文章ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "作者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章所属分类ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "原文链接")
    private String sourceUrl;

    @Schema(description = "文章状态")
    private ArticleStatus status;

    @Schema(description = "文章内容块列表")
    private List<ContentBlockResponse> contentBlocks;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;
}
