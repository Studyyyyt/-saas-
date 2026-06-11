package com.example.springboot.config;

/**
 * 诊所上下文，使用 ThreadLocal 存储当前请求的诊所ID
 * 供 MyBatis 拦截器和 Controller 使用
 */
public class ClinicContext {

    private static final ThreadLocal<String> CURRENT_CLINIC_ID = new ThreadLocal<>();

    /**
     * 设置当前诊所ID
     */
    public static void set(String clinicId) {
        CURRENT_CLINIC_ID.set(clinicId);
    }

    /**
     * 获取当前诊所ID
     */
    public static String get() {
        return CURRENT_CLINIC_ID.get();
    }

    /**
     * 清除当前诊所ID（请求结束后必须调用，防止内存泄漏）
     */
    public static void clear() {
        CURRENT_CLINIC_ID.remove();
    }

    /**
     * 检查当前是否有诊所上下文
     */
    public static boolean hasContext() {
        return CURRENT_CLINIC_ID.get() != null;
    }
}
