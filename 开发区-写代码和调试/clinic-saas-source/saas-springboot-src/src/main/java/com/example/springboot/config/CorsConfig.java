package com.example.springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * CORS 跨域配置和 Web 拦截器注册
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Autowired
    @NonNull
    private ClinicWebInterceptor clinicWebInterceptor;

    @Autowired
    @NonNull
    private LicenseCheckInterceptor licenseCheckInterceptor;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 授权验证拦截器（最先执行）
        registry.addInterceptor(licenseCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/needs-init",
                        "/auth/init-system",
                        "/auth/renew-license",
                        "/api/open/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/index.html",
                        "/static/**",
                        "/favicon.ico",
                        "/error"
                );

        // 诊所上下文拦截器
        registry.addInterceptor(clinicWebInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/api/open/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/index.html",
                        "/static/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
