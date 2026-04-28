package com.rauio.smartdangjian;


import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan(
        basePackages = "com.rauio.smartdangjian",
        annotationClass = org.apache.ibatis.annotations.Mapper.class
)
@EnableFileStorage
public class SmartDangjianApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartDangjianApplication.class, args);
    }
}
