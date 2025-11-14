package com.rauio.ZhihuiDangjian.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Captcha {
    private String uuid;
    private String code;
    private String base64;

}
