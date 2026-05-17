package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文件上传请求体")
public class UploadFileRequest {

    @Schema(description = "上传用户ID，服务端会覆盖为当前登录用户", hidden = true)
    private String userId;

    @Schema(description = "文件原始名称（含扩展名），用于提取后缀并生成对象存储键", example = "party-lesson-cover.png")
    @NotBlank(message = "文件名不能为空")
    private String  fileName;

    @Schema(description = "文件的 MIME 类型，如 image/png、video/mp4 等。用于设置 COS Content-Type 头并确定存储目录", example = "image/png")
    @NotNull(message = "文件类型不能为空")
    private String mimeType;
}
