package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientConsent;
import com.example.springboot.entity.PatientConsentSignRequest;
import com.example.springboot.entity.PatientTimeline;
import com.example.springboot.mapper.PatientConsentMapper;
import com.example.springboot.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class PatientConsentService {

    private final PatientConsentMapper patientConsentMapper;
    private final PatientMapper patientMapper;
    private final PatientTimelineService patientTimelineService;

    @Autowired
    public PatientConsentService(PatientConsentMapper patientConsentMapper,
                                 PatientMapper patientMapper,
                                 PatientTimelineService patientTimelineService) {
        this.patientConsentMapper = patientConsentMapper;
        this.patientMapper = patientMapper;
        this.patientTimelineService = patientTimelineService;
    }

    public List<PatientConsent> selectByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return List.of();
        }
        return patientConsentMapper.selectByPatientId(patientId);
    }

    public PatientConsent selectById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return patientConsentMapper.selectById(id);
    }

    @Transactional
    public PatientConsent issue(PatientConsent consent) {
        if (consent == null) {
            throw new IllegalArgumentException("知情同意书内容不能为空");
        }
        if (consent.getPatient_id() == null || consent.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        if (!StringUtils.hasText(consent.getTitle())) {
            throw new IllegalArgumentException("同意书标题不能为空");
        }
        if (!StringUtils.hasText(consent.getContent())) {
            throw new IllegalArgumentException("同意书正文不能为空");
        }

        Patient patient = resolvePatient(consent.getPatient_id());
        consent.setPatient_name(patient == null ? trim(consent.getPatient_name()) : trim(patient.getName()));
        consent.setDoctor_name(defaultDoctorName(consent.getDoctor_name()));
        consent.setTitle(trim(consent.getTitle()));
        consent.setContent(trim(consent.getContent()));
        consent.setStatus("待签署");
        consent.setIssued_at(new Date());
        consent.setRead_at(null);
        consent.setSigned_at(null);
        consent.setSignature_name("");
        consent.setSignature_data("");
        consent.setSignature_remark("");

        patientConsentMapper.insert(consent);
        addIssueTimeline(consent);
        return patientConsentMapper.selectById(consent.getId());
    }

    @Transactional
    public PatientConsent markRead(Long id, Long patientId) {
        PatientConsent consent = requireAuthorizedConsent(id, patientId);
        if (consent.getRead_at() == null) {
            Date now = new Date();
            patientConsentMapper.updateReadAt(id, now);
            consent = patientConsentMapper.selectById(id);
        }
        return consent;
    }

    @Transactional
    public PatientConsent sign(Long id, Long patientId, PatientConsentSignRequest request) {
        PatientConsent consent = requireAuthorizedConsent(id, patientId);
        if ("已签署".equals(trim(consent.getStatus())) && consent.getSigned_at() != null) {
            return consent;
        }
        if (request == null || !StringUtils.hasText(request.getSignature_data())) {
            throw new IllegalArgumentException("请先完成签名");
        }
        String signatureData = request.getSignature_data().trim();
        if (!signatureData.startsWith("data:image/")) {
            throw new IllegalArgumentException("签名图片格式无效");
        }

        Date now = new Date();
        consent.setStatus("已签署");
        consent.setRead_at(consent.getRead_at() == null ? now : consent.getRead_at());
        consent.setSigned_at(now);
        consent.setSignature_name(StringUtils.hasText(request.getSignature_name()) ? request.getSignature_name().trim() : consent.getPatient_name());
        consent.setSignature_data(signatureData);
        consent.setSignature_remark(trim(request.getSignature_remark()));

        patientConsentMapper.updateSigned(consent);
        addSignedTimeline(consent);
        return patientConsentMapper.selectById(id);
    }

    public void deleteByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        patientConsentMapper.deleteByPatientId(patientId);
    }

    private PatientConsent requireAuthorizedConsent(Long id, Long patientId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("知情同意书不存在");
        }
        PatientConsent consent = patientConsentMapper.selectById(id);
        if (consent == null || consent.getPatient_id() == null || !consent.getPatient_id().equals(patientId)) {
            throw new IllegalArgumentException("知情同意书不存在或无权访问");
        }
        return consent;
    }

    private Patient resolvePatient(Long patientId) {
        List<Patient> patients = patientMapper.selectById(patientId);
        return patients == null || patients.isEmpty() ? null : patients.get(0);
    }

    private void addIssueTimeline(PatientConsent consent) {
        if (consent == null || consent.getPatient_id() == null) {
            return;
        }
        PatientTimeline timeline = new PatientTimeline();
        timeline.setPatient_id(consent.getPatient_id());
        timeline.setEvent_time(new Date());
        timeline.setEvent_type("知情同意");
        timeline.setEvent_title("下发电子知情同意书");
        timeline.setEvent_content("已向患者下发《" + trim(consent.getTitle()) + "》，医生：" + defaultDoctorName(consent.getDoctor_name()));
        timeline.setSource_table("patient_consent");
        timeline.setSource_id(consent.getId());
        patientTimelineService.add(timeline);
    }

    private void addSignedTimeline(PatientConsent consent) {
        if (consent == null || consent.getPatient_id() == null) {
            return;
        }
        PatientTimeline timeline = new PatientTimeline();
        timeline.setPatient_id(consent.getPatient_id());
        timeline.setEvent_time(new Date());
        timeline.setEvent_type("知情同意");
        timeline.setEvent_title("患者已签署电子知情同意书");
        timeline.setEvent_content("患者已签署《" + trim(consent.getTitle()) + "》" +
                (StringUtils.hasText(consent.getSignature_name()) ? "，签署人：" + trim(consent.getSignature_name()) : ""));
        timeline.setSource_table("patient_consent");
        timeline.setSource_id(consent.getId());
        patientTimelineService.add(timeline);
    }

    private String defaultDoctorName(String doctorName) {
        String value = trim(doctorName);
        return value.isEmpty() ? "门诊医生" : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
