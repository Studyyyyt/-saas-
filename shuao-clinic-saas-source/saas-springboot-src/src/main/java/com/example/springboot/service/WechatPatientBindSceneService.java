package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientTimeline;
import com.example.springboot.entity.PatientWechatBindScene;
import com.example.springboot.mapper.PatientWechatBindSceneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

@Service
public class WechatPatientBindSceneService {

    private final PatientWechatBindSceneMapper sceneMapper;
    private final PatientService patientService;
    private final PatientTimelineService patientTimelineService;
    private final String appId;
    private final String appSecret;

    @Autowired
    public WechatPatientBindSceneService(PatientWechatBindSceneMapper sceneMapper,
                                         PatientService patientService,
                                         PatientTimelineService patientTimelineService,
                                         @Value("${wechat.app-id:}") String appId,
                                         @Value("${wechat.app-secret:}") String appSecret) {
        this.sceneMapper = sceneMapper;
        this.patientService = patientService;
        this.patientTimelineService = patientTimelineService;
        this.appId = safeTrim(appId);
        this.appSecret = safeTrim(appSecret);
    }

    public PatientWechatBindScene ensureSceneForPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("患者不存在");
        }
        PatientWechatBindScene existed = sceneMapper.selectLatestByPatientId(patientId);
        if (existed != null && existed.getQr_url() != null && !existed.getQr_url().trim().isEmpty()) {
            return existed;
        }
        if (existed == null) {
            existed = new PatientWechatBindScene();
            existed.setPatient_id(patientId);
            existed.setScene_key(buildSceneKey(patientId));
            existed.setStatus("pending");
            sceneMapper.insert(existed);
        }
        hydrateWechatQr(existed);
        sceneMapper.updateQrInfo(existed);
        return sceneMapper.selectLatestByPatientId(patientId);
    }

    public Patient bindPatientByScene(String sceneKey, String openid) {
        if (safeTrim(sceneKey).isEmpty() || safeTrim(openid).isEmpty()) {
            return null;
        }
        PatientWechatBindScene scene = sceneMapper.selectBySceneKey(sceneKey);
        if (scene == null || scene.getPatient_id() == null) {
            return null;
        }
        Patient patient = patientService.bindWechatOpenid(scene.getPatient_id(), openid);
        if (patient == null) {
            return null;
        }
        scene.setStatus("bound");
        scene.setBound_at(new Date());
        scene.setBound_openid(openid);
        sceneMapper.markBound(scene);
        logTimeline(scene.getPatient_id(), patient.getName(), sceneKey);
        return patient;
    }

    void hydrateWechatQr(PatientWechatBindScene scene) {
        try {
            String accessToken = fetchAccessToken();
            String payload = "{"
                    + "\"expire_seconds\": 2592000,"
                    + "\"action_name\":\"QR_STR_SCENE\","
                    + "\"action_info\":{\"scene\":{\"scene_str\":\"" + escapeJson(scene.getScene_key()) + "\"}}"
                    + "}";
            String response = postJson("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + encode(accessToken), payload);
            String ticket = extractJsonString(response, "ticket");
            if (ticket == null || ticket.isEmpty()) {
                throw new IllegalStateException("生成公众号参数二维码失败: " + response);
            }
            scene.setQr_ticket(ticket);
            scene.setQr_url("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + encode(ticket));
            scene.setExpire_seconds(extractJsonInt(response, "expire_seconds"));
            scene.setStatus("ready");
        } catch (IOException e) {
            throw new IllegalStateException("生成公众号参数二维码失败: " + e.getMessage(), e);
        }
    }

    String fetchAccessToken() {
        if (appId.isEmpty() || appSecret.isEmpty()) {
            throw new IllegalStateException("微信公众号配置缺失");
        }
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                    + "&appid=" + encode(appId)
                    + "&secret=" + encode(appSecret);
            String response = readUrl(url);
            String token = extractJsonString(response, "access_token");
            if (token == null || token.isEmpty()) {
                throw new IOException("failed to fetch access token: " + response);
            }
            return token;
        } catch (IOException e) {
            throw new IllegalStateException("获取微信公众号 access_token 失败: " + e.getMessage(), e);
        }
    }

    private void logTimeline(Long patientId, String patientName, String sceneKey) {
        PatientTimeline timeline = new PatientTimeline();
        timeline.setPatient_id(patientId);
        timeline.setEvent_time(new Date());
        timeline.setEvent_type("wechat_bind");
        timeline.setEvent_title("公众号绑定成功");
        timeline.setEvent_content("患者“" + safe(patientName) + "”通过公众号参数二维码完成微信绑定，scene=" + safe(sceneKey));
        timeline.setSource_table("patient_wechat_bind_scene");
        patientTimelineService.add(timeline);
    }

    private String buildSceneKey(Long patientId) {
        return "patient_bind_" + patientId;
    }

    private String readUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        return readConnectionResponse(connection);
    }

    private String postJson(String url, String payload) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(bytes);
            }
            return readConnectionResponse(connection);
        } catch (IOException e) {
            throw new IllegalStateException("请求微信二维码接口失败: " + e.getMessage(), e);
        }
    }

    private String readConnectionResponse(HttpURLConnection connection) throws IOException {
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

    private Integer extractJsonInt(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end <= start) {
            return null;
        }
        return Integer.parseInt(json.substring(start, end));
    }

    private String safeTrim(String value) { return value == null ? "" : value.trim(); }
    private String safe(String value) { return value == null ? "" : value; }
    private String encode(String value) throws IOException { return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8); }
    private String escapeJson(String value) { return safe(value).replace("\\", "\\\\").replace("\"", "\\\""); }
}
