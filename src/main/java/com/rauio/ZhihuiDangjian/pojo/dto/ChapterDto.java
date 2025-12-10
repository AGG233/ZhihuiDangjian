package com.rauio.ZhihuiDangjian.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "章节请求体")
public class ChapterDto {

    @Schema(description = "章节所属的课程ID")
    @NotBlank(message = "课程ID不能为空")
    private Long  courseId;

    @Schema(description = "章节的标题",example = "党的第十九次全国代表大会讲解")
    @NotBlank(message = "章节标题不能为空")
    private String  title;

    @Schema(description = "章节的描述内容",example = "本章主要讲解了党的十九大内容...")
    @NotBlank(message = "章节描述不能为空")
    private String  description;

    @Schema(description = "建议学习时间，单位为秒，比如为半个小时就填1800，不填默认为-1",example = "1800")
    @Builder.Default
    private Integer duration = -1;

    @Schema(description = "该章节在课程中的排列顺序，比如第九章",example = "9")
    @NotBlank(message = "章节顺序不能为空")
    private Integer orderIndex;

    @Schema(description = "章节是否为必学，如果是则为False，默认为false",example = "False")
    @Builder.Default
    private Boolean isOptional = false;

    @Schema(description = "章节的状态，分为草稿，公开，归档不展示，通常为公开，即published，如果还在编辑状态则为草稿，即draft，默认为draft",allowableValues = {"draft","published","archived"},example = "published")
    @Builder.Default
    private String  chapterStatus = "draft";

    @Schema(description = "章节的内容块列表,一般来说一个内容块对应着章节的一个内容，比如文本，视频，音频等等")
    @NotBlank(message = "章节内容块列表不能为空")
    private List<ContentBlockDto> content;
}
