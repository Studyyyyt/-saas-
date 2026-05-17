package com.example.springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * CORS 跨域配置
 * 遵循最小权限原则，仅允许指定的前端 Origin 访问
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许的跨域来源列表，通过 application.yml 配置
     * 默认允许本地开发环境前端地址
     */
    @Value("${cors.allowed-origins:http://localhost:7070,https://localhost:7070}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有来源，兼容浏览器调试工具（Apifox/Hoppscotch等）和本地开发
                .allowedOriginPatterns("*")
                // 允许所有 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许所有请求头
                .allowedHeaders("*")
                // 不携带凭证，避免与 allowedOriginPatterns("*") 冲突
                .allowCredentials(false)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }
}
