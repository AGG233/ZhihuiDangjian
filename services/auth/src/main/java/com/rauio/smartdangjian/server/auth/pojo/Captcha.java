package com.rauio.smartdangjian.server.auth.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "验证码响应")
public class Captcha {
    @Schema(description = "验证码唯一标识")
    private String uuid;

    @Schema(description = "验证码文本")
    private String code;

    @Schema(description = "验证码图片Base64")
    private String base64;
}
