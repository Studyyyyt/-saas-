package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientFollowup;
import com.example.springboot.mapper.PatientFollowupMapper;
import com.example.springboot.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PatientFollowupService {

    @Autowired
    private PatientFollowupMapper mapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    public List<PatientFollowup> selectAll() { return mapper.selectAll(); }
    public List<PatientFollowup> selectAllDetail() { return mapper.selectAllDetail(); }
    public List<PatientFollowup> selectByPatientId(Long patientId) { return mapper.selectByPatientId(patientId); }
    public PatientFollowup selectById(Long id) { return mapper.selectById(id); }
    public void add(PatientFollowup f) {
        normalizeFollowupForSave(f, false);
        mapper.insert(f);
        refreshPatientInsight(f.getPatient_id());
    }
    public void update(PatientFollowup f) {
        normalizeFollowupForSave(f, true);
        mapper.update(f);
        refreshPatientInsight(f.getPatient_id());
    }
    public void delete(Long id) {
        PatientFollowup existing = id == null ? null : mapper.selectById(id);
        mapper.deleteById(id);
        refreshPatientInsight(existing == null ? null : existing.getPatient_id());
    }
    public void deleteByPatientId(Long patientId) {
        mapper.deleteByPatientId(patientId);
        refreshPatientInsight(patientId);
    }

    private void normalizeFollowupForSave(PatientFollowup followup, boolean requireId) {
        if (followup == null) {
            throw new IllegalArgumentException("回访信息不能为空");
        }
        if (requireId && (followup.getId() == null || followup.getId() <= 0)) {
            throw new IllegalArgumentException("回访记录ID非法");
        }
        if (followup.getPatient_id() == null || followup.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        List<Patient> patients = patientMapper == null ? List.of() : patientMapper.selectById(followup.getPatient_id());
        if (patients == null || patients.isEmpty()) {
            throw new IllegalArgumentException("患者不存在");
        }
        if (followup.getFollowup_date() == null) {
            throw new IllegalArgumentException("计划回访时间不能为空");
        }

        String followupType = StringUtils.hasText(followup.getFollowup_type()) ? followup.getFollowup_type().trim() : "电话";
        followup.setFollowup_type(followupType);

        if (StringUtils.hasText(followup.getSummary())) {
            followup.setSummary(followup.getSummary().trim());
        } else {
            followup.setSummary(null);
        }

        Long doctorAccountId = followup.getDoctor_account_id();
        String doctorName = StringUtils.hasText(followup.getDoctor_name()) ? followup.getDoctor_name().trim() : "";

        if (doctorAccountId != null && doctorAccountId > 0 && accountService != null) {
            String displayName = accountService.findDoctorDisplayNameByAccountId(doctorAccountId);
            if (StringUtils.hasText(displayName)) {
                doctorName = displayName.trim();
            }
        }

        if (!StringUtils.hasText(doctorName)) {
            Patient patient = patients.get(0);
            if (patient != null && StringUtils.hasText(patient.getLatest_visit_doctor())) {
                doctorName = patient.getLatest_visit_doctor().trim();
            }
        }

        if (!StringUtils.hasText(doctorName)) {
            throw new IllegalArgumentException("负责医生不能为空");
        }

        if ((doctorAccountId == null || doctorAccountId <= 0) && accountService != null) {
            Long matchedDoctorAccountId = accountService.findDoctorAccountIdByName(doctorName);
            if (matchedDoctorAccountId != null && matchedDoctorAccountId > 0) {
                doctorAccountId = matchedDoctorAccountId;
            }
        }

        followup.setDoctor_account_id(doctorAccountId);
        followup.setDoctor_name(doctorName);
    }

    private void refreshPatientInsight(Long patientId) {
        if (patientInsightSummaryService != null && patientId != null && patientId > 0) {
            patientInsightSummaryService.refreshPatients(List.of(patientId));
        }
    }
}
