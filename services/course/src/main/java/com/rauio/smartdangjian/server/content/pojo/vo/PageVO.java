package com.rauio.smartdangjian.server.content.pojo.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
@Schema(description = "通用分页视图对象")
public class PageVO<T> {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "每页大小")
    private Long size;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "数据列表")
    private List<T> list;
}
