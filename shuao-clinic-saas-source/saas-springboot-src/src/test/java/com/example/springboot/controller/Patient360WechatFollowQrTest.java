package com.example.springboot.controller;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientWechatBindScene;
import com.example.springboot.service.AppointmentService;
import com.example.springboot.service.MedicalRecordService;
import com.example.springboot.service.PatientConsentService;
import com.example.springboot.service.PatientFollowupService;
import com.example.springboot.service.PatientImageService;
import com.example.springboot.service.PatientRiskTagService;
import com.example.springboot.service.PatientTimelineService;
import com.example.springboot.service.TreatmentBillingService;
import com.example.springboot.service.TreatmentService;
import com.example.springboot.service.WechatOAuthService;
import com.example.springboot.service.WechatPatientBindSceneService;
import com.example.springboot.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Patient360WechatFollowQrTest {

    private final StubPatientMapper patientMapper = new StubPatientMapper();
    private final StubMedicalRecordService medicalRecordService = new StubMedicalRecordService();
    private final StubPatientFollowupService followupService = new StubPatientFollowupService();
    private final StubPatientRiskTagService riskTagService = new StubPatientRiskTagService();
    private final StubPatientTimelineService timelineService = new StubPatientTimelineService();
    private final StubPatientImageService imageService = new StubPatientImageService();
    private final StubTreatmentService treatmentService = new StubTreatmentService();
    private final StubAppointmentService appointmentService = new StubAppointmentService();
    private final StubTreatmentBillingService treatmentBillingService = new StubTreatmentBillingService();
    private final StubPatientConsentService patientConsentService = new StubPatientConsentService();
    private final StubWechatOAuthService wechatOAuthService = new StubWechatOAuthService();
    private final StubWechatPatientBindSceneService sceneService = new StubWechatPatientBindSceneService();
    private Patient360Controller controller;

    @BeforeEach
    void setUp() {
        controller = new Patient360Controller();
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
        ReflectionTestUtils.setField(controller, "wechatPatientBindSceneService", sceneService);
    }

    @Test
    void shouldReturnWechatFollowQrUrlForUnboundPatient() {
        Patient patient = new Patient();
        patient.setId(18);
        patient.setName("未绑定患者");
        patient.setWechat_openid("");
        patientMapper.result = List.of(patient);
        sceneService.scene.setQr_url("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=ticket-18");

        Result result = controller.getOverview(18L);

        assertEquals("200", result.getCode());
        Map<String, Object> payload = (Map<String, Object>) result.getData();
        assertEquals("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=ticket-18", payload.get("wechatFollowQrUrl"));
        assertEquals("", payload.get("wechatBindUrl"));
    }

    @Test
    void shouldFallbackToBindUrlWhenWechatFollowQrFails() {
        Patient patient = new Patient();
        patient.setId(19);
        patient.setName("绑定失败回退患者");
        patient.setWechat_openid("");
        patientMapper.result = List.of(patient);
        wechatOAuthService.bindEntryUrl = "https://saas.shuao.cc/wechat/bind/start?patientId=19";
        sceneService.shouldThrow = true;

        Result result = controller.getOverview(19L);

        assertEquals("200", result.getCode());
        Map<String, Object> payload = (Map<String, Object>) result.getData();
        assertEquals("", payload.get("wechatFollowQrUrl"));
        assertEquals("https://saas.shuao.cc/wechat/bind/start?patientId=19", payload.get("wechatBindUrl"));
    }

    static class StubPatientMapper extends Patient360ControllerAppointmentOverviewTest.StubPatientMapper {}
    static class StubMedicalRecordService extends Patient360ControllerAppointmentOverviewTest.StubMedicalRecordService {}
    static class StubPatientFollowupService extends Patient360ControllerAppointmentOverviewTest.StubPatientFollowupService {}
    static class StubPatientRiskTagService extends Patient360ControllerAppointmentOverviewTest.StubPatientRiskTagService {}
    static class StubPatientTimelineService extends Patient360ControllerAppointmentOverviewTest.StubPatientTimelineService {}
    static class StubPatientImageService extends Patient360ControllerAppointmentOverviewTest.StubPatientImageService {}
    static class StubTreatmentService extends Patient360ControllerAppointmentOverviewTest.StubTreatmentService {}
    static class StubAppointmentService extends Patient360ControllerAppointmentOverviewTest.StubAppointmentService {}
    static class StubTreatmentBillingService extends Patient360ControllerAppointmentOverviewTest.StubTreatmentBillingService {}
    static class StubPatientConsentService extends Patient360ControllerAppointmentOverviewTest.StubPatientConsentService {}
    static class StubWechatOAuthService extends Patient360ControllerAppointmentOverviewTest.StubWechatOAuthService {}

    static class StubWechatPatientBindSceneService extends WechatPatientBindSceneService {
        PatientWechatBindScene scene = new PatientWechatBindScene();
        boolean shouldThrow = false;
        StubWechatPatientBindSceneService() {
            super(null, null, null, "wx-app", "wx-secret");
        }
        @Override public PatientWechatBindScene ensureSceneForPatient(Long patientId) {
            if (shouldThrow) {
                throw new IllegalStateException("wechat api unavailable");
            }
            return scene;
        }
    }
}
