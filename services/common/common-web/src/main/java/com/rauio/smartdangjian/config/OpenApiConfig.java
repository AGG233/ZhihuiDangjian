package com.rauio.smartdangjian.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@AutoConfiguration
@OpenAPIDefinition(
        info =
                @io.swagger.v3.oas.annotations.info.Info(
                        title = "智慧党建api文档",
                        description = "智慧党建api文档",
                        version = "1.0.0"))
public class OpenApiConfig {}
