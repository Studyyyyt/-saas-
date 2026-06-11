package com.example.springboot.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring Web 拦截器，从请求头中获取当前诊所ID并放入 ClinicContext
 * 供 MyBatis 多租户拦截器使用
 */
@Component
public class ClinicWebInterceptor implements HandlerInterceptor {

    /**
     * 白名单路径：这些路径不需要诊所上下文
     */
    private static final String[] WHITE_LIST = {
        "/auth/login",
        "/auth/register",
        "/api/open/",
        "/swagger-ui",
        "/v3/api-docs",
        "/index.html",
        "/static/",
        "/favicon.ico"
    };

    /** 合法的诊所ID格式：字母、数字、下划线、连字符，长度1-64 */
    private static final String CLINIC_ID_PATTERN = "^[a-zA-Z0-9_-]{1,64}$";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 白名单路径不处理
        for (String white : WHITE_LIST) {
            if (uri.startsWith(white)) {
                return true;
            }
        }

        // 从请求头中获取当前诊所ID
        String clinicId = request.getHeader("X-Clinic-Id");
        if (clinicId != null && !clinicId.isEmpty()) {
            // 校验诊所ID格式，防止恶意输入进入SQL
            if (!clinicId.matches(CLINIC_ID_PATTERN)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"code\":\"400\",\"msg\":\"非法的诊所ID格式\"}");
                return false;
            }
            ClinicContext.set(clinicId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除 ThreadLocal，防止内存泄漏
        ClinicContext.clear();
    }
}
