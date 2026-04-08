package com.rauio.smartdangjian.server.content.pojo.vo;

import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "文章视图对象")
public class ArticleVO {

    @Schema(description = "文章ID")
    private String id;

    @Schema(description = "作者ID")
    private String authorId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "文章状态")
    private ArticleStatus status;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;
}
