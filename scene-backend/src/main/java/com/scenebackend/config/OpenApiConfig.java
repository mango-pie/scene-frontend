package com.scenebackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI配置类
 * 替代原来的Swagger配置，兼容Spring Boot 3.x
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                // 标题
                .title("Scene Backend API Documentation")
                // 描述
                .description("场景后端接口文档，提供所有可用API的详细说明")
                // 版本
                .version("1.0.0")
                // 联系方式
                .contact(new Contact()
                        .name("开发团队")
                        .url("https://example.com")
                        .email("team@example.com"))
                // 许可证
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
    }
}