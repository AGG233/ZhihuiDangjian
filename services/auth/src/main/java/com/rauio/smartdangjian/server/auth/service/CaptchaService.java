package com.rauio.smartdangjian.server.auth.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.lang.UUID;
import com.rauio.smartdangjian.server.auth.pojo.Captcha;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.rauio.smartdangjian.constants.SecurityConstants.CAPTCHA_EXPIRATION;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final RedisTemplate<String,Object> redisTemplate;

    @Value("${auth.captcha.test-code:}")
    private String testCode;

    /**
     * 生成对外展示用验证码信息。
     *
     * @return 不包含明文验证码的验证码对象
     */
    public Captcha get() {
        Captcha captcha = generate();
        captcha.setCode(null);
        return captcha;
    }

    /**
     * 生成验证码并写入 Redis。
     *
     * @return 包含验证码内容和图片数据的验证码对象
     */
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

    /**
     * 校验验证码是否正确。
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return 是否校验通过
     */
    public Boolean validate(String uuid, String code) {
        if (testCode != null && !testCode.isBlank() && testCode.equals(code)) {
            return true;
        }
        return code != null && code.equals(redisTemplate.opsForValue().get("captcha:"+uuid));
    }
}
