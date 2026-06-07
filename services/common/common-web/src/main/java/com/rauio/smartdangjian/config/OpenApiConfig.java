package com.rauio.smartdangjian.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@AutoConfiguration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String version = getClass().getPackage().getImplementationVersion();
        return new OpenAPI()
                .info(new Info()
                        .title("智慧党建api文档")
                        .description("智慧党建api文档")
                        .version(version != null ? version : "dev"));
    }
}
