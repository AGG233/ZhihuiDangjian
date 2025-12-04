package com.rauio.ZhihuiDangjian.pojo.dto;

import com.rauio.ZhihuiDangjian.utils.Spec.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文章请求体")
public class ArticleDto {

    @Schema(description = "文章ID，默认留空",hidden = true)
    private Long id;
    @Schema(description = "作者的用户ID")
    private String author_id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要，可留空")
    private String summary;

    @Schema(description = "文章状态，有'draft','published','archived'，分别对应草稿,公开，归档不公开")
    private ArticleStatus status;
}