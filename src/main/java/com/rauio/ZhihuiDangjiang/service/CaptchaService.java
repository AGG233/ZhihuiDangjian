package com.rauio.ZhihuiDangjiang.service;

import com.rauio.ZhihuiDangjiang.pojo.Captcha;

public interface CaptchaService {
    Captcha     get();
    Captcha     generate();
    Boolean     validate(String uuid, String code);
}
