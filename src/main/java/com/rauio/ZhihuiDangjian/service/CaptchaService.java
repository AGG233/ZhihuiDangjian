package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.Captcha;

public interface CaptchaService {
    Captcha     get();
    Captcha     generate();
    Boolean     validate(String uuid, String code);
}
