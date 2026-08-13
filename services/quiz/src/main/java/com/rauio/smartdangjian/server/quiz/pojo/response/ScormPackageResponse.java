package com.rauio.smartdangjian.server.quiz.pojo.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SCORM 学习包响应。
 *
 * <p>上传解析成功后的学习包摘要，不含 imsmanifest.xml 原文。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SCORM学习包响应")
public class ScormPackageResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "学习包ID")
    private Long id;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "SCORM版本", example = "2004")
    private String version;

    @Schema(description = "manifest标识")
    private String identifier;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
