package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.AppointmentService;
import com.example.springboot.service.MedicalRecordService;
import com.example.springboot.service.PatientConsentService;
import com.example.springboot.service.PatientFollowupService;
import com.example.springboot.service.PatientImageService;
import com.example.springboot.service.PatientRiskTagService;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.PatientTimelineService;
import com.example.springboot.service.TreatmentBillingService;
import com.example.springboot.service.TreatmentService;
import com.example.springboot.service.WechatOAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.example.springboot.mapper.PatientMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientPortalBindRoutingTest {

    @Test
    void patientPortalCallbackShouldRedirectToPatientPortalHome() {
        PatientService patientService = mock(PatientService.class);
        MedicalRecordService medicalRecordService = mock(MedicalRecordService.class);
        PatientImageService patientImageService = mock(PatientImageService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        PatientConsentService patientConsentService = mock(PatientConsentService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);

        PatientPortalController controller = new PatientPortalController(
                patientService,
                medicalRecordService,
                patientImageService,
                appointmentService,
                patientConsentService,
                wechatOAuthService
        );

        Patient patient = new Patient();
        patient.setId(3);
        patient.setName("张三");
        patient.setWechat_openid("openid-patient-3");

        when(wechatOAuthService.isPortalState("portal-state-001")).thenReturn(true);
        when(wechatOAuthService.exchangeCodeForOpenid("code-portal-001")).thenReturn("openid-patient-3");
        when(patientService.selectByWechatOpenid("openid-patient-3")).thenReturn(patient);
        when(wechatOAuthService.buildPortalHomeUrl(3L))
                .thenReturn("https://saas.shuao.cc/patient-portal-home?patientId=3&portalToken=patient-portal-token-003");

        var redirect = controller.callback("code-portal-001", "portal-state-001");
        assertEquals("https://saas.shuao.cc/patient-portal-home?patientId=3&portalToken=patient-portal-token-003", redirect.getUrl());
    }

    @Test
    void patient360OverviewShouldReturnBindUrlPointingToPatientPortalFlow() {
        PatientMapper patientMapper = mock(PatientMapper.class);
        MedicalRecordService medicalRecordService = mock(MedicalRecordService.class);
        PatientFollowupService followupService = mock(PatientFollowupService.class);
        PatientRiskTagService riskTagService = mock(PatientRiskTagService.class);
        PatientTimelineService timelineService = mock(PatientTimelineService.class);
        PatientImageService imageService = mock(PatientImageService.class);
        TreatmentService treatmentService = mock(TreatmentService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        TreatmentBillingService treatmentBillingService = mock(TreatmentBillingService.class);
        PatientConsentService patientConsentService = mock(PatientConsentService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);

        Patient360Controller controller = new Patient360Controller();
        ReflectionTestUtils.setField(controller, "patientMapper", patientMapper);
        ReflectionTestUtils.setField(controller, "medicalRecordService", medicalRecordService);
        ReflectionTestUtils.setField(controller, "followupService", followupService);
        ReflectionTestUtils.setField(controller, "riskTagService", riskTagService);
        ReflectionTestUtils.setField(controller, "timelineService", timelineService);
        ReflectionTestUtils.setField(controller, "imageService", imageService);
        ReflectionTestUtils.setField(controller, "treatmentService", treatmentService);
        ReflectionTestUtils.setField(controller, "appointmentService", appointmentService);
        ReflectionTestUtils.setField(controller, "treatmentBillingService", treatmentBillingService);
        ReflectionTestUtils.setField(controller, "patientConsentService", patientConsentService);
        ReflectionTestUtils.setField(controller, "wechatOAuthService", wechatOAuthService);

        Patient patient = new Patient();
        patient.setId(5);
        patient.setName("李四");
        patient.setWechat_openid(null);

        when(patientMapper.selectById(5L)).thenReturn(List.of(patient));
        when(medicalRecordService.selectByPatientId(5L)).thenReturn(Collections.emptyList());
        when(followupService.selectByPatientId(5L)).thenReturn(Collections.emptyList());
        when(riskTagService.selectActiveByPatientId(5L)).thenReturn(Collections.emptyList());
        when(timelineService.selectByPatientId(5L)).thenReturn(Collections.emptyList());
        when(treatmentService.selectByPatientReference(5L)).thenReturn(Collections.emptyList());
        when(imageService.selectByPatientId(5L)).thenReturn(Collections.emptyList());
        when(appointmentService.selectPatientAppointments(5L)).thenReturn(Collections.emptyList());
        when(patientConsentService.selectByPatientId(5L)).thenReturn(Collections.emptyList());
        when(wechatOAuthService.buildPatientBindRedirectUrl(5L)).thenReturn("https://saas.shuao.cc/patient-portal-home?patientId=5");
        when(wechatOAuthService.buildBindEntryUrl(5L, "https://saas.shuao.cc/patient-portal-home?patientId=5"))
                .thenReturn("https://saas.shuao.cc/wechat/bind/start?patientId=5&returnUrl=https%3A%2F%2Fsaas.shuao.cc%2Fpatient-portal-home%3FpatientId%3D5");

        Result result = controller.getOverview(5L);
        Map<String, Object> payload = (Map<String, Object>) result.getData();
        String bindUrl = String.valueOf(payload.get("wechatBindUrl"));

        assertEquals("未绑定微信", payload.get("wechatBindStatusLabel"));
        assertFalse(Boolean.TRUE.equals(payload.get("wechatBound")));
        assertTrue(bindUrl.contains("/wechat/bind/start"));
        assertTrue(bindUrl.contains("patient-portal-home"));
        assertFalse(bindUrl.contains("/Patient360"));
    }

    @Test
    void patientPortalOverviewShouldRejectMismatchedPortalToken() {
        PatientService patientService = mock(PatientService.class);
        MedicalRecordService medicalRecordService = mock(MedicalRecordService.class);
        PatientImageService patientImageService = mock(PatientImageService.class);
        AppointmentService appointmentService = mock(AppointmentService.class);
        PatientConsentService patientConsentService = mock(PatientConsentService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);

        PatientPortalController controller = new PatientPortalController(
                patientService,
                medicalRecordService,
                patientImageService,
                appointmentService,
                patientConsentService,
                wechatOAuthService
        );

        when(wechatOAuthService.resolvePatientPortalToken("patient-token-005")).thenReturn(6L);

        Result result = controller.overview(5L, "patient-token-005");

        assertEquals("500", result.getCode());
        assertEquals("患者身份校验失败，请重新从公众号进入", result.getMsg());
    }
}
