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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API Key 认证过滤器
 * 所有接口均支持通过 X-API-Key 访问（单 Key 模式），
 * 不带 Key 的请求也放行以兼容前端现有登录流程。
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

        // 未提供 X-API-Key：放行，兼容前端现有登录流程
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 查询数据库校验 Key
        ApiKey apiKey = apiKeyMapper.findByKey(apiKeyHeader.trim());
        if (apiKey == null) {
            writeUnauthorized(response, "401", "无效的API Key");
            return;
        }

        // 检查是否启用
        if (apiKey.getIsEnabled() == null || !apiKey.getIsEnabled()) {
            writeUnauthorized(response, "401", "API Key已禁用");
            return;
        }

        // 检查是否过期
        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            writeUnauthorized(response, "401", "API Key已过期");
            return;
        }

        // 更新最后使用时间和使用次数（异步，不阻塞请求）
        try {
            apiKeyMapper.updateUsage(apiKey.getId());
        } catch (Exception e) {
            logger.warn("更新API Key使用统计失败: " + apiKey.getId(), e);
        }

        // 将 clinic_id 注入请求属性，供后续 Controller 使用
        request.setAttribute("apiKeyClinicId", apiKey.getClinicId());

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }

        // 白名单：登录、注册、API Key 自身接口、错误页面
        if (path.startsWith("/auth/")
                || path.equals("/api/api-key")
                || path.equals("/api/api-key/regenerate")
                || path.startsWith("/error")) {
            return true;
        }

        // 白名单：静态资源（Spring Boot 通常已单独处理，此处作为兜底）
        if (path.contains(".") && (
                path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".html")
                        || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")
                        || path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico")
                        || path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf")
                        || path.endsWith(".eot") || path.endsWith(".map") || path.endsWith(".json"))) {
            return true;
        }

        // 其他所有路径均经过 Filter（有 Key 则验证，无 Key 则放行）
        return false;
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
