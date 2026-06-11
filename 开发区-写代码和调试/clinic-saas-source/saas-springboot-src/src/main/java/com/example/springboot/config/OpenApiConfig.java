package com.example.springboot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger 接口文档配置
 * 访问地址：http://localhost:8080/swagger-ui.html
 * API JSON 地址：http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 基础信息
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("口腔门诊 SaaS 开放接口文档")
                        .description("供 n8n 等外部系统通过 API Key 调用的开放数据接口")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("技术支持"))
                        .license(new License()
                                .name("内部使用")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("/").description("当前服务器")
                ));
    }

    /**
     * 全局添加 API Key 参数说明
     * 所有接口都需要在 Header 中携带 X-API-Key
     */
    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            // 只为 OpenDataController 下的接口添加 API Key 参数说明
            String className = handlerMethod.getBeanType().getSimpleName();
            if ("OpenDataController".equals(className)) {
                operation.addParametersItem(new Parameter()
                        .name("X-API-Key")
                        .description("诊所 API Key，用于认证（格式：sk-saas-xxxxx）")
                        .in("header")
                        .required(true));
            }
            return operation;
        };
    }

}
