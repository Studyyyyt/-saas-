package com.example.springboot.controller;

import com.example.springboot.service.WechatPatientBindSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/wechat/mp")
public class WechatMpCallbackController {

    private final WechatPatientBindSceneService sceneService;
    private final String token;

    @Autowired
    public WechatMpCallbackController(WechatPatientBindSceneService sceneService,
                                      @Value("${wechat.mp.token:shuao-wechat-token}") String token) {
        this.sceneService = sceneService;
        this.token = token == null || token.trim().isEmpty() ? "shuao-wechat-token" : token.trim();
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String verify(@RequestParam String signature,
                         @RequestParam String timestamp,
                         @RequestParam String nonce,
                         @RequestParam String echostr) {
        return isValidSignature(signature, timestamp, nonce) ? echostr : "";
    }

    @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public String receive(@RequestParam String signature,
                          @RequestParam String timestamp,
                          @RequestParam String nonce,
                          @RequestBody String xml) {
        if (!isValidSignature(signature, timestamp, nonce)) {
            return "";
        }
        if (!"subscribe".equalsIgnoreCase(extractXmlValue(xml, "Event"))
                && !"SCAN".equalsIgnoreCase(extractXmlValue(xml, "Event"))) {
            return successXml();
        }
        String openid = extractXmlValue(xml, "FromUserName");
        String eventKey = extractXmlValue(xml, "EventKey");
        String sceneKey = normalizeSceneKey(eventKey);
        if (sceneKey.isEmpty()) {
            return successXml();
        }
        sceneService.bindPatientByScene(sceneKey, openid);
        String content = "已完成公众号绑定，请点击患者入口继续查看。\nhttps://saas.shuao.cc/wechat/portal?openid="
                + escapeXml(openid) + "&scene=" + escapeXml(sceneKey);
        return textReply(extractXmlValue(xml, "ToUserName"), openid, content);
    }

    private boolean isValidSignature(String signature, String timestamp, String nonce) {
        try {
            String[] parts = {token, timestamp, nonce};
            Arrays.sort(parts);
            String joined = String.join("", parts);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractXmlValue(String xml, String tag) {
        Matcher matcher = Pattern.compile("<" + tag + "><!\\[CDATA\\[(.*?)]]></" + tag + ">|<" + tag + ">(.*?)</" + tag + ">", Pattern.DOTALL).matcher(xml == null ? "" : xml);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1) != null ? matcher.group(1).trim() : (matcher.group(2) == null ? "" : matcher.group(2).trim());
    }

    private String normalizeSceneKey(String eventKey) {
        String value = eventKey == null ? "" : eventKey.trim();
        if (value.startsWith("qrscene_")) {
            return value.substring("qrscene_".length());
        }
        return value;
    }

    private String textReply(String toUser, String fromUser, String content) {
        long now = System.currentTimeMillis() / 1000;
        return "<xml>"
                + "<ToUserName><![CDATA[" + toUser + "]]></ToUserName>"
                + "<FromUserName><![CDATA[" + fromUser + "]]></FromUserName>"
                + "<CreateTime>" + now + "</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType>"
                + "<Content><![CDATA[" + content + "]]></Content>"
                + "</xml>";
    }

    private String successXml() {
        return "success";
    }

    private String escapeXml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
