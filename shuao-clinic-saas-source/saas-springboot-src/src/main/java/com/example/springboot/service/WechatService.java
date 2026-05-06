package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Service
public class WechatService {

    private final String deliveryMode;
    private final String appId;
    private final String appSecret;
    private final String appointmentCreatedTemplateId;
    private final String businessAnalysisTemplateId;
    private final String businessAlertTemplateId;
    private final String appointmentNoticeBaseUrl;

    @Autowired
    public WechatService(
            @Value("${wechat.delivery-mode:mock}") String deliveryMode,
            @Value("${wechat.app-id:}") String appId,
            @Value("${wechat.app-secret:}") String appSecret,
            @Value("${wechat.template.appointment-created:}") String appointmentCreatedTemplateId,
            @Value("${wechat.template.business-analysis:}") String businessAnalysisTemplateId,
            @Value("${wechat.template.business-alert:}") String businessAlertTemplateId,
            @Value("${wechat.notice-base-url:https://saas.shuao.cc}") String appointmentNoticeBaseUrl) {
        this.deliveryMode = safeTrim(deliveryMode).isEmpty() ? "mock" : safeTrim(deliveryMode);
        this.appId = safeTrim(appId);
        this.appSecret = safeTrim(appSecret);
        this.appointmentCreatedTemplateId = safeTrim(appointmentCreatedTemplateId);
        this.businessAnalysisTemplateId = safeTrim(businessAnalysisTemplateId);
        this.businessAlertTemplateId = safeTrim(businessAlertTemplateId);
        this.appointmentNoticeBaseUrl = trimTrailingSlash(safeTrim(appointmentNoticeBaseUrl));
    }

    public WechatService(String deliveryMode,
                         String appId,
                         String appSecret,
                         String appointmentCreatedTemplateId) {
        this(deliveryMode, appId, appSecret, appointmentCreatedTemplateId, "", "", "https://saas.shuao.cc");
    }

    public void sendAdminBusinessReportNotification(Account account,
                                                    String reportTypeLabel,
                                                    String periodLabel,
                                                    String headline,
                                                    String summary,
                                                    Integer operatingScore,
                                                    String detailUrl) {
        if (account == null) {
            System.out.println("[WECHAT_ADMIN_REPORT_SKIP] reason=null-account");
            return;
        }
        String openid = safe(account.getWechat_openid()).trim();
        if (openid.isEmpty()) {
            System.out.println("[WECHAT_ADMIN_REPORT_SKIP] reason=blank-openid, accountId=" + account.getId());
            return;
        }
        String title = trimToLength(reportTypeLabel + "：" + safe(headline), 20);
        String summaryText = trimToLength(summary, 20);
        String scoreText = operatingScore == null ? "评分待生成" : ("经营评分 " + operatingScore);
        String periodText = trimToLength(periodLabel, 20);
        String payload = "{"
                + "\"touser\":\"" + escapeJson(openid) + "\","
                + "\"template_id\":\"" + escapeJson(businessAnalysisTemplateId) + "\","
                + "\"url\":\"" + escapeJson(detailUrl) + "\","
                + "\"data\":{"
                + "\"keyword1\":{\"value\":\"" + escapeJson(trimToLength(reportTypeLabel, 20)) + "\"},"
                + "\"keyword2\":{\"value\":\"" + escapeJson(periodText) + "\"},"
                + "\"keyword3\":{\"value\":\"" + escapeJson(scoreText) + "\"},"
                + "\"thing4\":{\"value\":\"" + escapeJson(title) + "\"},"
                + "\"thing5\":{\"value\":\"" + escapeJson(summaryText) + "\"}"
                + "}}";
        sendManagementTemplateNotification("WECHAT_ADMIN_REPORT", openid, businessAnalysisTemplateId, payload,
                reportTypeLabel + " " + periodLabel + " -> " + headline);
    }

    public void sendAdminBusinessAlertNotification(Account account,
                                                   String alertTitle,
                                                   String alertLevel,
                                                   String alertSummary,
                                                   String detailUrl) {
        if (account == null) {
            System.out.println("[WECHAT_ADMIN_ALERT_SKIP] reason=null-account");
            return;
        }
        String openid = safe(account.getWechat_openid()).trim();
        if (openid.isEmpty()) {
            System.out.println("[WECHAT_ADMIN_ALERT_SKIP] reason=blank-openid, accountId=" + account.getId());
            return;
        }
        String payload = "{"
                + "\"touser\":\"" + escapeJson(openid) + "\","
                + "\"template_id\":\"" + escapeJson(businessAlertTemplateId) + "\","
                + "\"url\":\"" + escapeJson(detailUrl) + "\","
                + "\"data\":{"
                + "\"keyword1\":{\"value\":\"" + escapeJson(trimToLength(alertTitle, 20)) + "\"},"
                + "\"keyword2\":{\"value\":\"" + escapeJson(trimToLength(alertLevel, 20)) + "\"},"
                + "\"keyword3\":{\"value\":\"" + escapeJson(trimToLength(currentDateText(), 20)) + "\"},"
                + "\"thing4\":{\"value\":\"" + escapeJson(trimToLength(alertSummary, 20)) + "\"}"
                + "}}";
        sendManagementTemplateNotification("WECHAT_ADMIN_ALERT", openid, businessAlertTemplateId, payload,
                alertLevel + " " + alertTitle + " -> " + alertSummary);
    }

    public void sendAppointmentCreatedNotification(Patient patient, Appointment appointment) {
        sendAppointmentNotification(patient, appointment, false);
    }

    public void sendAppointmentReminderNotification(Patient patient, Appointment appointment) {
        sendAppointmentNotification(patient, appointment, true);
    }

    private void sendAppointmentNotification(Patient patient, Appointment appointment, boolean reminderMode) {
        System.out.println("[WECHAT_SEND_ENTER] patientId=" + (patient == null ? "null" : patient.getId())
                + ", patientName=" + (patient == null ? "null" : patient.getName())
                + ", openid=" + (patient == null ? "null" : patient.getWechat_openid())
                + ", appointmentId=" + (appointment == null ? "null" : appointment.getId())
                + ", reminderMode=" + reminderMode
                + ", deliveryMode=" + deliveryMode
                + ", appIdPresent=" + !appId.isEmpty()
                + ", appSecretPresent=" + !appSecret.isEmpty()
                + ", templatePresent=" + !appointmentCreatedTemplateId.isEmpty());
        if (patient == null || appointment == null) {
            System.out.println("[WECHAT_SEND_SKIP] reason=null-patient-or-appointment");
            return;
        }
        if (patient.getWechat_openid() == null || patient.getWechat_openid().trim().isEmpty()) {
            System.out.println("[WECHAT_SEND_SKIP] reason=blank-openid, patientId=" + patient.getId());
            return;
        }

        if (!"real".equalsIgnoreCase(deliveryMode)) {
            System.out.println("[WECHAT_SEND_BRANCH] mode=" + deliveryMode + " -> mock");
            logMockNotification(patient, appointment);
            return;
        }

        if (appId.isEmpty() || appSecret.isEmpty() || appointmentCreatedTemplateId.isEmpty()) {
            System.out.println("[WECHAT_SEND_SKIP] reason=missing-config, mode=" + deliveryMode
                    + ", appIdPresent=" + !appId.isEmpty()
                    + ", appSecretPresent=" + !appSecret.isEmpty()
                    + ", templatePresent=" + !appointmentCreatedTemplateId.isEmpty());
            logMockNotification(patient, appointment);
            return;
        }

        try {
            System.out.println("[WECHAT_SEND_TOKEN_FETCH] patientId=" + patient.getId() + ", appointmentId=" + appointment.getId());
            String accessToken = fetchAccessToken();
            System.out.println("[WECHAT_SEND_TOKEN_OK] tokenLength=" + accessToken.length());
            String payload = buildAppointmentTemplatePayload(patient, appointment, reminderMode);
            System.out.println("[WECHAT_SEND_POST] payloadLength=" + payload.length()
                    + ", patientId=" + patient.getId()
                    + ", appointmentId=" + appointment.getId());
            postJson("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + encode(accessToken), payload);
            System.out.println("[WECHAT_SEND_OK] patientId=" + patient.getId() + ", appointmentId=" + appointment.getId());
        } catch (Exception e) {
            System.out.println("[WECHAT_REAL_FAIL] type=" + e.getClass().getSimpleName() + ", message=" + e.getMessage());
            e.printStackTrace(System.out);
            logMockNotification(patient, appointment);
        }
    }

    String debugConfigSummary() {
        return "deliveryMode=" + deliveryMode + ",appId=" + appId + ",templateId=" + appointmentCreatedTemplateId;
    }

    private void sendManagementTemplateNotification(String logPrefix,
                                                    String openid,
                                                    String templateId,
                                                    String payload,
                                                    String mockSummary) {
        System.out.println("[" + logPrefix + "_ENTER] openid=" + openid
                + ", deliveryMode=" + deliveryMode
                + ", templatePresent=" + !templateId.isEmpty());
        if (!"real".equalsIgnoreCase(deliveryMode)) {
            System.out.println("[" + logPrefix + "_MOCK] " + mockSummary);
            return;
        }
        if (appId.isEmpty() || appSecret.isEmpty() || templateId.isEmpty()) {
            System.out.println("[" + logPrefix + "_SKIP] reason=missing-config, appIdPresent=" + !appId.isEmpty()
                    + ", appSecretPresent=" + !appSecret.isEmpty()
                    + ", templatePresent=" + !templateId.isEmpty());
            System.out.println("[" + logPrefix + "_MOCK] " + mockSummary);
            return;
        }
        try {
            String accessToken = fetchAccessToken();
            postJson("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + encode(accessToken), payload);
            System.out.println("[" + logPrefix + "_OK] openid=" + openid);
        } catch (Exception exception) {
            System.out.println("[" + logPrefix + "_FAIL] type=" + exception.getClass().getSimpleName() + ", message=" + exception.getMessage());
            exception.printStackTrace(System.out);
            System.out.println("[" + logPrefix + "_MOCK] " + mockSummary);
        }
    }

    private String fetchAccessToken() throws IOException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + encode(appId)
                + "&secret=" + encode(appSecret);
        String response = readUrl(url);
        String token = extractJsonString(response, "access_token");
        if (token == null || token.isEmpty()) {
            throw new IOException("failed to fetch access token: " + response);
        }
        return token;
    }

    private String buildAppointmentTemplatePayload(Patient patient, Appointment appointment, boolean reminderMode) {
        String patientName = safe(patient.getName());
        String dateValue = safe(String.valueOf(appointment.getAppointment_date()));
        String timeValue = normalizeTimeText(appointment.getAppointment_time());
        String purpose = safe(appointment.getAppointment_purpose());
        String doctor = safe(appointment.getDoctor_name());
        String hospitalName = "长沙舒澳口腔";
        String scheduleValue = dateValue + (timeValue.isEmpty() ? "" : " " + timeValue);
        String doctorValue = doctor.isEmpty() ? "门诊医生待确认" : doctor;
        String purposeValue = purpose.isEmpty() ? "到院面诊" : purpose;
        String firstLine = (reminderMode ? "明日就诊提醒：" : "预约提醒：") + scheduleValue;
        String remarkLine = "医生：" + doctorValue + "，项目：" + purposeValue;

        return "{"
                + "\"touser\":\"" + escapeJson(patient.getWechat_openid()) + "\"," 
                + "\"template_id\":\"" + escapeJson(appointmentCreatedTemplateId) + "\"," 
                + "\"url\":\"" + escapeJson(buildAppointmentNoticeUrl(appointment)) + "\"," 
                + "\"first\":\"" + escapeJson(firstLine) + "\"," 
                + "\"remark\":\"" + escapeJson(remarkLine) + "\"," 
                + "\"data\":{"
                + "\"keyword1\":{\"value\":\"" + escapeJson(scheduleValue) + "\"},"
                + "\"keyword2\":{\"value\":\"" + escapeJson(doctorValue) + "\"},"
                + "\"keyword3\":{\"value\":\"" + escapeJson(purposeValue) + "\"},"
                + "\"phrase2\":{\"value\":\"" + escapeJson(patientName) + "\"},"
                + "\"time7\":{\"value\":\"" + escapeJson(scheduleValue) + "\"},"
                + "\"thing8\":{\"value\":\"" + escapeJson(purposeValue) + "\"},"
                + "\"thing11\":{\"value\":\"" + escapeJson(hospitalName) + "\"},"
                + "\"thing9\":{\"value\":\"" + escapeJson(doctorValue) + "\"}"
                + "}}";
    }

    private void postJson(String url, String payload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(bytes);
        }
        String response = readConnectionResponse(connection);
        String errCode = extractJsonRawValue(response, "errcode");
        if (errCode != null && !"0".equals(errCode) && !"0.0".equals(errCode)) {
            throw new IOException("wechat send failed: " + response);
        }
    }

    private String readUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        return readConnectionResponse(connection);
    }

    private String readConnectionResponse(HttpURLConnection connection) throws IOException {
        try (Scanner scanner = new Scanner(
                connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private void logMockNotification(Patient patient, Appointment appointment) {
        System.out.println("[WECHAT_MOCK] send appointment notification -> openid="
                + patient.getWechat_openid()
                + ", patient=" + patient.getName()
                + ", date=" + appointment.getAppointment_date()
                + ", time=" + appointment.getAppointment_time()
                + ", purpose=" + appointment.getAppointment_purpose());
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

    private String extractJsonRawValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && ",} \n\r\t".indexOf(json.charAt(end)) == -1) {
            end++;
        }
        return json.substring(start, end).replace("\"", "").trim();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) throws IOException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String buildAppointmentNoticeUrl(Appointment appointment) {
        return appointmentNoticeBaseUrl + "/appointment-notice?id=" + appointment.getId();
    }

    private String normalizeTimeText(Object value) {
        String text = safe(value == null ? null : String.valueOf(value)).trim();
        if (text.endsWith(":00:00")) {
            return text.substring(0, text.length() - 3);
        }
        if (text.endsWith(":00") && text.length() == 8) {
            return text.substring(0, 5);
        }
        return text;
    }

    private String trimToLength(String value, int maxLength) {
        String text = safe(value).trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String currentDateText() {
        return java.time.LocalDate.now().toString();
    }

    private String escapeJson(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
