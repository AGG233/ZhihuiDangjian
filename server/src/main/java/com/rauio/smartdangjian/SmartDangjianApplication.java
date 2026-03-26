package com.rauio.smartdangjian;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan(
        basePackages = "com.rauio.smartdangjian",
        annotationClass = org.apache.ibatis.annotations.Mapper.class
)

//@EnableFeignClients(basePackages = "com.rauio.smartdangjian")
//@EnableDiscoveryClient
public class SmartDangjianApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartDangjianApplication.class, args);
    }
}
