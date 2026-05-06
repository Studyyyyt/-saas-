package com.example.springboot.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.WechatOAuthService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/wechat/bind")
public class WechatBindController {

    private final PatientService patientService;
    private final WechatOAuthService wechatOAuthService;

    public WechatBindController(PatientService patientService, WechatOAuthService wechatOAuthService) {
        this.patientService = patientService;
        this.wechatOAuthService = wechatOAuthService;
    }

    @GetMapping("/start")
    public RedirectView start(@RequestParam Long patientId,
                              @RequestParam(required = false) String returnUrl) {
        List<Patient> patients = patientService.selectById(patientId);
        if (patients == null || patients.isEmpty()) {
            return new RedirectView("/portal-auth-error?reason=patient");
        }
        return new RedirectView(wechatOAuthService.buildAuthorizeUrl(patientId, returnUrl));
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam String code, @RequestParam String state) {
        String returnUrl = wechatOAuthService.consumeBindReturnUrl(state);
        Long patientId = wechatOAuthService.consumeBindState(state);
        if (patientId == null) {
            return new RedirectView("/portal-auth-error?reason=state");
        }
        String openid = wechatOAuthService.exchangeCodeForOpenid(code);
        patientService.bindWechatOpenid(patientId, openid);
        return new RedirectView(wechatOAuthService.buildSuccessRedirectUrl(patientId, returnUrl));
    }

    @GetMapping(value = "/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrcode(@RequestParam String text,
                                         @RequestParam(defaultValue = "220") int size) throws Exception {
        String content = text == null ? "" : text.trim();
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        int imageSize = Math.min(Math.max(size, 120), 512);
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, imageSize, imageSize, hints);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .contentType(MediaType.IMAGE_PNG)
                .body(outputStream.toByteArray());
    }
}
