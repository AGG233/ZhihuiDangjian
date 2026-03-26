package com.rauio.smartdangjian.server.resource.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("resource_meta")
@Schema(description = "资源元数据实体，对应resource_meta表，记录文件上传后的归属、名称、哈希、对象存储键、类型和状态")
public class ResourceMeta {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "资源ID，系统内资源主键", example = "1919810")
    private String  id;

    @Schema(description = "上传人ID，标识该资源由哪个用户创建", example = "114514")
    private String  uploaderId;

    @Schema(description = "文件原始名称，通常为用户上传时的文件名", example = "党课封面.png")
    private String  originalName;

    @Schema(description = "文件内容哈希，用于去重、秒传和资源检索", example = "8f14e45fceea167a5a36dedd4bea2543")
    private String  hash;

    @Schema(description = "对象存储键，即文件在COS中的完整存储路径", example = "image/8f14e45fceea167a5a36dedd4bea2543.png")
    private String  objectKey;

    @Schema(description = "资源类型，一般为MIME类型", example = "image/png")
    private String  resourceType;

    @Schema(description = "资源状态，通常1表示可用，具体含义由业务约定", example = "1")
    private Integer status;

}
