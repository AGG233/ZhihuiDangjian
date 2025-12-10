package com.rauio.ZhihuiDangjian.config;


import com.rauio.ZhihuiDangjian.pojo.response.Result;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.SpringBootConfiguration;

@SpringBootConfiguration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "智慧党建api文档",
                description = "智慧党建api文档",
                version = "1.0.0"
        )
)
public class OpenApiConfig {
    @PostConstruct
    public void init() {
        SpringDocUtils.getConfig().addResponseTypeToIgnore(Result.class);
    }
}
