package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.service.AppointmentService;
import com.example.springboot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/patient360")
public class Patient360Controller {

    private static final Logger log = LoggerFactory.getLogger(Patient360Controller.class);

    @Autowired private PatientMapper patientMapper;
    @Autowired private MedicalRecordService medicalRecordService;
    @Autowired private PatientFollowupService followupService;
    @Autowired private PatientRiskTagService riskTagService;
    @Autowired private PatientTimelineService timelineService;
    @Autowired private PatientImageService imageService;
    @Autowired private TreatmentService treatmentService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private TreatmentBillingService treatmentBillingService;
    @Autowired private PatientConsentService patientConsentService;
    @Autowired private PatientInsightSummaryService patientInsightSummaryService;
    @Autowired private PatientReferralRecordService patientReferralRecordService;
    @Autowired private ConsultationRecordService consultationRecordService;

    /**
     * 完整的患者360视图（向后兼容，一次性返回所有数据）
     */
    @GetMapping("/overview/{patientId}")
    public Result getOverview(@PathVariable Long patientId) {
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients.isEmpty()) return Result.error("患者不存在");
        Patient patient = patients.get(0);

        List<MedicalRecord> records = medicalRecordService.selectByPatientId(patientId);
        List<PatientFollowup> followups = followupService.selectByPatientId(patientId);
        List<PatientRiskTag> riskTags = riskTagService.selectActiveByPatientId(patientId);
        List<PatientTimeline> timeline = timelineService.selectByPatientId(patientId);
        List<Treatment> treatments = treatmentService.selectByPatientReference(patientId);
        treatmentBillingService.enrichTreatments(treatments);
        List<PatientImage> images = imageService.selectByPatientId(patientId);
        List<Appointment> appointments = appointmentService.selectPatientAppointments(patientId);
        List<PatientConsent> consents = patientConsentService.selectByPatientId(patientId);
        List<ConsultationRecord> consultations = consultationRecordService.selectByPatientId(patientId);
        PatientInsightSummary patientInsight = patientInsightSummaryService == null ? null : patientInsightSummaryService.getOrRefresh(patientId);
        PatientReferralRecord referralRecord = patientReferralRecordService == null ? null : patientReferralRecordService.selectByPatientId(patientId);

        double totalFee = patientInsight != null && patientInsight.getTotal_spent() != null
                ? patientInsight.getTotal_spent().doubleValue()
                : 0D;
        double arrearsAmount = treatments.stream()
                .map(Treatment::getArrears_amount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        Date lastVisit = patientInsight == null ? null : patientInsight.getLast_visit_date();
        Date nextFollowup = followups.stream()
                .map(PatientFollowup::getNext_followup_date)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patient", patient);
        result.put("visitCount", patientInsight == null || patientInsight.getTotal_visit_count() == null ? records.size() : patientInsight.getTotal_visit_count());
        result.put("lastVisit", lastVisit);
        result.put("nextFollowup", nextFollowup);
        result.put("totalFee", totalFee);
        result.put("hasArrears", arrearsAmount > 0.0001);
        result.put("arrearsAmount", arrearsAmount);
        result.put("patientInsight", patientInsight);
        result.put("referralRecord", referralRecord);
        result.put("riskTags", riskTags);
        List<MedicalRecord> recentRecords = records.size() > 5 ? records.subList(0, 5) : records;
        result.put("records", recentRecords);
        result.put("recentRecords", recentRecords);
        result.put("pendingLabOperationCount", records.stream()
                .map(MedicalRecord::getPending_lab_count)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        result.put("recentFollowups", followups.size() > 5 ? followups.subList(0, 5) : followups);
        result.put("timeline", timeline.size() > 30 ? timeline.subList(0, 30) : timeline);
        result.put("appointments", appointments);
        result.put("treatments", treatments);
        result.put("images", images);
        result.put("consents", consents);
        result.put("consultations", consultations);

        return Result.success(result);
    }

    /**
     * 患者基础信息 + 费用统计
     */
    @GetMapping("/basic/{patientId}")
    public Result getBasic(@PathVariable Long patientId) {
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients.isEmpty()) return Result.error("患者不存在");
        Patient patient = patients.get(0);

        PatientInsightSummary patientInsight = patientInsightSummaryService == null ? null : patientInsightSummaryService.getOrRefresh(patientId);
        List<Treatment> treatments = treatmentService.selectByPatientReference(patientId);
        treatmentBillingService.enrichTreatments(treatments);

        double totalFee = patientInsight != null && patientInsight.getTotal_spent() != null
                ? patientInsight.getTotal_spent().doubleValue()
                : 0D;
        double arrearsAmount = treatments.stream()
                .map(Treatment::getArrears_amount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patient", patient);
        result.put("visitCount", patientInsight == null || patientInsight.getTotal_visit_count() == null ? 0 : patientInsight.getTotal_visit_count());
        result.put("lastVisit", patientInsight == null ? null : patientInsight.getLast_visit_date());
        result.put("totalFee", totalFee);
        result.put("hasArrears", arrearsAmount > 0.0001);
        result.put("arrearsAmount", arrearsAmount);
        return Result.success(result);
    }

    /**
     * 病历列表 + 待处理化验单计数
     */
    @GetMapping("/medical-records/{patientId}")
    public Result getMedicalRecords(@PathVariable Long patientId) {
        List<MedicalRecord> records = medicalRecordService.selectByPatientId(patientId);
        List<MedicalRecord> recentRecords = records.size() > 5 ? records.subList(0, 5) : records;
        int pendingLabCount = records.stream()
                .map(MedicalRecord::getPending_lab_count)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("recentRecords", recentRecords);
        result.put("pendingLabOperationCount", pendingLabCount);
        return Result.success(result);
    }

    /**
     * 患者时间轴
     */
    @GetMapping("/timeline/{patientId}")
    public Result getTimeline(@PathVariable Long patientId) {
        List<PatientTimeline> timeline = timelineService.selectByPatientId(patientId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timeline", timeline.size() > 30 ? timeline.subList(0, 30) : timeline);
        return Result.success(result);
    }

    /**
     * 预约列表
     */
    @GetMapping("/appointments/{patientId}")
    public Result getAppointments(@PathVariable Long patientId) {
        List<Appointment> appointments = appointmentService.selectPatientAppointments(patientId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("appointments", appointments);
        return Result.success(result);
    }

    /**
     * 治疗处置列表（含费用信息）
     */
    @GetMapping("/treatments/{patientId}")
    public Result getTreatments(@PathVariable Long patientId) {
        List<Treatment> treatments = treatmentService.selectByPatientReference(patientId);
        treatmentBillingService.enrichTreatments(treatments);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("treatments", treatments);
        return Result.success(result);
    }

    /**
     * 患者影像列表
     */
    @GetMapping("/images/{patientId}")
    public Result getImages(@PathVariable Long patientId) {
        List<PatientImage> images = imageService.selectByPatientId(patientId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("images", images);
        return Result.success(result);
    }

    /**
     * 知情同意书列表
     */
    @GetMapping("/consents/{patientId}")
    public Result getConsents(@PathVariable Long patientId) {
        List<PatientConsent> consents = patientConsentService.selectByPatientId(patientId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("consents", consents);
        return Result.success(result);
    }

    /**
     * 随访记录 + 下次随访时间
     */
    @GetMapping("/followups/{patientId}")
    public Result getFollowups(@PathVariable Long patientId) {
        List<PatientFollowup> followups = followupService.selectByPatientId(patientId);
        Date nextFollowup = followups.stream()
                .map(PatientFollowup::getNext_followup_date)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("followups", followups);
        result.put("recentFollowups", followups.size() > 5 ? followups.subList(0, 5) : followups);
        result.put("nextFollowup", nextFollowup);
        return Result.success(result);
    }

    /**
     * 患者洞察摘要 + 转介绍记录
     */
    @GetMapping("/insight/{patientId}")
    public Result getInsight(@PathVariable Long patientId) {
        PatientInsightSummary patientInsight = patientInsightSummaryService == null ? null : patientInsightSummaryService.getOrRefresh(patientId);
        PatientReferralRecord referralRecord = patientReferralRecordService == null ? null : patientReferralRecordService.selectByPatientId(patientId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientInsight", patientInsight);
        result.put("referralRecord", referralRecord);
        return Result.success(result);
    }

    /**
     * 风险标签
     */
    @GetMapping("/risk-tags/{patientId}")
    public Result getRiskTags(@PathVariable Long patientId) {
        List<PatientRiskTag> riskTags = riskTagService.selectActiveByPatientId(patientId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("riskTags", riskTags);
        return Result.success(result);
    }
}
