package com.example.springboot.service;

import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.MedicalRecordMapper;
import com.example.springboot.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordMapper mapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private MedicalRecordOperationService medicalRecordOperationService;

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    public List<MedicalRecord> selectAll() {
        return selectAll(null, null, null, null);
    }

    public List<MedicalRecord> selectAll(Long doctorAccountId, String recordStatus, String startDate, String endDate) {
        List<MedicalRecord> records = mapper.selectAllWithFilter(doctorAccountId, recordStatus, startDate, endDate);
        medicalRecordOperationService.enrichMedicalRecords(records, false);
        return records;
    }

    public List<MedicalRecord> selectByPatientId(Long patientId) {
        List<MedicalRecord> records = mapper.selectByPatientId(patientId);
        medicalRecordOperationService.enrichMedicalRecords(records, false);
        return records;
    }

    public List<MedicalRecord> selectByPatientName(String name) {
        List<MedicalRecord> records = mapper.selectByPatientName(name);
        medicalRecordOperationService.enrichMedicalRecords(records, false);
        return records;
    }

    public MedicalRecord selectById(Long id) {
        MedicalRecord record = mapper.selectById(id);
        medicalRecordOperationService.enrichMedicalRecord(record, true);
        return record;
    }

    @Transactional
    public void add(MedicalRecord record) {
        populatePatientReference(record);
        normalizeForSave(record);
        mapper.insert(record);
        medicalRecordOperationService.replaceByMedicalRecord(record.getId(), record.getOperation_items(), record.getDoctor_account_id(), record.getDoctor_name());
        medicalRecordOperationService.syncLabOrdersForMedicalRecord(record);
        medicalRecordOperationService.enrichMedicalRecord(record, true);
        refreshPatientInsight(record.getPatient_id());
    }

    @Transactional
    public void update(MedicalRecord record) {
        populatePatientReference(record);
        normalizeForSave(record);
        mapper.update(record);
        if (record.getOperation_items() != null) {
            medicalRecordOperationService.replaceByMedicalRecord(record.getId(), record.getOperation_items(), record.getDoctor_account_id(), record.getDoctor_name());
        }
        medicalRecordOperationService.syncLabOrdersForMedicalRecord(record);
        medicalRecordOperationService.enrichMedicalRecord(record, true);
        refreshPatientInsight(record.getPatient_id());
    }

    @Transactional
    public void delete(Long id) {
        Long patientId = null;
        MedicalRecord existing = id == null ? null : mapper.selectById(id);
        if (existing != null) {
            patientId = existing.getPatient_id();
        }
        if (id != null && id > 0) {
            medicalRecordOperationService.replaceByMedicalRecord(id, List.of(), null, null);
        }
        mapper.deleteById(id);
        refreshPatientInsight(patientId);
    }

    @Transactional
    public void deleteByPatientId(Long patientId) {
        List<MedicalRecord> records = patientId == null ? List.of() : mapper.selectByPatientId(patientId);
        for (MedicalRecord record : records) {
            if (record != null && record.getId() != null) {
                medicalRecordOperationService.replaceByMedicalRecord(record.getId(), List.of(), null, null);
            }
        }
        mapper.deleteByPatientId(patientId);
    }

    private void refreshPatientInsight(Long patientId) {
        if (patientInsightSummaryService != null && patientId != null && patientId > 0) {
            patientInsightSummaryService.refreshPatients(List.of(patientId));
        }
    }

    private void populatePatientReference(MedicalRecord record) {
        if (record == null) {
            return;
        }
        if (patientMapper == null) {
            if (StringUtils.hasText(record.getPatient_name())) {
                record.setPatient_name(record.getPatient_name().trim());
            }
            return;
        }
        if (record.getPatient_id() != null && record.getPatient_id() > 0) {
            List<Patient> patients = patientMapper.selectById(record.getPatient_id());
            if (patients != null && !patients.isEmpty() && patients.get(0) != null) {
                Patient patient = patients.get(0);
                record.setPatient_id((long) patient.getId());
                if (StringUtils.hasText(patient.getName())) {
                    record.setPatient_name(patient.getName().trim());
                }
                return;
            }
        }
    }

    private void normalizeForSave(MedicalRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("病历数据不能为空");
        }
        record.setPatient_name(trimToEmpty(record.getPatient_name()));
        record.setNurse_name(trimToEmpty(record.getNurse_name()));
        record.setAssistant_name(trimToEmpty(record.getAssistant_name()));
        record.setRecord_type(normalizeRecordType(record.getRecord_type()));
        record.setChief_complaint(trimToEmpty(record.getChief_complaint()));
        record.setPresent_illness_history(trimToEmpty(record.getPresent_illness_history()));
        record.setPast_history(trimToEmpty(record.getPast_history()));
        record.setInfectious_history(trimToEmpty(record.getInfectious_history()));
        record.setAllergy_history(trimToEmpty(record.getAllergy_history()));
        record.setGeneral_condition(trimToEmpty(record.getGeneral_condition()));
        record.setExamination(trimToEmpty(record.getExamination()));
        record.setAuxiliary_examination(trimToEmpty(record.getAuxiliary_examination()));
        record.setDiagnosis(trimToEmpty(record.getDiagnosis()));
        record.setTreatment_plan(trimToEmpty(record.getTreatment_plan()));
        record.setTreatment(trimToEmpty(record.getTreatment()));
        record.setMedical_advice(trimToEmpty(record.getMedical_advice()));
        record.setPrescription(trimToEmpty(record.getPrescription()));
        record.setRecord_tags(trimToEmpty(record.getRecord_tags()));
        record.setImage_summary(trimToEmpty(record.getImage_summary()));
        record.setNotes(trimToEmpty(record.getNotes()));
        record.setRecord_status(normalizeRecordStatus(record.getRecord_status()));
        record.setTooth_positions(normalizeToothPositions(record.getTooth_positions()));

        if (record.getDoctor_account_id() == null || record.getDoctor_account_id() <= 0) {
            Long doctorAccountId = accountService.findDoctorAccountIdByName(record.getDoctor_name());
            if (doctorAccountId != null && doctorAccountId > 0) {
                record.setDoctor_account_id(doctorAccountId);
            }
        }
        if (record.getPatient_id() == null || record.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID必填");
        }
        if (record.getDoctor_account_id() == null || record.getDoctor_account_id() <= 0) {
            throw new IllegalArgumentException("医生账号必填");
        }
        String doctorName = accountService.findDoctorDisplayNameByAccountId(record.getDoctor_account_id());
        if (doctorName == null || doctorName.trim().isEmpty()) {
            throw new IllegalArgumentException("医生账号不存在或未启用");
        }
        record.setDoctor_name(doctorName.trim());

        if (!record.getTreatment().isEmpty() && record.getTooth_positions().isEmpty()) {
            throw new IllegalArgumentException("请选择牙位");
        }
        if (!record.getTreatment().isEmpty() && !record.getTooth_positions().isEmpty()) {
            record.setTreatment(appendToothSuffix(record.getTreatment(), record.getTooth_positions()));
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeToothPositions(String value) {
        if (value == null) {
            return "";
        }
        String[] parts = value.split(",");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String text = part == null ? "" : part.trim();
            if (text.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(text);
        }
        return builder.toString();
    }

    private String appendToothSuffix(String treatment, String toothPositions) {
        String suffix = "（牙位：" + toothPositions + "）";
        if (treatment.endsWith(suffix)) {
            return treatment;
        }
        return treatment + suffix;
    }

    private String normalizeRecordType(String value) {
        String normalized = trimToEmpty(value);
        return normalized.isEmpty() ? "初诊" : normalized;
    }

    private String normalizeRecordStatus(String value) {
        String normalized = trimToEmpty(value);
        return normalized.isEmpty() ? "final" : normalized;
    }
}
