package com.rauio.smartdangjian.server.quiz.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * SCORM 学习包实体。
 *
 * <p>对应 scorm_package 表，记录一次导入的 SCORM 学习包（imsmanifest.xml 与包文件地址）。
 * 主键由 MyBatis-Plus ASSIGN_ID 雪花算法生成（与全项目 bigint unsigned 主键一致）。
 */
@Data
@Builder
@TableName("scorm_package")
@Schema(description = "SCORM学习包")
public class ScormPackage {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "学习包ID")
    private Long id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "SCORM版本", example = "2004")
    private String version;

    @Schema(description = "manifest标识")
    private String identifier;

    @TableField("manifest_content")
    @Schema(description = "imsmanifest.xml原文")
    private String manifestContent;

    @TableField("file_url")
    @Schema(description = "包文件地址")
    private String fileUrl;

    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
