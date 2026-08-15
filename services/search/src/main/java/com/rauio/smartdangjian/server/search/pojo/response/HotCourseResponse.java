package com.rauio.smartdangjian.server.search.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "热门课程视图对象")
public class HotCourseResponse {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "报名人数")
    private Integer enrollmentCount;

    @Schema(description = "近30天学习人数")
    private Integer recentLearnerCount;

    @Schema(description = "热度分 = 报名人数 + 近30天学习人数")
    private Integer hotScore;
}
