package com.example.springboot.service;

import com.example.springboot.entity.PortalAccessToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WechatOAuthService {

    private static final String TOKEN_BIND_STATE = "WECHAT_BIND_STATE";
    private static final String TOKEN_PATIENT_PORTAL_STATE = "PATIENT_PORTAL_STATE";
    private static final String TOKEN_STAFF_PORTAL_STATE = "STAFF_PORTAL_STATE";
    private static final String TOKEN_STAFF_BIND = "STAFF_BIND";
    private static final String TOKEN_PATIENT_PORTAL = "PATIENT_PORTAL";
    private static final String TOKEN_STAFF_PORTAL = "STAFF_PORTAL";
    private static final String TOKEN_ADMIN_REPORT = "ADMIN_REPORT";

    private final String oauthAppId;
    private final String oauthAppSecret;
    private final String bindBaseUrl;
    private final String bindSuccessUrl;
    private final String patientPortalUrl;
    private final String staffPortalUrl;
    private final String adminReportUrl;
    private final PortalAccessTokenService portalAccessTokenService;

    @Autowired
    public WechatOAuthService(
            @Value("${wechat.oauth.app-id:${wechat.app-id:}}") String oauthAppId,
            @Value("${wechat.oauth.app-secret:${wechat.app-secret:}}") String oauthAppSecret,
            @Value("${wechat.bind.base-url:https://saas.shuao.cc}") String bindBaseUrl,
            @Value("${wechat.bind.success-url:https://saas.shuao.cc/app/bind-success}") String bindSuccessUrl,
            @Value("${wechat.portal-url:https://saas.shuao.cc/patient-portal-home}") String patientPortalUrl,
            @Value("${wechat.staff-portal-url:https://saas.shuao.cc/staff-portal-home}") String staffPortalUrl,
            @Value("${wechat.admin-report-url:https://saas.shuao.cc/admin-report-h5}") String adminReportUrl,
            PortalAccessTokenService portalAccessTokenService) {
        this.oauthAppId = safeTrim(oauthAppId);
        this.oauthAppSecret = safeTrim(oauthAppSecret);
        this.bindBaseUrl = safeTrim(bindBaseUrl).isEmpty() ? "https://saas.shuao.cc" : safeTrim(bindBaseUrl);
        this.bindSuccessUrl = safeTrim(bindSuccessUrl).isEmpty() ? "https://saas.shuao.cc/app/bind-success" : safeTrim(bindSuccessUrl);
        this.patientPortalUrl = safeTrim(patientPortalUrl).isEmpty() ? "https://saas.shuao.cc/patient-portal-home" : safeTrim(patientPortalUrl);
        this.staffPortalUrl = safeTrim(staffPortalUrl).isEmpty() ? "https://saas.shuao.cc/staff-portal-home" : safeTrim(staffPortalUrl);
        this.adminReportUrl = safeTrim(adminReportUrl).isEmpty() ? "https://saas.shuao.cc/admin-report-h5" : safeTrim(adminReportUrl);
        this.portalAccessTokenService = portalAccessTokenService;
    }

    public WechatOAuthService(String oauthAppId,
                              String oauthAppSecret,
                              String bindBaseUrl,
                              String bindSuccessUrl) {
        this.oauthAppId = safeTrim(oauthAppId);
        this.oauthAppSecret = safeTrim(oauthAppSecret);
        this.bindBaseUrl = safeTrim(bindBaseUrl).isEmpty() ? "https://saas.shuao.cc" : safeTrim(bindBaseUrl);
        this.bindSuccessUrl = safeTrim(bindSuccessUrl).isEmpty() ? "https://saas.shuao.cc/app/bind-success" : safeTrim(bindSuccessUrl);
        this.patientPortalUrl = "https://saas.shuao.cc/patient-portal-home";
        this.staffPortalUrl = "https://saas.shuao.cc/staff-portal-home";
        this.adminReportUrl = "https://saas.shuao.cc/admin-report-h5";
        this.portalAccessTokenService = new PortalAccessTokenService();
    }

    public WechatOAuthService(String oauthAppId,
                              String oauthAppSecret,
                              String bindBaseUrl,
                              String bindSuccessUrl,
                              String patientPortalUrl,
                              String staffPortalUrl,
                              String adminReportUrl) {
        this.oauthAppId = safeTrim(oauthAppId);
        this.oauthAppSecret = safeTrim(oauthAppSecret);
        this.bindBaseUrl = safeTrim(bindBaseUrl).isEmpty() ? "https://saas.shuao.cc" : safeTrim(bindBaseUrl);
        this.bindSuccessUrl = safeTrim(bindSuccessUrl).isEmpty() ? "https://saas.shuao.cc/app/bind-success" : safeTrim(bindSuccessUrl);
        this.patientPortalUrl = safeTrim(patientPortalUrl).isEmpty() ? "https://saas.shuao.cc/patient-portal-home" : safeTrim(patientPortalUrl);
        this.staffPortalUrl = safeTrim(staffPortalUrl).isEmpty() ? "https://saas.shuao.cc/staff-portal-home" : safeTrim(staffPortalUrl);
        this.adminReportUrl = safeTrim(adminReportUrl).isEmpty() ? "https://saas.shuao.cc/admin-report-h5" : safeTrim(adminReportUrl);
        this.portalAccessTokenService = new PortalAccessTokenService();
    }

    public String buildAuthorizeUrl(Long patientId) {
        return buildAuthorizeUrl(patientId, null);
    }

    public String buildAuthorizeUrl(Long patientId, String returnUrl) {
        String cleanReturnUrl = safeTrim(returnUrl);
        String state = portalAccessTokenService.issueToken(TOKEN_BIND_STATE, patientId, cleanReturnUrl, Duration.ofMinutes(15));
        String redirectUri = encode(bindBaseUrl + "/wechat/bind/callback");
        return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + encode(oauthAppId)
                + "&redirect_uri=" + redirectUri
                + "&response_type=code&scope=snsapi_userinfo&state=" + state
                + "#wechat_redirect";
    }

    public String buildPortalAuthorizeUrl() {
        String state = portalAccessTokenService.issueToken(TOKEN_PATIENT_PORTAL_STATE, null, "patient-portal", Duration.ofMinutes(15));
        String redirectUri = encode(bindBaseUrl + "/patient-portal/callback");
        return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + encode(oauthAppId)
                + "&redirect_uri=" + redirectUri
                + "&response_type=code&scope=snsapi_base&state=" + state
                + "#wechat_redirect";
    }

    public String buildStaffAuthorizeUrl() {
        String state = portalAccessTokenService.issueToken(TOKEN_STAFF_PORTAL_STATE, null, "staff-portal", Duration.ofMinutes(15));
        String redirectUri = encode(bindBaseUrl + "/staff-portal/callback");
        return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + encode(oauthAppId)
                + "&redirect_uri=" + redirectUri
                + "&response_type=code&scope=snsapi_base&state=" + state
                + "#wechat_redirect";
    }

    public boolean isPortalState(String state) {
        return portalAccessTokenService.consumeToken(TOKEN_PATIENT_PORTAL_STATE, state) != null;
    }

    public boolean isStaffPortalState(String state) {
        return portalAccessTokenService.consumeToken(TOKEN_STAFF_PORTAL_STATE, state) != null;
    }

    public String issueStaffBindToken(String openid) {
        return portalAccessTokenService.issueToken(TOKEN_STAFF_BIND, null, openid, Duration.ofMinutes(30));
    }

    public String consumeStaffBindToken(String token) {
        PortalAccessToken record = portalAccessTokenService.consumeToken(TOKEN_STAFF_BIND, token);
        return record == null ? null : record.getPayload();
    }

    public String issuePatientPortalToken(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("患者不存在");
        }
        return portalAccessTokenService.issueToken(TOKEN_PATIENT_PORTAL, patientId, null, Duration.ofDays(30));
    }

    public Long resolvePatientPortalToken(String token) {
        PortalAccessToken record = portalAccessTokenService.resolveActiveToken(TOKEN_PATIENT_PORTAL, token);
        return record == null ? null : record.getSubject_id();
    }

    public String issueStaffPortalToken(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("员工账号不存在");
        }
        return portalAccessTokenService.issueToken(TOKEN_STAFF_PORTAL, accountId, null, Duration.ofDays(30));
    }

    public Long resolveStaffPortalToken(String token) {
        PortalAccessToken record = portalAccessTokenService.resolveActiveToken(TOKEN_STAFF_PORTAL, token);
        return record == null ? null : record.getSubject_id();
    }

    public String issueAdminReportToken(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("管理员账号不存在");
        }
        return portalAccessTokenService.issueToken(TOKEN_ADMIN_REPORT, accountId, null, Duration.ofDays(7));
    }

    public Long resolveAdminReportToken(String token) {
        PortalAccessToken record = portalAccessTokenService.resolveActiveToken(TOKEN_ADMIN_REPORT, token);
        return record == null ? null : record.getSubject_id();
    }

    public Long consumeBindState(String state) {
        PortalAccessToken record = portalAccessTokenService.consumeToken(TOKEN_BIND_STATE, state);
        return record == null ? null : record.getSubject_id();
    }

    public String consumeBindReturnUrl(String state) {
        PortalAccessToken record = portalAccessTokenService.resolveActiveToken(TOKEN_BIND_STATE, state);
        return record == null ? null : record.getPayload();
    }

    public String buildBindEntryUrl(Long patientId, String returnUrl) {
        StringBuilder builder = new StringBuilder(bindBaseUrl)
                .append("/wechat/bind/start?patientId=")
                .append(patientId == null ? "" : patientId);
        String cleanReturnUrl = safeTrim(returnUrl);
        if (!cleanReturnUrl.isEmpty()) {
            builder.append("&returnUrl=").append(encode(cleanReturnUrl));
        }
        return builder.toString();
    }

    public String exchangeCodeForOpenid(String code) {
        if (oauthAppId.isEmpty() || oauthAppSecret.isEmpty()) {
            return "mock-openid-" + code;
        }
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + encode(oauthAppId)
                    + "&secret=" + encode(oauthAppSecret)
                    + "&code=" + encode(code)
                    + "&grant_type=authorization_code";
            String response = readUrl(url);
            String openid = extractJsonString(response, "openid");
            if (openid == null || openid.isEmpty()) {
                throw new IOException("failed to exchange openid: " + response);
            }
            return openid;
        } catch (Exception e) {
            System.out.println("[WECHAT_OAUTH_FAIL] " + e.getMessage());
            return "mock-openid-" + code;
        }
    }

    public String buildSuccessRedirectUrl(Long patientId) {
        return buildPatientPortalUrl(patientId, issuePatientPortalToken(patientId), "success");
    }

    public String buildSuccessRedirectUrl(Long patientId, String returnUrl) {
        String cleanReturnUrl = safeTrim(returnUrl);
        if (!cleanReturnUrl.isEmpty()) {
            String resolvedUrl = cleanReturnUrl;
            if (!resolvedUrl.contains("patientId=")) {
                resolvedUrl = appendQueryParam(resolvedUrl, "patientId", String.valueOf(patientId));
            }
            resolvedUrl = appendQueryParam(resolvedUrl, "portalToken", issuePatientPortalToken(patientId));
            resolvedUrl = appendQueryParam(resolvedUrl, "bindStatus", "success");
            return resolvedUrl;
        }
        return buildSuccessRedirectUrl(patientId);
    }

    public String buildPortalHomeUrl(Long patientId) {
        return buildPatientPortalUrl(patientId, issuePatientPortalToken(patientId), null);
    }

    public String buildPatientBindRedirectUrl(Long patientId) {
        return patientPortalUrl + "?patientId=" + patientId;
    }

    public String buildPatient360Url(Long patientId) {
        return bindBaseUrl + "/Patient360?id=" + patientId;
    }

    public String buildStaffHomeUrl(Long accountId) {
        return buildStaffHomeUrl(accountId, issueStaffPortalToken(accountId));
    }

    public String buildAdminReportUrl(Long accountId, String focus) {
        String token = issueAdminReportToken(accountId);
        StringBuilder builder = new StringBuilder(adminReportUrl)
                .append("?accountId=").append(accountId)
                .append("&reportToken=").append(encode(token));
        String focusValue = safeTrim(focus);
        if (!focusValue.isEmpty()) {
            builder.append("&focus=").append(encode(focusValue));
        }
        return builder.toString();
    }

    private String buildStaffHomeUrl(Long accountId, String staffToken) {
        return staffPortalUrl + "?accountId=" + accountId
                + "&staffToken=" + encode(staffToken);
    }

    private String buildPatientPortalUrl(Long patientId, String portalToken, String bindStatus) {
        StringBuilder builder = new StringBuilder(patientPortalUrl)
                .append("?patientId=").append(patientId)
                .append("&portalToken=").append(encode(portalToken));
        if (!safeTrim(bindStatus).isEmpty()) {
            builder.append("&bindStatus=").append(encode(bindStatus));
        }
        return builder.toString();
    }

    private String appendQueryParam(String url, String key, String value) {
        String cleanUrl = safeTrim(url);
        if (cleanUrl.isEmpty()) {
            return cleanUrl;
        }
        String encodedValue = encode(value);
        if (cleanUrl.contains(key + "=")) {
            return cleanUrl;
        }
        return cleanUrl + (cleanUrl.contains("?") ? "&" : "?") + key + "=" + encodedValue;
    }

    private String readUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        try (Scanner scanner = new Scanner(
                connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return json.substring(start, end);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
