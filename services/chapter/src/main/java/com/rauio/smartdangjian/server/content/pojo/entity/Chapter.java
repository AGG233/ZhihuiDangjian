package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("chapter")
@Schema(description = "章节")
public class Chapter {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
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

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
