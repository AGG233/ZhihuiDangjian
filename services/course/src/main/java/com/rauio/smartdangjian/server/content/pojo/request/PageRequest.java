package com.rauio.smartdangjian.server.content.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Schema(description = "分页请求参数")
public class PageRequest {
    @Schema(description = "页码，从1开始", example = "1")
    private int pageNum;
    @Schema(description = "每页条数", example = "10")
    private int pageSize;
}
