package com.example.springboot.service;

import com.example.springboot.entity.Clinic;
import com.example.springboot.mapper.AccountMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 激活码授权验证服务
 * 负责调用外部 Easytoac 激活码系统验证诊所授权状态
 */
@Service
public class LicenseVerificationService {

    /** 激活码服务地址，通过环境变量或配置文件注入 */
    @Value("${license.server.url:http://license-server:3000}")
    private String licenseServerUrl;

    /** 是否启用授权验证，默认启用 */
    @Value("${license.enabled:true}")
    private boolean licenseEnabled;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ClinicService clinicService;
    private final AccountMapper accountMapper;

    public LicenseVerificationService(@Lazy ClinicService clinicService, AccountMapper accountMapper) {
        this.clinicService = clinicService;
        this.accountMapper = accountMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 验证诊所的激活码授权状态
     *
     * @param clinic 诊所实体
     * @return 验证结果，包含是否有效及过期时间
     */
    public LicenseVerifyResult verifyClinicLicense(Clinic clinic) {
        return verifyClinicLicense(clinic, null);
    }

    /**
     * 验证诊所的激活码授权状态（可指定 machine_id）
     *
     * @param clinic 诊所实体
     * @param overrideMachineId 指定的 machine_id，优先于数据库查询
     * @return 验证结果，包含是否有效及过期时间
     */
    public LicenseVerifyResult verifyClinicLicense(Clinic clinic, String overrideMachineId) {
        // 如果未启用授权验证，直接返回有效
        if (!licenseEnabled) {
            return LicenseVerifyResult.valid();
        }

        // 如果诊所未绑定激活码，返回无效
        if (clinic == null || clinic.getActivationCode() == null || clinic.getActivationCode().trim().isEmpty()) {
            return LicenseVerifyResult.invalid("诊所未绑定激活码");
        }

        try {
            // machine_id 直接使用超级管理员用户名，便于区分不同 SaaS 实例
            // 注意：若超级管理员账号改名，machine_id 会变，需重新激活
            String machineId = overrideMachineId;
            if (machineId == null || machineId.isEmpty()) {
                machineId = accountMapper.selectFirstAdminUsername();
            }
            if (machineId == null || machineId.isEmpty()) {
                machineId = clinic.getId();
            }

            // 构造请求体
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "code", clinic.getActivationCode(),
                    "machine_id", machineId
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(licenseServerUrl + "/api/verify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return LicenseVerifyResult.invalid("授权服务异常，状态码: " + response.statusCode());
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Boolean success = (Boolean) result.get("success");

            if (Boolean.TRUE.equals(success)) {
                // 解析过期时间
                String expiresAt = (String) result.get("expires_at");
                LocalDateTime expiresDateTime = null;
                if (expiresAt != null && !expiresAt.isEmpty()) {
                    try {
                        expiresDateTime = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_DATE_TIME);
                    } catch (Exception e1) {
                        try {
                            // 尝试带时区偏移的格式，如 2026-05-31T12:00:00+08:00
                            expiresDateTime = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        } catch (Exception e2) {
                            try {
                                // 尝试去掉末尾 Z 的格式
                                expiresDateTime = LocalDateTime.parse(expiresAt.replace("Z", ""));
                            } catch (Exception e3) {
                                System.err.println("无法解析过期时间: " + expiresAt);
                            }
                        }
                    }
                }
                return LicenseVerifyResult.valid(expiresDateTime);
            } else {
                String message = (String) result.getOrDefault("message", "激活码验证失败");
                return LicenseVerifyResult.invalid(message);
            }

        } catch (Exception e) {
            // 网络异常时，如果本地已记录过期时间且未过期，允许通过（容错机制）
            if (clinic.getLicenseExpiresAt() != null && clinic.getLicenseExpiresAt().isAfter(LocalDateTime.now())) {
                return LicenseVerifyResult.valid(clinic.getLicenseExpiresAt());
            }
            return LicenseVerifyResult.invalid("授权服务不可用: " + e.getMessage());
        }
    }

    /**
     * 验证指定诊所ID的授权状态
     */
    public LicenseVerifyResult verifyByClinicId(String clinicId) {
        Clinic clinic = clinicService.getById(clinicId);
        if (clinic == null) {
            return LicenseVerifyResult.invalid("诊所不存在");
        }
        return verifyClinicLicense(clinic);
    }

    /**
     * 授权验证结果
     */
    public static class LicenseVerifyResult {
        private final boolean valid;
        private final String message;
        private final LocalDateTime expiresAt;

        private LicenseVerifyResult(boolean valid, String message, LocalDateTime expiresAt) {
            this.valid = valid;
            this.message = message;
            this.expiresAt = expiresAt;
        }

        public static LicenseVerifyResult valid() {
            return new LicenseVerifyResult(true, null, null);
        }

        public static LicenseVerifyResult valid(LocalDateTime expiresAt) {
            return new LicenseVerifyResult(true, null, expiresAt);
        }

        public static LicenseVerifyResult invalid(String message) {
            return new LicenseVerifyResult(false, message, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }
}
