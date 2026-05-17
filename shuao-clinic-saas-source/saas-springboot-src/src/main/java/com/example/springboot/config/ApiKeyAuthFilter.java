package com.example.springboot.config;

import com.example.springboot.entity.ApiKey;
import com.example.springboot.mapper.ApiKeyMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API Key 认证过滤器
 * 拦截 /api/open/** 路径，校验 X-API-Key Header
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyMapper apiKeyMapper;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(ApiKeyMapper apiKeyMapper, ObjectMapper objectMapper) {
        this.apiKeyMapper = apiKeyMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKeyHeader = request.getHeader("X-API-Key");

        // 未提供 X-API-Key
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            writeUnauthorized(response, "401", "缺少X-API-Key");
            return;
        }

        // 查询数据库校验 Key
        ApiKey apiKey = apiKeyMapper.findByKey(apiKeyHeader.trim());
        if (apiKey == null || apiKey.getIsEnabled() == null || !apiKey.getIsEnabled()) {
            writeUnauthorized(response, "401", "无效的API Key");
            return;
        }

        // 将 clinic_id 注入请求属性，供后续 Controller 使用
        request.setAttribute("apiKeyClinicId", apiKey.getClinicId());

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 仅拦截 /api/open/** 路径
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/open/");
    }

    /**
     * 向响应写入 401 JSON
     */
    private void writeUnauthorized(HttpServletResponse response, String code, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("msg", msg);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
