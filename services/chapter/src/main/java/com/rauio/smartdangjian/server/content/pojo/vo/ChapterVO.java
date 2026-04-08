package com.rauio.smartdangjian.server.content.pojo.vo;

import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "章节视图对象")
public class ChapterVO {

    @Schema(description = "章节ID")
    private String id;

    @Schema(description = "所属课程ID")
    private String courseId;

    @Schema(description = "章节标题")
    private String title;

    @Schema(description = "章节描述")
    private String description;

    @Schema(description = "建议学习时长（秒）")
    private Integer duration;

    @Schema(description = "章节排序序号")
    private Integer orderIndex;

    @Schema(description = "是否为选修章节")
    private Boolean isOptional;

    @Schema(description = "章节状态")
    private String chapterStatus;

    @Schema(description = "章节内容块列表")
    private List<ContentBlock> content;
}
