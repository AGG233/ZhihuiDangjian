package com.rauio.smartdangjian.server.quiz.pojo.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SCORM 成绩上报请求。
 *
 * <p>对应 cmi.core.lesson_status / score.* / session_time / total_time 数据模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SCORM成绩上报请求")
public class ScormSubmitRequest {

    @Schema(description = "SCO标识")
    private String scoIdentifier;

    @Schema(description = "学习状态 cmi.core.lesson_status", example = "completed")
    private String lessonStatus;

    @Schema(description = "原始分数 cmi.core.score.raw", example = "85.00")
    private BigDecimal scoreRaw;

    @Schema(description = "会话时长（秒）cmi.core.session_time", example = "600")
    private Integer sessionTimeSeconds;

    @Schema(description = "累计时长（秒）cmi.core.total_time", example = "3600")
    private Integer totalTimeSeconds;
}
