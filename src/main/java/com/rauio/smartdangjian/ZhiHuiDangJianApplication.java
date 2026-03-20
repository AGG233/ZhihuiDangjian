package com.rauio.smartdangjian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@MapperScan("com.rauio.smartdangjian.mapper")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, FreeMarkerAutoConfiguration.class})
public class ZhiHuiDangJianApplication {
	public static void main(String[] args) {
        SpringApplication.run(ZhiHuiDangJianApplication.class, args);
    }
}