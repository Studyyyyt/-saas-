package com.example.springboot.service;

import com.example.springboot.entity.ConsultationCreateResponse;
import com.example.springboot.entity.ConsultationFollowup;
import com.example.springboot.entity.ConsultationPromptFlags;
import com.example.springboot.entity.ConsultationQuery;
import com.example.springboot.entity.ConsultationRecord;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.ConsultationRecordMapper;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.util.MarketingChannelCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ConsultationRecordService {

    public static final List<String> CONSULTATION_CHANNEL_OPTIONS = MarketingChannelCatalog.CONSULTATION_CHANNEL_OPTIONS;

    public static final List<String> CUSTOMER_SOURCE_OPTIONS = MarketingChannelCatalog.CUSTOMER_SOURCE_OPTIONS;

    public static final List<String> CHIEF_PROJECT_OPTIONS = List.of(
            "种植", "正畸", "修复", "洗牙", "补牙", "拔牙", "儿童齿科", "美白", "其他"
    );

    public static final List<String> INTENT_LEVEL_OPTIONS = List.of("高", "中", "低");

    public static final List<String> HANDLING_RESULT_OPTIONS = List.of("已成交", "已预约到店", "待跟进", "不再跟进");

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PatientReferralRecordService patientReferralRecordService;

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    @Autowired
    private ConsultationFollowupService consultationFollowupService;

    public List<ConsultationRecord> search(ConsultationQuery query) {
        ConsultationQuery normalized = normalizeQuery(query);
        List<ConsultationRecord> records = consultationRecordMapper.search(normalized);
        enrichRecords(records);
        return records;
    }

    public ConsultationRecord selectById(Long id) {
        ConsultationRecord record = consultationRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        enrichRecords(List.of(record));
        return record;
    }

    public List<ConsultationRecord> selectByPatientId(Long patientId) {
        List<ConsultationRecord> records = consultationRecordMapper.selectByPatientId(patientId);
        enrichRecords(records);
        return records;
    }

    public List<ConsultationFollowup> selectFollowups(Long consultationId) {
        if (consultationId == null || consultationId <= 0) {
            return List.of();
        }
        return consultationFollowupService.listByConsultationId(consultationId);
    }

    public int countFollowups(Long consultationId) {
        if (consultationId == null || consultationId <= 0) {
            return 0;
        }
        return consultationFollowupService.countByConsultationId(consultationId);
    }

    public ConsultationPromptFlags matchPatientByPhone(String phone) {
        return buildPromptFlags(phone);
    }

    public List<ConsultationRecord> searchForPatientCreate(ConsultationQuery query) {
        ConsultationQuery normalized = normalizeQuery(query);
        if (!StringUtils.hasText(normalized.getPhone()) && !StringUtils.hasText(normalized.getName())) {
            return List.of();
        }
        List<ConsultationRecord> records = consultationRecordMapper.searchForPatientCreate(normalized);
        enrichRecords(records);
        return records;
    }

    @Transactional
    public ConsultationCreateResponse add(ConsultationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("咨询记录不能为空");
        }
        ConsultationPromptFlags promptFlags = buildPromptFlags(record.getContact_phone());
        Patient linkedPatient = normalizeCreateRecord(record);
        consultationRecordMapper.insert(record);
        backfillPatientSourceIfNeeded(linkedPatient, record.getConsultation_channel());

        ConsultationCreateResponse response = new ConsultationCreateResponse();
        ConsultationRecord created = consultationRecordMapper.selectById(record.getId());
        enrichRecords(List.of(created));
        syncReferralAndPatientInsights(created);
        response.setRecord(created);
        response.setPromptFlags(promptFlags);
        response.setWeekCount(countCurrentWeekConsultations());
        return response;
    }

    @Transactional
    public ConsultationRecord update(ConsultationRecord record) {
        if (record == null || record.getId() == null || record.getId() <= 0) {
            throw new IllegalArgumentException("咨询记录ID不能为空");
        }
        ConsultationRecord existing = consultationRecordMapper.selectById(record.getId());
        if (existing == null) {
            throw new IllegalArgumentException("咨询记录不存在");
        }
        Patient linkedPatient = normalizeUpdateRecord(record, existing);
        consultationRecordMapper.updateEditableFields(record);
        backfillPatientSourceIfNeeded(linkedPatient, record.getConsultation_channel());
        ConsultationRecord updated = consultationRecordMapper.selectById(record.getId());
        enrichRecords(List.of(updated));
        syncReferralAndPatientInsights(updated, existing.getPatient_id(), existing.getReferrer_patient_id());
        return updated;
    }

    @Transactional
    public ConsultationRecord linkPatient(Long consultationId, Long patientId, Long updatedBy) {
        if (consultationId == null || consultationId <= 0) {
            throw new IllegalArgumentException("咨询记录ID不能为空");
        }
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        ConsultationRecord record = consultationRecordMapper.selectById(consultationId);
        if (record == null) {
            throw new IllegalArgumentException("咨询记录不存在");
        }
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients == null || patients.isEmpty()) {
            throw new IllegalArgumentException("患者不存在");
        }
        Patient patient = patients.get(0);
        String nextHandlingResult = "待跟进".equals(trim(record.getHandling_result())) ? "已预约到店" : trim(record.getHandling_result());
        Date arrivedAt = record.getArrived_at() == null ? now() : record.getArrived_at();
        consultationRecordMapper.linkPatient(consultationId, patientId, nextHandlingResult, arrivedAt, normalizeOperatorId(updatedBy));

        if (shouldBackfillPatientSource(patient.getCustomer_source())) {
            patientMapper.updateCustomerSource(patientId, trim(record.getConsultation_channel()));
        }

        ConsultationRecord linked = consultationRecordMapper.selectById(consultationId);
        enrichRecords(List.of(linked));
        syncReferralAndPatientInsights(linked, record.getPatient_id(), record.getReferrer_patient_id());
        return linked;
    }

    @Transactional
    public ConsultationRecord linkPatientForArchiveCreate(Long consultationId, Long patientId, String patientCustomerSource) {
        ConsultationRecord linked = linkPatient(consultationId, patientId, null);
        if (linked == null) {
            return null;
        }
        if (shouldBackfillPatientSource(patientCustomerSource)) {
            patientMapper.updateCustomerSource(patientId, trim(linked.getConsultation_channel()));
        }
        syncReferralAndPatientInsights(linked);
        return linked;
    }

    @Transactional
    public ConsultationRecord markFirstDealByPatientId(Long patientId, Date dealAt) {
        if (patientId == null || patientId <= 0) {
            return null;
        }
        Date effectiveDealAt = dealAt == null ? now() : dealAt;
        ConsultationRecord target = consultationRecordMapper.selectLatestOpenByPatientBeforeTime(patientId, effectiveDealAt);
        if (target == null || target.getId() == null) {
            return null;
        }
        int updated = consultationRecordMapper.markDealAtIfAbsent(target.getId(), effectiveDealAt);
        if (updated <= 0) {
            return consultationRecordMapper.selectById(target.getId());
        }
        ConsultationRecord record = consultationRecordMapper.selectById(target.getId());
        enrichRecords(List.of(record));
        syncReferralAndPatientInsights(record);
        return record;
    }

    @Transactional
    public void clearPatientLinkByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        consultationRecordMapper.clearPatientLinkByPatientId(patientId, null);
    }

    public int countHighIntentPendingRecent7Days() {
        ConsultationQuery query = new ConsultationQuery();
        query.setIntentLevel("高");
        query.setHandlingResult("待跟进");
        LocalDateTime end = LocalDateTime.now(ZONE_ID);
        LocalDateTime start = end.minusDays(7);
        query.setStartTime(formatDateTime(Date.from(start.atZone(ZONE_ID).toInstant())));
        query.setEndTime(formatDateTime(Date.from(end.atZone(ZONE_ID).toInstant())));
        return consultationRecordMapper.search(query).size();
    }

    public boolean shouldBackfillPatientSource(String customerSource) {
        String normalized = trim(customerSource);
        return normalized.isEmpty() || "暂未确认".equals(normalized);
    }

    private ConsultationPromptFlags buildPromptFlags(String phone) {
        ConsultationPromptFlags flags = new ConsultationPromptFlags();
        String normalizedPhone = trim(phone);
        if (!normalizedPhone.isEmpty()) {
            List<Patient> matchedPatients = patientMapper.selectByPhoneExact(normalizedPhone);
            if (matchedPatients != null && !matchedPatients.isEmpty()) {
                Patient patient = matchedPatients.get(0);
                flags.setPhoneMatchedPatient(true);
                flags.setMatchedPatientId((long) patient.getId());
                flags.setMatchedPatientName(trim(patient.getName()));
            }
            int openCount = consultationRecordMapper.countOpenConsultationsByPhone(normalizedPhone);
            flags.setOpenConsultationCount(openCount);
            flags.setPhoneHasOpenConsultation(openCount >= 1);
        }
        return flags;
    }

    private Patient normalizeCreateRecord(ConsultationRecord record) {
        record.setPatient_id(normalizePositiveId(record.getPatient_id()));
        Patient linkedPatient = resolveLinkedPatient(record.getPatient_id());
        record.setConsultation_time(parseDate(record.getConsultation_time(), "咨询时间不能为空"));
        record.setConsultation_channel(validateOption(trim(record.getConsultation_channel()), CONSULTATION_CHANNEL_OPTIONS, "咨询渠道不合法"));
        normalizeReferralFields(record);
        record.setChief_project(validateOption(trim(record.getChief_project()), CHIEF_PROJECT_OPTIONS, "主诉项目不合法"));
        record.setIntent_level(validateOption(trim(record.getIntent_level()), INTENT_LEVEL_OPTIONS, "意向强度不合法"));
        String handlingResult = trim(record.getHandling_result());
        if (handlingResult.isEmpty()) {
            handlingResult = "待跟进";
        }
        record.setHandling_result(validateOption(handlingResult, HANDLING_RESULT_OPTIONS, "处理结果不合法"));
        record.setContact_name(trimToNull(record.getContact_name()));
        record.setContact_phone(normalizePhoneOptional(record.getContact_phone()));
        record.setRemarks(normalizeRemarks(record.getRemarks()));
        record.setEstimated_amount(normalizeAmount(record.getEstimated_amount()));
        record.setCustomer_concerns(normalizeCustomerConcerns(record.getCustomer_concerns()));
        record.setAi_analysis_summary(trimToNull(record.getAi_analysis_summary()));
        record.setAi_analysis_score(normalizeAiScore(record.getAi_analysis_score()));
        Long operatorId = normalizePositiveId(record.getCreated_by());
        if (operatorId == null) {
            String manualName = trimToNull(record.getCreated_by_name());
            if (manualName == null) {
                throw new IllegalArgumentException("录入人姓名不能为空");
            }
            record.setCreated_by(null);
            record.setCreated_by_name(manualName);
            record.setUpdated_by(normalizePositiveId(record.getUpdated_by()));
        } else {
            record.setCreated_by(operatorId);
            record.setCreated_by_name(resolveOperatorName(operatorId, record.getCreated_by_name()));
            record.setUpdated_by(operatorId);
        }
        if ("已成交".equals(record.getHandling_result())) {
            if (linkedPatient == null) {
                throw new IllegalArgumentException("已成交必须关联患者信息");
            }
            record.setArrived_at(now());
            record.setDeal_at(now());
        } else if ("已预约到店".equals(record.getHandling_result())) {
            record.setArrived_at(now());
            record.setDeal_at(null);
        } else {
            record.setArrived_at(null);
            record.setDeal_at(null);
        }
        return linkedPatient;
    }

    private Patient normalizeUpdateRecord(ConsultationRecord record, ConsultationRecord existing) {
        record.setPatient_id(normalizePositiveId(record.getPatient_id()));
        Patient linkedPatient = resolveLinkedPatient(record.getPatient_id());
        record.setConsultation_time(parseDate(record.getConsultation_time(), "咨询时间不能为空"));
        record.setConsultation_channel(validateOption(trim(record.getConsultation_channel()), CONSULTATION_CHANNEL_OPTIONS, "咨询渠道不合法"));
        normalizeReferralFields(record);
        record.setChief_project(validateOption(trim(record.getChief_project()), CHIEF_PROJECT_OPTIONS, "主诉项目不合法"));
        record.setIntent_level(validateOption(trim(record.getIntent_level()), INTENT_LEVEL_OPTIONS, "意向强度不合法"));
        String handlingResult = trim(record.getHandling_result());
        if (handlingResult.isEmpty()) {
            handlingResult = existing.getHandling_result();
        }
        record.setHandling_result(validateOption(handlingResult, HANDLING_RESULT_OPTIONS, "处理结果不合法"));
        record.setContact_name(trimToNull(record.getContact_name()));
        record.setContact_phone(normalizePhoneOptional(record.getContact_phone()));
        record.setRemarks(normalizeRemarks(record.getRemarks()));
        record.setEstimated_amount(normalizeAmount(record.getEstimated_amount()));
        record.setCustomer_concerns(normalizeCustomerConcerns(record.getCustomer_concerns()));
        record.setAi_analysis_summary(trimToNull(record.getAi_analysis_summary()));
        record.setAi_analysis_score(normalizeAiScore(record.getAi_analysis_score()));
        Long operatorId = normalizePositiveId(record.getUpdated_by());
        record.setUpdated_by(operatorId);
        Long newCreatedBy = normalizePositiveId(record.getCreated_by());
        if (newCreatedBy != null && newCreatedBy > 0) {
            record.setCreated_by(newCreatedBy);
            record.setCreated_by_name(trimToNull(record.getCreated_by_name()));
        } else {
            String manualName = trimToNull(record.getCreated_by_name());
            if (manualName != null) {
                record.setCreated_by(null);
                record.setCreated_by_name(manualName);
            } else {
                record.setCreated_by(existing.getCreated_by());
                record.setCreated_by_name(existing.getCreated_by_name());
            }
        }
        if ("已成交".equals(record.getHandling_result())) {
            if (linkedPatient == null) {
                throw new IllegalArgumentException("已成交必须关联患者信息");
            }
            record.setArrived_at(existing.getArrived_at() == null ? now() : existing.getArrived_at());
            record.setDeal_at(existing.getDeal_at() == null ? now() : existing.getDeal_at());
        } else if ("已预约到店".equals(record.getHandling_result())) {
            record.setArrived_at(existing.getArrived_at() == null ? now() : existing.getArrived_at());
            record.setDeal_at(existing.getDeal_at());
        } else {
            record.setArrived_at(existing.getArrived_at());
            record.setDeal_at(existing.getDeal_at());
        }
        return linkedPatient;
    }

    private void enrichRecords(List<ConsultationRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> patientIds = new LinkedHashSet<>();
        for (ConsultationRecord record : records) {
            if (record == null) {
                continue;
            }
            record.setHas_deal(record.getDeal_at() != null);
            if (record.getPatient_id() != null && record.getPatient_id() > 0) {
                patientIds.add(record.getPatient_id());
            }
        }
        Map<Long, Double> revenueByPatientId = buildPatientRevenueMap(new ArrayList<>(patientIds));
        for (ConsultationRecord record : records) {
            if (record == null) {
                continue;
            }
            Double amount = record.getPatient_id() == null ? 0D : revenueByPatientId.getOrDefault(record.getPatient_id(), 0D);
            record.setTotal_deal_amount(round2(amount));
        }
    }

    private void normalizeReferralFields(ConsultationRecord record) {
        if (record == null) {
            return;
        }
        if (!"转介绍".equals(trim(record.getConsultation_channel()))) {
            clearReferralFields(record);
            return;
        }
        boolean hasInternal = normalizePositiveId(record.getReferrer_patient_id()) != null
                || StringUtils.hasText(trimToNull(record.getReferrer_patient_name()));
        boolean hasExternal = StringUtils.hasText(trimToNull(record.getExternal_referrer_name()))
                || StringUtils.hasText(trimToNull(record.getExternal_referrer_contact()))
                || StringUtils.hasText(trimToNull(record.getExternal_referrer_type()));
        if (!hasInternal && !hasExternal) {
            clearReferralFields(record);
            return;
        }
        if (hasInternal && hasExternal) {
            throw new IllegalArgumentException("介绍人不能同时填写内部患者和外部介绍人");
        }
        if (hasInternal) {
            Long referrerPatientId = normalizePositiveId(record.getReferrer_patient_id());
            if (referrerPatientId == null) {
                throw new IllegalArgumentException("请选择有效的介绍患者");
            }
            List<Patient> referrerPatients = patientMapper.selectById(referrerPatientId);
            if (referrerPatients == null || referrerPatients.isEmpty() || referrerPatients.get(0) == null) {
                throw new IllegalArgumentException("介绍患者不存在");
            }
            Patient referrerPatient = referrerPatients.get(0);
            record.setReferrer_type("patient");
            record.setReferrer_patient_id((long) referrerPatient.getId());
            record.setReferrer_patient_name(trimToNull(referrerPatient.getName()));
            record.setExternal_referrer_type(null);
            record.setExternal_referrer_name(null);
            record.setExternal_referrer_contact(null);
            return;
        }
        String externalReferrerName = trimToNull(record.getExternal_referrer_name());
        if (!StringUtils.hasText(externalReferrerName)) {
            throw new IllegalArgumentException("外部介绍人姓名不能为空");
        }
        record.setReferrer_type("external");
        record.setReferrer_patient_id(null);
        record.setReferrer_patient_name(null);
        record.setExternal_referrer_type(trimToNull(record.getExternal_referrer_type()));
        record.setExternal_referrer_name(externalReferrerName);
        record.setExternal_referrer_contact(trimToNull(record.getExternal_referrer_contact()));
    }

    private void clearReferralFields(ConsultationRecord record) {
        record.setReferrer_type(null);
        record.setReferrer_patient_id(null);
        record.setReferrer_patient_name(null);
        record.setExternal_referrer_type(null);
        record.setExternal_referrer_name(null);
        record.setExternal_referrer_contact(null);
    }

    private void syncReferralAndPatientInsights(ConsultationRecord record, Long... extraPatientIds) {
        if (record == null) {
            return;
        }
        if (patientReferralRecordService != null) {
            patientReferralRecordService.syncFromConsultation(record);
        }
        if (patientInsightSummaryService != null) {
            List<Long> patientIds = new ArrayList<>();
            if (record.getPatient_id() != null && record.getPatient_id() > 0) {
                patientIds.add(record.getPatient_id());
            }
            if (record.getReferrer_patient_id() != null && record.getReferrer_patient_id() > 0) {
                patientIds.add(record.getReferrer_patient_id());
            }
            if (extraPatientIds != null) {
                for (Long extraPatientId : extraPatientIds) {
                    if (extraPatientId != null && extraPatientId > 0) {
                        patientIds.add(extraPatientId);
                    }
                }
            }
            if (!patientIds.isEmpty()) {
                patientInsightSummaryService.refreshPatients(patientIds);
            }
        }
    }

    private Map<Long, Double> buildPatientRevenueMap(List<Long> patientIds) {
        Map<Long, Double> result = new HashMap<>();
        if (patientIds == null || patientIds.isEmpty()) {
            return result;
        }
        List<Finance> finances = financeMapper.selectByPatientIds(patientIds);
        if (finances == null || finances.isEmpty()) {
            return result;
        }
        for (Finance finance : finances) {
            if (finance == null || finance.getPatient_id() == null || finance.getPatient_id() <= 0
                    || finance.getTreatment_id() == null || finance.getTreatment_id() <= 0) {
                continue;
            }
            double amount = round2(finance.getAmount());
            if (amount <= 0) {
                continue;
            }
            long patientId = finance.getPatient_id();
            if (isRefundFinance(finance)) {
                result.merge(patientId, -amount, Double::sum);
            } else if (isChargeFinance(finance)) {
                result.merge(patientId, amount, Double::sum);
            }
        }
        for (Map.Entry<Long, Double> entry : result.entrySet()) {
            entry.setValue(Math.max(0D, round2(entry.getValue())));
        }
        return result;
    }

    private boolean isChargeFinance(Finance finance) {
        String bizType = trim(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if ("TREATMENT_CHARGE".equals(bizType)) {
            return true;
        }
        if ("TREATMENT_REFUND".equals(bizType)) {
            return false;
        }
        String type = trim(finance.getType());
        return type.contains("收费");
    }

    private boolean isRefundFinance(Finance finance) {
        String bizType = trim(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if ("TREATMENT_REFUND".equals(bizType)) {
            return true;
        }
        if ("TREATMENT_CHARGE".equals(bizType)) {
            return false;
        }
        String type = trim(finance.getType());
        return type.contains("退款");
    }

    private ConsultationQuery normalizeQuery(ConsultationQuery query) {
        ConsultationQuery normalized = query == null ? new ConsultationQuery() : query;
        normalized.setKeyword(trimToNull(normalized.getKeyword()));
        normalized.setChannel(trimToNull(normalized.getChannel()));
        normalized.setChiefProject(trimToNull(normalized.getChiefProject()));
        normalized.setIntentLevel(trimToNull(normalized.getIntentLevel()));
        normalized.setHandlingResult(trimToNull(normalized.getHandlingResult()));
        normalized.setName(trimToNull(normalized.getName()));
        normalized.setPhone(trimToNull(normalized.getPhone()));
        normalized.setStartTime(trimToNull(normalized.getStartTime()));
        normalized.setEndTime(trimToNull(normalized.getEndTime()));
        return normalized;
    }

    private int countCurrentWeekConsultations() {
        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDate weekEndDate = weekStartDate.plusWeeks(1);
        Date weekStart = Date.from(weekStartDate.atStartOfDay(ZONE_ID).toInstant());
        Date weekEnd = Date.from(weekEndDate.atStartOfDay(ZONE_ID).toInstant());
        return consultationRecordMapper.countByConsultationTimeRange(weekStart, weekEnd);
    }

    private Date parseDate(Date value, String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private String normalizePhoneOptional(String phone) {
        String normalized = trim(phone);
        if (normalized.isEmpty()) {
            return null;
        }
        if (!normalized.matches("^\\d{11}$")) {
            throw new IllegalArgumentException("手机号需为11位数字");
        }
        return normalized;
    }

    private String normalizeRemarks(String remarks) {
        String normalized = trim(remarks);
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("备注不能超过200字");
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private String validateOption(String value, List<String> options, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
        if (!options.contains(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private Patient resolveLinkedPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return null;
        }
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients == null || patients.isEmpty() || patients.get(0) == null) {
            throw new IllegalArgumentException("关联患者不存在");
        }
        return patients.get(0);
    }

    private Long normalizeOperatorId(Long operatorId) {
        return operatorId != null && operatorId > 0 ? operatorId : null;
    }

    private void backfillPatientSourceIfNeeded(Patient patient, String consultationChannel) {
        if (patient == null || patient.getId() <= 0) {
            return;
        }
        if (shouldBackfillPatientSource(patient.getCustomer_source())) {
            patientMapper.updateCustomerSource((long) patient.getId(), trim(consultationChannel));
        }
    }

    private String resolveOperatorName(Long operatorId, String fallbackName) {
        if (operatorId != null && operatorId > 0) {
            List<com.example.springboot.entity.Account> accounts = accountService.selectById(operatorId);
            if (accounts != null && !accounts.isEmpty() && StringUtils.hasText(accounts.get(0).getName())) {
                return accounts.get(0).getName().trim();
            }
        }
        return trim(fallbackName);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String normalized = trim(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private Date now() {
        return new Date();
    }

    private String formatDateTime(Date value) {
        if (value == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(value.toInstant(), ZONE_ID);
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private Double normalizeAmount(Double amount) {
        if (amount == null || amount < 0 || Double.isNaN(amount)) {
            return null;
        }
        return round2(amount);
    }

    private String normalizeCustomerConcerns(String concerns) {
        String normalized = trim(concerns);
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("客户顾虑不能超过500字");
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private Integer normalizeAiScore(Integer score) {
        if (score == null) {
            return null;
        }
        if (score < 0) {
            return 0;
        }
        if (score > 100) {
            return 100;
        }
        return score;
    }
}
