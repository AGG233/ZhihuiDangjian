package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@TableName("article")
@Schema(description = "文章")
public class Article {

    @Schema(description = "文章ID")
    private String id;

    @Schema(description = "作者ID")
    private String authorId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @EnumValue
    @Schema(description = "文章状态")
    private ArticleStatus status;

    @Schema(description = "发布时间")
    private String published_at;

    @Schema(description = "创建时间")
    private String created_at;

    @Schema(description = "更新时间")
    private String updated_at;
}
