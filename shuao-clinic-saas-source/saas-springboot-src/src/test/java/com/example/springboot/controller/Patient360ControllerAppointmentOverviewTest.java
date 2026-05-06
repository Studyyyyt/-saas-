package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientFollowup;
import com.example.springboot.entity.PatientImage;
import com.example.springboot.entity.PatientRiskTag;
import com.example.springboot.entity.PatientTimeline;
import com.example.springboot.entity.Treatment;
import com.example.springboot.mapper.PatientMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.sql.Time;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Patient360ControllerAppointmentOverviewTest {

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
    void getOverview_shouldIncludeAllAppointmentsForPatient() {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patientMapper.result = List.of(patient);

        Appointment latest = new Appointment();
        latest.setId(11);
        latest.setPatient_name("张三");
        latest.setAppointment_date(Date.valueOf("2026-04-23"));
        latest.setAppointment_time(Time.valueOf("11:00:00"));
        latest.setAppointment_purpose("复诊");
        latest.setStatus("待治疗");

        Appointment older = new Appointment();
        older.setId(10);
        older.setPatient_name("张三");
        older.setAppointment_date(Date.valueOf("2026-04-20"));
        older.setAppointment_time(Time.valueOf("09:30:00"));
        older.setAppointment_purpose("洁牙");
        older.setStatus("已取消");

        appointmentService.result = List.of(latest, older);

        Result result = controller.getOverview(1L);

        assertEquals("200", result.getCode());
        Map<String, Object> payload = (Map<String, Object>) result.getData();
        List<Appointment> appointments = (List<Appointment>) payload.get("appointments");
        assertEquals(2, appointments.size());
        assertEquals(List.of(11, 10), appointments.stream().map(Appointment::getId).toList());
        assertTrue(payload.containsKey("appointments"));
    }

    @Test
    void getOverview_shouldIncludeWechatBindingStatusAndQrUrl() {
        Patient patient = new Patient();
        patient.setId(8);
        patient.setName("赵六");
        patient.setWechat_openid("");
        patientMapper.result = List.of(patient);
        wechatOAuthService.bindEntryUrl = "https://shuao.cc/wechat/bind/start?patientId=8&returnUrl=https%3A%2F%2Fsaas.shuao.cc%2Fpatient-portal-home%3FpatientId%3D8";

        Result result = controller.getOverview(8L);

        assertEquals("200", result.getCode());
        Map<String, Object> payload = (Map<String, Object>) result.getData();
        assertEquals(Boolean.FALSE, payload.get("wechatBound"));
        assertEquals("未绑定微信", payload.get("wechatBindStatusLabel"));
        assertEquals("", payload.get("wechatBindUrl"));
        assertEquals("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=ticket-8", payload.get("wechatFollowQrUrl"));
    }

    @Test
    void getOverview_shouldReturnBoundStatusWhenOpenidExists() {
        Patient patient = new Patient();
        patient.setId(9);
        patient.setName("孙七");
        patient.setWechat_openid("openid-patient-009");
        patientMapper.result = List.of(patient);

        Result result = controller.getOverview(9L);

        assertEquals("200", result.getCode());
        Map<String, Object> payload = (Map<String, Object>) result.getData();
        assertEquals(Boolean.TRUE, payload.get("wechatBound"));
        assertEquals("已绑定微信", payload.get("wechatBindStatusLabel"));
        assertEquals("", payload.get("wechatBindUrl"));
        assertEquals("", payload.get("wechatFollowQrUrl"));
    }

    static class StubPatientMapper implements PatientMapper {
        List<Patient> result = Collections.emptyList();
        @Override public List<Patient> selectAll() { return Collections.emptyList(); }
        @Override public List<Patient> selectById(Long id) { return result; }
        @Override public List<Patient> selectByName(String name) { return Collections.emptyList(); }
        @Override public List<Patient> selectByWechatOpenid(String openid) { return Collections.emptyList(); }
        @Override public List<Patient> selectByPhoneExact(String phone) { return Collections.emptyList(); }
        @Override public List<Patient> searchByKeyword(String keyword) { return Collections.emptyList(); }
        @Override public void addPatient(Patient patient) { }
        @Override public void updatePatient(Patient patient) { }
        @Override public void bindWechatOpenid(Patient patient) { }
        @Override public void updateCustomerSource(Long id, String customerSource) { }
        @Override public void deletePatient(int id) { }
        @Override public void deletePatientBatch(List<Long> ids) { }
        @Override public void clearRelatedPatientReference(Long relatedPatientId) { }
    }

    static class StubMedicalRecordService extends MedicalRecordService {
        @Override public List<MedicalRecord> selectByPatientId(Long patientId) { return Collections.emptyList(); }
    }

    static class StubPatientFollowupService extends PatientFollowupService {
        @Override public List<PatientFollowup> selectByPatientId(Long patientId) { return Collections.emptyList(); }
    }

    static class StubPatientRiskTagService extends PatientRiskTagService {
        @Override public List<PatientRiskTag> selectActiveByPatientId(Long patientId) { return Collections.emptyList(); }
    }

    static class StubPatientTimelineService extends PatientTimelineService {
        @Override public List<PatientTimeline> selectByPatientId(Long patientId) { return Collections.emptyList(); }
    }

    static class StubPatientImageService extends PatientImageService {
        @Override public List<PatientImage> selectByPatientId(Long patientId) { return Collections.emptyList(); }
    }

    static class StubTreatmentService extends TreatmentService {
        @Override public List<Treatment> selectByPatientReference(Long patientId) { return Collections.emptyList(); }
    }

    static class StubTreatmentBillingService extends TreatmentBillingService {
        StubTreatmentBillingService() {
            super(null, null, null);
        }
        @Override public void enrichTreatments(List<Treatment> treatments) { }
    }

    static class StubPatientConsentService extends PatientConsentService {
        StubPatientConsentService() {
            super(null, null, null);
        }
        @Override public List<com.example.springboot.entity.PatientConsent> selectByPatientId(Long patientId) { return Collections.emptyList(); }
    }

    static class StubAppointmentService extends AppointmentService {
        List<Appointment> result = Collections.emptyList();
        @Override public List<Appointment> selectPatientAppointments(Long patientId) { return result; }
    }

    static class StubWechatOAuthService extends WechatOAuthService {
        String bindEntryUrl = "";
        StubWechatOAuthService() {
            super("", "", "https://shuao.cc", "https://shuao.cc/app/bind-success");
        }
        @Override public String buildPatientBindRedirectUrl(Long patientId) { return "https://saas.shuao.cc/patient-portal-home?patientId=" + patientId; }
        @Override public String buildBindEntryUrl(Long patientId, String returnUrl) { return bindEntryUrl; }
        @Override public String buildAuthorizeUrl(Long patientId, String returnUrl) { return bindEntryUrl; }
    }

    static class StubWechatPatientBindSceneService extends WechatPatientBindSceneService {
        StubWechatPatientBindSceneService() {
            super(null, null, null, "wx-app", "wx-secret");
        }
        @Override public com.example.springboot.entity.PatientWechatBindScene ensureSceneForPatient(Long patientId) {
            com.example.springboot.entity.PatientWechatBindScene scene = new com.example.springboot.entity.PatientWechatBindScene();
            scene.setQr_url("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=ticket-" + patientId);
            return scene;
        }
    }
}
