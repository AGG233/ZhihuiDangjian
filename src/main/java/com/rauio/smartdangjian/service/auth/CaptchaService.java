package com.rauio.smartdangjian.service.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.lang.UUID;
import com.rauio.smartdangjian.pojo.Captcha;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.rauio.smartdangjian.constants.SecurityConstants.CAPTCHA_EXPIRATION;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final RedisTemplate<String,Object> redisTemplate;

    public Captcha get() {
        Captcha captcha = generate();
        captcha.setCode(null);
        return captcha;
    }
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
    public Boolean validate(String uuid, String code) {
        return code != null && code.equals(redisTemplate.opsForValue().get("captcha:"+uuid));
    }
}
