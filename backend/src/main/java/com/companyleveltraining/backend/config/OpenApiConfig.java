package com.companyleveltraining.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI campusOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("校园综合服务平台 API")
                .version("1.0.0")
                .description("实验室预约、门户、通知、日程、AI 助手和管理员工作台接口文档"))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
            .schemaRequirement(BEARER_AUTH, new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));
    }
}
