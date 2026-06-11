package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientReferralRecord;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.LabOrderMapper;
import com.example.springboot.mapper.MedicalRecordMapper;
import com.example.springboot.mapper.PatientConsentMapper;
import com.example.springboot.mapper.PatientImageMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import com.example.springboot.util.PatientSearchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PatientService {

    private final PatientMapper patientMapper;
    private final TreatmentBillingService treatmentBillingService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PatientFollowupService patientFollowupService;

    @Autowired
    private PatientRiskTagService patientRiskTagService;

    @Autowired
    private PatientTimelineService patientTimelineService;

    @Autowired
    private PatientImageService patientImageService;

    @Autowired
    private PatientConsentService patientConsentService;

    @Autowired
    private ConsultationRecordService consultationRecordService;

    @Autowired
    private LabOrderService labOrderService;

    @Autowired
    private PatientReferralRecordService patientReferralRecordService;

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private TreatmentMapper treatmentMapper;

    @Autowired
    private PatientImageMapper patientImageMapper;

    @Autowired
    private PatientConsentMapper patientConsentMapper;

    @Autowired
    private LabOrderMapper labOrderMapper;

    @Autowired
    public PatientService(PatientMapper patientMapper, TreatmentBillingService treatmentBillingService) {
        this.patientMapper = patientMapper;
        this.treatmentBillingService = treatmentBillingService;
    }

    public PatientService(PatientMapper patientMapper) {
        this(patientMapper, null);
    }

    public List<Patient> selectAll() {
        List<Patient> patients = patientMapper.selectAll();
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichPatients(patients);
        }
        return patients;
    }

    public List<Patient> selectById(Long id) {
        List<Patient> patients = patientMapper.selectById(id);
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichPatients(patients);
        }
        return patients;
    }

    public List<Patient> selectByName(String name) {
        return searchPatients(name);
    }

    public List<Patient> searchPatients(String keyword) {
        String normalizedKeyword = PatientSearchUtils.normalizeKeyword(keyword);
        if (!StringUtils.hasText(normalizedKeyword)) {
            List<Patient> patients = patientMapper.selectAll();
            if (treatmentBillingService != null) {
                treatmentBillingService.enrichPatients(patients);
            }
            return patients;
        }
        List<Patient> matched = patientMapper.searchByKeyword(normalizedKeyword);
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichPatients(matched);
        }
        return matched;
    }

    /**
     * 多条件组合查询患者
     *
     * @param keyword 搜索关键词
     * @param gender 性别
     * @param ageMin 最小年龄
     * @param ageMax 最大年龄
     * @param customerSource 客户来源
     * @param hasArrears 是否有欠款
     * @return 患者列表
     */
    public List<Patient> searchPatients(String keyword, String gender, Integer ageMin, Integer ageMax, String customerSource, Boolean hasArrears) {
        List<Patient> patients = patientMapper.searchPatients(keyword, gender, ageMin, ageMax, customerSource);
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichPatients(patients);
        }
        if (hasArrears != null && patients != null) {
            patients = patients.stream()
                    .filter(p -> p != null && hasArrears.equals(p.getHas_arrears()))
                    .toList();
        }
        return patients;
    }

    public Patient selectByWechatOpenid(String openid) {
        List<Patient> patients = patientMapper.selectByWechatOpenid(openid);
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichPatients(patients);
        }
        return (patients == null || patients.isEmpty()) ? null : patients.get(0);
    }

    public List<Patient> selectByPhoneExact(String phone) {
        return StringUtils.hasText(phone) ? patientMapper.selectByPhoneExact(phone.trim()) : List.of();
    }

    @Transactional
    public void addPatient(Patient patient) {
        if (StringUtils.hasText(patient.getPhone())) {
            List<Patient> existing = patientMapper.selectByPhoneExact(patient.getPhone().trim());
            if (existing != null && !existing.isEmpty()) {
                throw new IllegalArgumentException("该手机号已存在，请勿重复创建患者");
            }
        }
        normalizePatientCustomerSource(patient, true);
        normalizePatientSearchTokens(patient);
        patientMapper.addPatient(patient);
        if (patient != null
                && patient.getConsultation_record_id() != null
                && patient.getConsultation_record_id() > 0
                && consultationRecordService != null) {
            consultationRecordService.linkPatientForArchiveCreate(
                    patient.getConsultation_record_id(),
                    (long) patient.getId(),
                    patient.getCustomer_source()
            );
        }
        syncReferralAndInsights(patient, null);
    }

    @Transactional
    public void updatePatient(Patient patient) {
        Long previousReferrerPatientId = null;
        List<Patient> existingPatients = patient == null || patient.getId() <= 0 ? List.of() : patientMapper.selectById((long) patient.getId());
        if (existingPatients != null && !existingPatients.isEmpty() && existingPatients.get(0) != null) {
            previousReferrerPatientId = existingPatients.get(0).getReferrer_patient_id();
        }
        normalizePatientCustomerSource(patient, false);
        normalizePatientSearchTokens(patient);
        patientMapper.updatePatient(patient);
        cascadeUpdatePatientName(patient);
        if (patient != null
                && patient.getConsultation_record_id() != null
                && patient.getConsultation_record_id() > 0
                && consultationRecordService != null) {
            consultationRecordService.linkPatientForArchiveCreate(
                    patient.getConsultation_record_id(),
                    (long) patient.getId(),
                    patient.getCustomer_source()
            );
        }
        syncReferralAndInsights(patient, previousReferrerPatientId);
    }

    public Patient bindWechatOpenid(Long id, String wechatOpenid) {
        List<Patient> patients = patientMapper.selectById(id);
        if (patients == null || patients.isEmpty()) {
            return null;
        }
        Patient patient = patients.get(0);
        patient.setWechat_openid(wechatOpenid);
        patientMapper.bindWechatOpenid(patient);
        return patient;
    }

    @Transactional
    public void deletePatient(int id) {
        List<Patient> patients = patientMapper.selectById((long) id);
        if (patients == null || patients.isEmpty() || patients.get(0) == null) {
            patientMapper.deletePatient(id);
            return;
        }
        Patient patient = patients.get(0);
        Long patientId = (long) patient.getId();

        patientMapper.clearRelatedPatientReference(patientId);
        appointmentService.deleteByPatientReference(patientId);
        treatmentService.deleteByPatientReference(patientId);
        financeService.deleteByPatientId(patientId);
        if (labOrderService != null) {
            labOrderService.deleteByPatientId(patientId);
        }
        if (consultationRecordService != null) {
            consultationRecordService.clearPatientLinkByPatientId(patientId);
        }
        if (patientInsightSummaryService != null) {
            patientInsightSummaryService.deleteByPatientId(patientId);
        }
        if (patientReferralRecordService != null) {
            patientReferralRecordService.deleteByPatientId(patientId);
        }
        medicalRecordService.deleteByPatientId(patientId);
        patientFollowupService.deleteByPatientId(patientId);
        patientRiskTagService.deleteByPatientId(patientId);
        patientTimelineService.deleteByPatientId(patientId);
        patientImageService.deleteByPatientId(patientId);
        patientConsentService.deleteByPatientId(patientId);
        patientMapper.deletePatient(id);
    }

    @Transactional
    public void deletePatientBatch(List<Long> ids) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            if (id != null) {
                deletePatient(id.intValue());
            }
        }
    }

    private void normalizePatientCustomerSource(Patient patient, boolean requireCustomerSource) {
        if (patient == null) {
            return;
        }
        if (hasReferralPayload(patient)) {
            patient.setCustomer_source("转介绍");
            return;
        }
        String normalizedSource = patient.getCustomer_source() == null ? "" : patient.getCustomer_source().trim();
        if (!StringUtils.hasText(normalizedSource)) {
            if (requireCustomerSource) {
                throw new IllegalArgumentException("客户来源必填");
            }
            if (patient.getId() > 0) {
                List<Patient> existingPatients = patientMapper.selectById((long) patient.getId());
                if (existingPatients != null && !existingPatients.isEmpty() && existingPatients.get(0) != null) {
                    patient.setCustomer_source(existingPatients.get(0).getCustomer_source());
                    return;
                }
            }
            patient.setCustomer_source(null);
            return;
        }
        if (!ConsultationRecordService.CUSTOMER_SOURCE_OPTIONS.contains(normalizedSource)) {
            throw new IllegalArgumentException("客户来源不合法");
        }
        patient.setCustomer_source(normalizedSource);
    }

    private void normalizePatientSearchTokens(Patient patient) {
        if (patient == null) {
            return;
        }
        String pinyin = PatientSearchUtils.normalizeKeyword(PatientSearchUtils.toPinyin(patient.getName()));
        String initials = PatientSearchUtils.normalizeKeyword(PatientSearchUtils.toInitials(patient.getName()));
        patient.setName_pinyin(StringUtils.hasText(pinyin) ? pinyin : null);
        patient.setName_initials(StringUtils.hasText(initials) ? initials : null);
    }

    private void cascadeUpdatePatientName(Patient patient) {
        if (patient == null || patient.getId() <= 0 || !StringUtils.hasText(patient.getName())) {
            return;
        }
        Long patientId = (long) patient.getId();
        String patientName = patient.getName().trim();
        if (appointmentMapper != null) {
            appointmentMapper.updatePatientNameByPatientId(patientId, patientName);
        }
        if (medicalRecordMapper != null) {
            medicalRecordMapper.updatePatientNameByPatientId(patientId, patientName);
        }
        if (treatmentMapper != null) {
            treatmentMapper.updatePatientNameByPatientId(patientId, patientName);
        }
        if (patientImageMapper != null) {
            patientImageMapper.updatePatientNameByPatientId(patientId, patientName);
        }
        if (patientConsentMapper != null) {
            patientConsentMapper.updatePatientNameByPatientId(patientId, patientName);
        }
        if (labOrderMapper != null) {
            labOrderMapper.updatePatientNameByPatientId(patientId, patientName);
        }
    }

    private void syncReferralAndInsights(Patient patient, Long previousReferrerPatientId) {
        if (patient == null || patient.getId() <= 0) {
            return;
        }
        Set<Long> affectedPatientIds = new LinkedHashSet<>();
        affectedPatientIds.add((long) patient.getId());
        if (previousReferrerPatientId != null && previousReferrerPatientId > 0) {
            affectedPatientIds.add(previousReferrerPatientId);
        }

        PatientReferralRecord referralRecord = null;
        if (patientReferralRecordService != null) {
            if (hasReferralPayload(patient)) {
                referralRecord = patientReferralRecordService.syncFromPatient(patient);
            } else if (patient.getConsultation_record_id() != null && patient.getConsultation_record_id() > 0) {
                referralRecord = patientReferralRecordService.syncFromConsultationId(patient.getConsultation_record_id(), (long) patient.getId());
            } else if (!"转介绍".equals(StringUtils.hasText(patient.getCustomer_source()) ? patient.getCustomer_source().trim() : "")) {
                patientReferralRecordService.deleteByPatientId((long) patient.getId());
            }
        }
        if (referralRecord != null && referralRecord.getReferrer_patient_id() != null && referralRecord.getReferrer_patient_id() > 0) {
            affectedPatientIds.add(referralRecord.getReferrer_patient_id());
        }
        if (patientInsightSummaryService != null && !affectedPatientIds.isEmpty()) {
            patientInsightSummaryService.refreshPatients(affectedPatientIds);
        }
    }

    private boolean hasReferralPayload(Patient patient) {
        if (patient == null) {
            return false;
        }
        return patient.getReferrer_patient_id() != null && patient.getReferrer_patient_id() > 0
                || StringUtils.hasText(patient.getReferrer_patient_name())
                || StringUtils.hasText(patient.getExternal_referrer_type())
                || StringUtils.hasText(patient.getExternal_referrer_name())
                || StringUtils.hasText(patient.getExternal_referrer_contact())
                || StringUtils.hasText(patient.getReferrer_type())
                || StringUtils.hasText(patient.getReferral_remark());
    }
}
