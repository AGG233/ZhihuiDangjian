package com.rauio.ZhihuiDangjian.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder

@Schema(description = "资源信息请求体")
public class ResourceDto {

    @Schema(description = "资源id")
    private String  id;

    @Schema(description = "资源原始名称")
    private String  originalFilename;

    @Schema(description = "资源类型")
    private String  mimeType;

    @Schema(description = "资源链接")
    private String  path;

    @Schema(description = "资源大小")
    private Long    fileSizeBytes;
}
