package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "资源元数据更新请求。仅修改可编辑字段，未传字段保持原值不变")
public class ResourceMetaUpdateRequest {

    @Schema(description = "文件原始名称，允许重新维护展示名称", example = "党课封面.png")
    @Pattern(regexp = "^$|.*\\S.*", message = "originalName不能为空白字符")
    private String originalName;

    @Schema(description = "资源类型：0表示图片，1表示视频", example = "0")
    private Integer resourceType;

    @Schema(description = "对象存储键，即文件在COS中的完整存储路径", example = "image/8f14e45fceea167a5a36dedd4bea2543.png")
    @Pattern(regexp = "^$|.*\\S.*", message = "objectKey不能为空白字符")
    private String objectKey;

    @Schema(description = "资源状态：0表示上传中，1表示公开可用，2表示隐藏", example = "1")
    private Integer status;
}
