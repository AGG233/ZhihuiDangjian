package com.rauio.ZhihuiDangjian.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.lang.UUID;
import com.rauio.ZhihuiDangjian.pojo.Captcha;
import com.rauio.ZhihuiDangjian.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.rauio.ZhihuiDangjian.constants.SecurityConstants.CAPTCHA_EXPIRATION;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String,Object> redisTemplate;

    public Captcha get() {
        Captcha captcha = generate();
        captcha.setCode(null);
        return captcha;
    }

    @Override
    public Captcha generate() {
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100,4,500);
        captcha.setGenerator(randomGenerator);
        captcha.createCode();
        captcha.setTextAlpha(0.2f);
        String code = captcha.getCode();

        String uuid = UUID.randomUUID().toString().replace("-","");
        String base64 = captcha.getImageBase64();
        redisTemplate.opsForValue().set("captcha:"+uuid, code, CAPTCHA_EXPIRATION, TimeUnit.MILLISECONDS);

        return Captcha.builder().uuid(uuid).code(code).base64(base64).build();
    }

    @Override
    public Boolean validate(String uuid, String code) {
        return code != null && code.equals(redisTemplate.opsForValue().get("captcha:"+uuid));
    }
}
