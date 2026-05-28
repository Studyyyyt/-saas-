package com.example.springboot.service;

import com.example.springboot.entity.ConsultationRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientReferralRecord;
import com.example.springboot.mapper.ConsultationRecordMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.PatientReferralRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PatientReferralRecordService {

    private final PatientReferralRecordMapper patientReferralRecordMapper;
    private final PatientMapper patientMapper;
    private final ConsultationRecordMapper consultationRecordMapper;

    @Autowired
    public PatientReferralRecordService(PatientReferralRecordMapper patientReferralRecordMapper,
                                        PatientMapper patientMapper,
                                        ConsultationRecordMapper consultationRecordMapper) {
        this.patientReferralRecordMapper = patientReferralRecordMapper;
        this.patientMapper = patientMapper;
        this.consultationRecordMapper = consultationRecordMapper;
    }

    public PatientReferralRecord selectByPatientId(Long patientId) {
        return patientId == null || patientId <= 0 ? null : patientReferralRecordMapper.selectByPatientId(patientId);
    }

    public List<PatientReferralRecord> selectByPatientIds(List<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return List.of();
        }
        return patientReferralRecordMapper.selectByPatientIds(patientIds);
    }

    public List<PatientReferralRecord> selectByReferrerPatientId(Long referrerPatientId) {
        return referrerPatientId == null || referrerPatientId <= 0
                ? List.of()
                : patientReferralRecordMapper.selectByReferrerPatientId(referrerPatientId);
    }

    public List<PatientReferralRecord> selectAll() {
        return patientReferralRecordMapper.selectAll();
    }

    public void deleteByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        patientReferralRecordMapper.deleteByPatientId(patientId);
    }

    public PatientReferralRecord syncFromPatient(Patient patient) {
        if (patient == null || patient.getId() <= 0) {
            return null;
        }
        Long patientId = (long) patient.getId();
        PatientReferralRecord snapshot = buildFromPatient(patient);
        if (snapshot == null) {
            if (!"转介绍".equals(trim(patient.getCustomer_source()))) {
                patientReferralRecordMapper.deleteByPatientId(patientId);
            }
            return null;
        }
        if (snapshot.getReferrer_patient_id() != null && snapshot.getReferrer_patient_id().equals(patientId)) {
            throw new IllegalArgumentException("介绍患者不能是本人");
        }
        snapshot.setPatient_id(patientId);
        snapshot.setConsultation_record_id(normalizePositiveId(patient.getConsultation_record_id()));
        patientReferralRecordMapper.upsert(snapshot);
        return patientReferralRecordMapper.selectByPatientId(patientId);
    }

    public PatientReferralRecord syncFromConsultation(ConsultationRecord record) {
        if (record == null || record.getPatient_id() == null || record.getPatient_id() <= 0) {
            return null;
        }
        Long patientId = record.getPatient_id();
        PatientReferralRecord snapshot = buildFromConsultation(record);
        PatientReferralRecord existing = patientReferralRecordMapper.selectByPatientId(patientId);
        if (snapshot == null) {
            if (existing != null && record.getId() != null && record.getId().equals(existing.getConsultation_record_id())) {
                patientReferralRecordMapper.deleteByPatientId(patientId);
            }
            return null;
        }
        if (snapshot.getReferrer_patient_id() != null && snapshot.getReferrer_patient_id().equals(patientId)) {
            throw new IllegalArgumentException("介绍患者不能是本人");
        }
        snapshot.setPatient_id(patientId);
        snapshot.setConsultation_record_id(record.getId());
        patientReferralRecordMapper.upsert(snapshot);
        return patientReferralRecordMapper.selectByPatientId(patientId);
    }

    public PatientReferralRecord syncFromConsultationId(Long consultationId, Long patientId) {
        if (consultationId == null || consultationId <= 0 || patientId == null || patientId <= 0) {
            return null;
        }
        ConsultationRecord record = consultationRecordMapper.selectById(consultationId);
        if (record == null) {
            return null;
        }
        record.setPatient_id(patientId);
        return syncFromConsultation(record);
    }

    public Set<Long> collectAffectedPatientIds(Collection<Long> patientIds) {
        Set<Long> result = new LinkedHashSet<>();
        if (patientIds == null) {
            return result;
        }
        List<Long> normalizedIds = patientIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        result.addAll(normalizedIds);
        if (normalizedIds.isEmpty()) {
            return result;
        }
        for (PatientReferralRecord record : selectByPatientIds(normalizedIds)) {
            Long referrerPatientId = normalizePositiveId(record == null ? null : record.getReferrer_patient_id());
            if (referrerPatientId != null) {
                result.add(referrerPatientId);
            }
        }
        return result;
    }

    private PatientReferralRecord buildFromPatient(Patient patient) {
        if (patient == null) {
            return null;
        }
        PatientReferralRecord record = new PatientReferralRecord();
        record.setReferrer_type(trim(patient.getReferrer_type()));
        record.setReferrer_patient_id(normalizePositiveId(patient.getReferrer_patient_id()));
        record.setReferrer_patient_name(trim(patient.getReferrer_patient_name()));
        record.setExternal_referrer_type(trim(patient.getExternal_referrer_type()));
        record.setExternal_referrer_name(trim(patient.getExternal_referrer_name()));
        record.setExternal_referrer_contact(trim(patient.getExternal_referrer_contact()));
        record.setRemark(trim(patient.getReferral_remark()));
        return normalizeSnapshot(record);
    }

    private PatientReferralRecord buildFromConsultation(ConsultationRecord consultationRecord) {
        if (consultationRecord == null) {
            return null;
        }
        if (!"转介绍".equals(trim(consultationRecord.getConsultation_channel()))) {
            return null;
        }
        PatientReferralRecord record = new PatientReferralRecord();
        record.setReferrer_type(trim(consultationRecord.getReferrer_type()));
        record.setReferrer_patient_id(normalizePositiveId(consultationRecord.getReferrer_patient_id()));
        record.setReferrer_patient_name(trim(consultationRecord.getReferrer_patient_name()));
        record.setExternal_referrer_type(trim(consultationRecord.getExternal_referrer_type()));
        record.setExternal_referrer_name(trim(consultationRecord.getExternal_referrer_name()));
        record.setExternal_referrer_contact(trim(consultationRecord.getExternal_referrer_contact()));
        record.setRemark(trim(consultationRecord.getRemarks()));
        record.setCreated_by(normalizePositiveId(consultationRecord.getCreated_by()));
        record.setCreated_by_name(trim(consultationRecord.getCreated_by_name()));
        return normalizeSnapshot(record);
    }

    private PatientReferralRecord normalizeSnapshot(PatientReferralRecord record) {
        if (record == null) {
            return null;
        }
        boolean hasInternal = normalizePositiveId(record.getReferrer_patient_id()) != null
                || StringUtils.hasText(trim(record.getReferrer_patient_name()));
        boolean hasExternal = StringUtils.hasText(trim(record.getExternal_referrer_name()))
                || StringUtils.hasText(trim(record.getExternal_referrer_contact()))
                || StringUtils.hasText(trim(record.getExternal_referrer_type()));
        if (!hasInternal && !hasExternal) {
            return null;
        }
        if (hasInternal && hasExternal) {
            throw new IllegalArgumentException("介绍人不能同时选择内部患者和外部介绍人");
        }

        if (hasInternal) {
            Long referrerPatientId = normalizePositiveId(record.getReferrer_patient_id());
            if (referrerPatientId == null) {
                throw new IllegalArgumentException("内部介绍人必须选择有效患者");
            }
            List<Patient> referrerPatients = patientMapper.selectById(referrerPatientId);
            if (referrerPatients == null || referrerPatients.isEmpty() || referrerPatients.get(0) == null) {
                throw new IllegalArgumentException("介绍患者不存在");
            }
            Patient referrerPatient = referrerPatients.get(0);
            record.setReferrer_type("patient");
            record.setReferrer_patient_id((long) referrerPatient.getId());
            record.setReferrer_patient_name(trim(referrerPatient.getName()));
            record.setExternal_referrer_type(null);
            record.setExternal_referrer_name(null);
            record.setExternal_referrer_contact(null);
            return record;
        }

        String externalName = trim(record.getExternal_referrer_name());
        if (!StringUtils.hasText(externalName)) {
            throw new IllegalArgumentException("外部介绍人姓名不能为空");
        }
        record.setReferrer_type("external");
        record.setReferrer_patient_id(null);
        record.setReferrer_patient_name(null);
        record.setExternal_referrer_type(trim(record.getExternal_referrer_type()));
        record.setExternal_referrer_name(externalName);
        record.setExternal_referrer_contact(trim(record.getExternal_referrer_contact()));
        return record;
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
