package com.example.springboot.config;

import com.example.springboot.service.LicenseVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 授权验证拦截器
 * 定期调用 Easytoac 激活码服务验证系统授权状态
 * 激活码被删除或过期后，拦截非白名单请求返回 403
 */
@Component
public class LicenseCheckInterceptor implements HandlerInterceptor {

    /** 白名单路径：这些路径不受授权拦截 */
    private static final String[] WHITE_LIST = {
        "/auth/login",
        "/auth/register",
        "/auth/needs-init",
        "/auth/init",
        "/auth/init-system",
        "/auth/renew-license",
        "/api/api-key",
        "/api/open/",
        "/swagger-ui",
        "/v3/api-docs",
        "/index.html",
        "/static/",
        "/favicon.ico",
        "/error"
    };

    /** 缓存有效期：2 分钟（毫秒） */
    private static final long CACHE_TTL_MS = 2 * 60 * 1000;

    /** 内存缓存：key 固定为 default，value 为缓存结果 */
    private final ConcurrentHashMap<String, CachedResult> cache = new ConcurrentHashMap<>();

    @Autowired
    private LicenseVerificationService licenseVerificationService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 白名单路径放行
        for (String white : WHITE_LIST) {
            if (uri.startsWith(white)) {
                return true;
            }
        }

        // 检查缓存
        CachedResult cached = cache.get("default");
        if (cached != null && !cached.isExpired()) {
            if (!cached.isValid()) {
                reject(response, cached.getMessage());
                return false;
            }
            return true;
        }

        // 缓存不存在或已过期，调用外部服务验证
        LicenseVerificationService.LicenseVerifyResult result =
                licenseVerificationService.verifyByClinicId("default");

        // 更新缓存
        cache.put("default", new CachedResult(result.isValid(), result.getMessage(), System.currentTimeMillis()));

        if (!result.isValid()) {
            reject(response, result.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 手动刷新缓存（供前端调用刷新接口时使用）
     */
    public void invalidateCache() {
        cache.clear();
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-License-Invalid", "true");
        String json = String.format("{\"code\":\"403\",\"msg\":\"授权无效：%s\",\"data\":null}", message);
        response.getWriter().write(json);
    }

    /**
     * 缓存结果内部类
     */
    private static class CachedResult {
        private final boolean valid;
        private final String message;
        private final long cacheTime;

        CachedResult(boolean valid, String message, long cacheTime) {
            this.valid = valid;
            this.message = message;
            this.cacheTime = cacheTime;
        }

        boolean isValid() {
            return valid;
        }

        String getMessage() {
            return message;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cacheTime > CACHE_TTL_MS;
        }
    }
}
