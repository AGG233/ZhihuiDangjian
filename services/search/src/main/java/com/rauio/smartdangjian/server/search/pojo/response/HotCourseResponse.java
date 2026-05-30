package com.rauio.smartdangjian.server.search.pojo.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "热门课程")
public class HotCourseResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "学习人数")
    private Integer learnerCount;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "封面图片资源ID")
    private Long coverImageId;

    @Schema(description = "报名人数")
    private Integer enrollmentCount;

    @Schema(description = "平均评分")
    private BigDecimal averageRating;
}
