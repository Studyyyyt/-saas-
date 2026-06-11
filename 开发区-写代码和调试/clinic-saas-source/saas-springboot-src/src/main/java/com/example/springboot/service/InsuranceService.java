package com.example.springboot.service;

import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsuranceService {

    @Autowired
    private InsuranceConfigMapper insuranceConfigMapper;
    @Autowired
    private InsurancePatientProfileMapper insurancePatientProfileMapper;
    @Autowired
    private InsuranceSettlementMapper insuranceSettlementMapper;
    @Autowired
    private InsuranceOperationLogMapper insuranceOperationLogMapper;
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private TreatmentCatalogMapper treatmentCatalogMapper;

    public InsuranceOverview getOverview() {
        InsuranceOverview overview = new InsuranceOverview();
        overview.setConfig(insuranceConfigMapper.selectPrimary());
        overview.setPatientProfileCount(insurancePatientProfileMapper.countAll());
        overview.setSettlementCount(insuranceSettlementMapper.countAll());
        overview.setPendingSettlementCount(insuranceSettlementMapper.countByStatus("PENDING"));
        overview.setFailedSettlementCount(insuranceSettlementMapper.countByStatus("FAILED"));
        return overview;
    }

    public InsuranceConfig getConfig() {
        return insuranceConfigMapper.selectPrimary();
    }

    public InsuranceConfig saveConfig(InsuranceConfig config) {
        validateConfig(config);
        InsuranceConfig existed = insuranceConfigMapper.selectPrimary();
        if (existed == null) {
            normalizeConfig(config);
            insuranceConfigMapper.insert(config);
            return config;
        }
        config.setId(existed.getId());
        normalizeConfig(config);
        insuranceConfigMapper.update(config);
        return insuranceConfigMapper.selectPrimary();
    }

    public InsurancePatientProfile getPatientProfile(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        return insurancePatientProfileMapper.selectByPatientId(patientId);
    }

    public InsurancePatientProfile savePatientProfile(InsurancePatientProfile profile) {
        validatePatientProfile(profile);
        InsurancePatientProfile existed = insurancePatientProfileMapper.selectByPatientId(profile.getPatient_id());
        if (existed == null) {
            normalizePatientProfile(profile);
            insurancePatientProfileMapper.insert(profile);
            return profile;
        }
        profile.setId(existed.getId());
        normalizePatientProfile(profile);
        insurancePatientProfileMapper.update(profile);
        return insurancePatientProfileMapper.selectByPatientId(profile.getPatient_id());
    }

    public List<InsuranceSettlement> getSettlements(Long patientId) {
        if (patientId != null && patientId > 0) {
            return insuranceSettlementMapper.selectByPatientId(patientId);
        }
        return insuranceSettlementMapper.selectAll();
    }

    public InsuranceSettlement createSettlementDraft(InsuranceSettlement settlement) {
        validateSettlementDraft(settlement);
        normalizeSettlementDraft(settlement);
        if (settlement.getSettlement_status() == null || settlement.getSettlement_status().trim().isEmpty()) {
            settlement.setSettlement_status("PENDING");
        }
        if (settlement.getUpload_status() == null || settlement.getUpload_status().trim().isEmpty()) {
            settlement.setUpload_status("NOT_UPLOADED");
        }
        if (settlement.getSettlement_time() == null) {
            settlement.setSettlement_time(new Date());
        }
        if (!StringUtils.hasText(settlement.getVisit_no())) {
            settlement.setVisit_no(generateVisitNo(settlement.getPatient_id()));
        }
        if (!StringUtils.hasText(settlement.getSettlement_no())) {
            settlement.setSettlement_no(generateSettlementNo(settlement.getPatient_id()));
        }
        insuranceSettlementMapper.insert(settlement);
        logSettlementDraftCreated(settlement);
        return settlement;
    }

    public List<InsuranceOperationLog> getRecentLogs(int limit) {
        int safeLimit = limit <= 0 ? 20 : Math.min(limit, 100);
        return insuranceOperationLogMapper.selectRecent(safeLimit);
    }

    public Map<String, Object> buildMockSettlementPayload(Long patientId, Long treatmentCatalogId, Double totalAmount) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        Patient patient = patientMapper.selectById(patientId).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("患者不存在"));
        InsurancePatientProfile profile = insurancePatientProfileMapper.selectByPatientId(patientId);
        TreatmentCatalog catalog = treatmentCatalogId == null ? null : treatmentCatalogMapper.selectById(treatmentCatalogId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("platform", getPlatformCode());
        payload.put("orgCode", getOrgCode());
        payload.put("bizType", "OUTPATIENT_SETTLEMENT");
        payload.put("timestamp", new Date().getTime());

        Map<String, Object> patientInfo = new LinkedHashMap<>();
        patientInfo.put("patientId", patient.getId());
        patientInfo.put("name", patient.getName());
        patientInfo.put("gender", patient.getGender());
        patientInfo.put("phone", patient.getPhone());
        patientInfo.put("idCardNo", profile == null ? null : profile.getId_card_no());
        patientInfo.put("insurancePersonNo", profile == null ? null : profile.getInsurance_person_no());
        patientInfo.put("insuredType", profile == null ? null : profile.getInsured_type());
        payload.put("patient", patientInfo);

        Map<String, Object> settlementInfo = new LinkedHashMap<>();
        settlementInfo.put("visitNo", generateVisitNo(patientId));
        settlementInfo.put("totalAmount", totalAmount == null ? 0D : totalAmount);
        settlementInfo.put("cashAmount", totalAmount == null ? 0D : totalAmount);
        settlementInfo.put("insuranceAmount", 0D);
        settlementInfo.put("personalAmount", totalAmount == null ? 0D : totalAmount);
        payload.put("settlement", settlementInfo);

        Map<String, Object> itemInfo = new LinkedHashMap<>();
        itemInfo.put("catalogId", catalog == null ? null : catalog.getId());
        itemInfo.put("itemName", catalog == null ? null : catalog.getItem_name());
        itemInfo.put("defaultFee", catalog == null ? null : catalog.getDefault_fee());
        itemInfo.put("medicalInsuranceCode", catalog == null ? null : catalog.getMedical_insurance_code());
        itemInfo.put("medicalInsuranceName", catalog == null ? null : catalog.getMedical_insurance_name());
        itemInfo.put("medicalInsuranceCategory", catalog == null ? null : catalog.getMedical_insurance_category());
        itemInfo.put("selfPayRatio", catalog == null ? null : catalog.getSelf_pay_ratio());
        payload.put("item", itemInfo);

        return payload;
    }

    public void logOperation(InsuranceOperationLog log) {
        if (log == null) {
            return;
        }
        insuranceOperationLogMapper.insert(log);
    }

    private void validateConfig(InsuranceConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("医保配置不能为空");
        }
        if (!StringUtils.hasText(config.getPlatform_code())) {
            throw new IllegalArgumentException("平台编码不能为空");
        }
        if (!StringUtils.hasText(config.getPlatform_name())) {
            throw new IllegalArgumentException("平台名称不能为空");
        }
    }

    private void normalizeConfig(InsuranceConfig config) {
        config.setPlatform_code(trimToNull(config.getPlatform_code()));
        config.setPlatform_name(trimToNull(config.getPlatform_name()));
        config.setApi_base_url(trimToNull(config.getApi_base_url()));
        config.setOrg_code(trimToNull(config.getOrg_code()));
        config.setOrg_name(trimToNull(config.getOrg_name()));
        config.setApp_id(trimToNull(config.getApp_id()));
        config.setApp_secret(trimToNull(config.getApp_secret()));
        config.setSign_key(trimToNull(config.getSign_key()));
        config.setEncryption_type(trimToNull(config.getEncryption_type()));
        config.setRegion_code(trimToNull(config.getRegion_code()));
        config.setExt_json(trimToNull(config.getExt_json()));
        if (config.getEnabled() == null) {
            config.setEnabled(1);
        }
    }

    private void validatePatientProfile(InsurancePatientProfile profile) {
        if (profile == null || profile.getPatient_id() == null || profile.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        if (patientMapper.selectById(profile.getPatient_id()).isEmpty()) {
            throw new IllegalArgumentException("患者不存在");
        }
    }

    private void normalizePatientProfile(InsurancePatientProfile profile) {
        profile.setInsurance_person_no(trimToNull(profile.getInsurance_person_no()));
        profile.setId_card_no(trimToNull(profile.getId_card_no()));
        profile.setInsured_region_code(trimToNull(profile.getInsured_region_code()));
        profile.setInsured_type(trimToNull(profile.getInsured_type()));
        profile.setCard_no(trimToNull(profile.getCard_no()));
        profile.setCard_type(trimToNull(profile.getCard_type()));
        profile.setPerson_name(trimToNull(profile.getPerson_name()));
        profile.setGender(trimToNull(profile.getGender()));
        profile.setBirthday(trimToNull(profile.getBirthday()));
        profile.setPhone(trimToNull(profile.getPhone()));
        profile.setLast_auth_no(trimToNull(profile.getLast_auth_no()));
        profile.setExt_json(trimToNull(profile.getExt_json()));
        if (profile.getStatus() == null) {
            profile.setStatus(1);
        }
    }

    private void validateSettlementDraft(InsuranceSettlement settlement) {
        if (settlement == null || settlement.getPatient_id() == null || settlement.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        if (patientMapper.selectById(settlement.getPatient_id()).isEmpty()) {
            throw new IllegalArgumentException("患者不存在");
        }
        if (settlement.getTotal_amount() == null) {
            throw new IllegalArgumentException("结算总金额不能为空");
        }
        if (settlement.getTotal_amount() < 0 || valueOrZero(settlement.getInsurance_amount()) < 0
                || valueOrZero(settlement.getPersonal_amount()) < 0 || valueOrZero(settlement.getCash_amount()) < 0) {
            throw new IllegalArgumentException("结算金额不能为负数");
        }
        double diff = Math.abs(settlement.getTotal_amount() - (
                valueOrZero(settlement.getInsurance_amount())
                        + valueOrZero(settlement.getPersonal_amount())
                        + valueOrZero(settlement.getCash_amount())
        ));
        if (diff > 0.01D) {
            throw new IllegalArgumentException("金额不平衡，请检查医保支付、个人金额、现金金额之和是否等于结算总金额");
        }
    }

    private void normalizeSettlementDraft(InsuranceSettlement settlement) {
        settlement.setSettlement_no(trimToNull(settlement.getSettlement_no()));
        settlement.setVisit_no(trimToNull(settlement.getVisit_no()));
        settlement.setBiz_type(trimToNull(settlement.getBiz_type()));
        settlement.setSettlement_status(trimToNull(settlement.getSettlement_status()));
        settlement.setUpload_status(trimToNull(settlement.getUpload_status()));
        settlement.setUpload_payload(trimToNull(settlement.getUpload_payload()));
        settlement.setResponse_payload(trimToNull(settlement.getResponse_payload()));
        settlement.setRemark(trimToNull(settlement.getRemark()));
        if (settlement.getInsurance_amount() == null) {
            settlement.setInsurance_amount(0D);
        }
        if (settlement.getPersonal_amount() == null) {
            settlement.setPersonal_amount(0D);
        }
        if (settlement.getCash_amount() == null) {
            settlement.setCash_amount(0D);
        }
        if (!StringUtils.hasText(settlement.getBiz_type())) {
            settlement.setBiz_type("OUTPATIENT_SETTLEMENT");
        }
    }

    private void logSettlementDraftCreated(InsuranceSettlement settlement) {
        InsuranceOperationLog log = new InsuranceOperationLog();
        log.setOperation_type("SETTLEMENT_DRAFT_CREATE");
        log.setRef_type("insurance_settlement");
        log.setRef_id(settlement.getId() == null ? null : String.valueOf(settlement.getId()));
        log.setRequest_url("/insurance/settlements/draft");
        log.setRequest_method("POST");
        log.setRequest_payload(buildSettlementDraftLogPayload(settlement));
        log.setResponse_payload(null);
        log.setResponse_code("200");
        log.setResponse_message("医保结算草稿创建成功");
        log.setStatus("SUCCESS");
        logOperation(log);
    }

    private String buildSettlementDraftLogPayload(InsuranceSettlement settlement) {
        return "patientId=" + settlement.getPatient_id()
                + ", settlementNo=" + settlement.getSettlement_no()
                + ", visitNo=" + settlement.getVisit_no()
                + ", totalAmount=" + settlement.getTotal_amount()
                + ", insuranceAmount=" + settlement.getInsurance_amount()
                + ", personalAmount=" + settlement.getPersonal_amount()
                + ", cashAmount=" + settlement.getCash_amount();
    }

    private double valueOrZero(Double value) {
        return value == null ? 0D : value;
    }

    private String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private String getPlatformCode() {
        InsuranceConfig config = insuranceConfigMapper.selectPrimary();
        return config == null ? "LOCAL_MOCK" : config.getPlatform_code();
    }

    private String getOrgCode() {
        InsuranceConfig config = insuranceConfigMapper.selectPrimary();
        return config == null ? null : config.getOrg_code();
    }

    private String generateVisitNo(Long patientId) {
        return "VISIT-" + patientId + "-" + System.currentTimeMillis();
    }

    private String generateSettlementNo(Long patientId) {
        return "SETTLE-" + patientId + "-" + System.currentTimeMillis();
    }
}
