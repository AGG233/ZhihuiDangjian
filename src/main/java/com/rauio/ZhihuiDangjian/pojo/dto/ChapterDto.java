package com.rauio.ZhihuiDangjian.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "章节请求体")
public class ChapterDto {

    @Schema(description = "章节所属的课程ID")
    private Long  courseId;

    @Schema(description = "章节的标题",example = "党的第十九次全国代表大会讲解")
    private String  title;

    @Schema(description = "章节的描述内容",example = "本章主要讲解了党的十九大内容...")
    private String  description;

    @Schema(description = "建议学习时间，单位为秒，比如为半个小时就填1800",example = "1800")
    private Integer duration;

    @Schema(description = "该章节在课程中的排列顺序，比如第九章",example = "9")
    private Integer orderIndex;

    @Schema(description = "章节是否为必学，如果是则为False",example = "False")
    private Boolean isOptional;

    @Schema(description = "章节的状态，分为草稿，公开，归档不展示，通常来说都是公开，即published",allowableValues = {"draft","published","archived"},example = "published")
    private String  chapterStatus;

    @Schema(description = "章节的内容块列表,一般来说一个内容块对应着章节的一个内容，比如文本，视频，音频等等")
    private List<ContentBlockDto> content;
}
