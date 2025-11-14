package com.rauio.ZhihuiDangjian.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class UserAspect {

    private static final Logger logger = LoggerFactory.getLogger(ResourceAccessAspect.class);
}
