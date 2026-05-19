package com.rauio.smartdangjian.common.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学校信息响应")
public class SchoolResponse {

    @Schema(description = "学校ID")
    private String id;

    @Schema(description = "学校名称")
    private String name;
}
