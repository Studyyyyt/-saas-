package com.example.springboot.service;

import com.example.springboot.entity.ConsultationQuery;
import com.example.springboot.entity.ConsultationRecord;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientFollowup;
import com.example.springboot.entity.PatientInsightSummary;
import com.example.springboot.entity.PatientReferralRecord;
import com.example.springboot.entity.Treatment;
import com.example.springboot.mapper.ConsultationRecordMapper;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.MedicalRecordMapper;
import com.example.springboot.mapper.PatientFollowupMapper;
import com.example.springboot.mapper.PatientInsightSummaryMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.PatientReferralRecordMapper;
import com.example.springboot.mapper.TreatmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class PatientInsightSummaryService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final PatientInsightSummaryMapper patientInsightSummaryMapper;
    private final PatientReferralRecordMapper patientReferralRecordMapper;
    private final PatientMapper patientMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final TreatmentMapper treatmentMapper;
    private final FinanceMapper financeMapper;
    private final PatientFollowupMapper patientFollowupMapper;
    private final ConsultationRecordMapper consultationRecordMapper;

    @Autowired
    public PatientInsightSummaryService(PatientInsightSummaryMapper patientInsightSummaryMapper,
                                        PatientReferralRecordMapper patientReferralRecordMapper,
                                        PatientMapper patientMapper,
                                        MedicalRecordMapper medicalRecordMapper,
                                        TreatmentMapper treatmentMapper,
                                        FinanceMapper financeMapper,
                                        PatientFollowupMapper patientFollowupMapper,
                                        ConsultationRecordMapper consultationRecordMapper) {
        this.patientInsightSummaryMapper = patientInsightSummaryMapper;
        this.patientReferralRecordMapper = patientReferralRecordMapper;
        this.patientMapper = patientMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.treatmentMapper = treatmentMapper;
        this.financeMapper = financeMapper;
        this.patientFollowupMapper = patientFollowupMapper;
        this.consultationRecordMapper = consultationRecordMapper;
    }

    public PatientInsightSummary selectByPatientId(Long patientId) {
        return patientId == null || patientId <= 0 ? null : patientInsightSummaryMapper.selectByPatientId(patientId);
    }

    public List<PatientInsightSummary> selectAll() {
        return patientInsightSummaryMapper.selectAll();
    }

    public List<PatientInsightSummary> selectByPatientIds(List<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return List.of();
        }
        return patientInsightSummaryMapper.selectByPatientIds(patientIds);
    }

    public PatientInsightSummary getOrRefresh(Long patientId) {
        PatientInsightSummary summary = selectByPatientId(patientId);
        if (summary != null) {
            return summary;
        }
        Map<Long, PatientInsightSummary> refreshed = refreshPatients(List.of(patientId));
        return refreshed.get(patientId);
    }

    public Map<String, Object> buildOverview() {
        List<PatientInsightSummary> summaries = selectAll();
        LocalDate firstDayOfMonth = LocalDate.now(ZONE_ID).withDayOfMonth(1);
        Instant monthStartInstant = firstDayOfMonth.atStartOfDay(ZONE_ID).toInstant();
        int monthNewReferralCount = 0;
        for (PatientReferralRecord record : patientReferralRecordMapper.selectAll()) {
            Date createdAt = record == null ? null : record.getCreated_at();
            if (createdAt != null && !createdAt.toInstant().isBefore(monthStartInstant)) {
                monthNewReferralCount++;
            }
        }

        int highValueCount = 0;
        int lostRiskCount = 0;
        int wordOfMouthCount = 0;
        Date latestUpdatedAt = null;
        for (PatientInsightSummary summary : summaries) {
            if (Boolean.TRUE.equals(summary.getHigh_value_flag())) {
                highValueCount++;
            }
            if (Boolean.TRUE.equals(summary.getLost_risk_flag())) {
                lostRiskCount++;
            }
            if (Boolean.TRUE.equals(summary.getWord_of_mouth_flag())) {
                wordOfMouthCount++;
            }
            if (summary.getUpdated_at() != null && (latestUpdatedAt == null || summary.getUpdated_at().after(latestUpdatedAt))) {
                latestUpdatedAt = summary.getUpdated_at();
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_patient_count", summaries.size());
        result.put("high_value_count", highValueCount);
        result.put("lost_risk_count", lostRiskCount);
        result.put("word_of_mouth_count", wordOfMouthCount);
        result.put("month_new_referral_count", monthNewReferralCount);
        result.put("updated_at", latestUpdatedAt);
        return result;
    }

    public Map<Long, PatientInsightSummary> refreshPatients(Collection<Long> patientIds) {
        Set<Long> affectedIds = new LinkedHashSet<>();
        if (patientIds != null) {
            for (Long patientId : patientIds) {
                if (patientId != null && patientId > 0) {
                    affectedIds.add(patientId);
                }
            }
        }
        if (affectedIds.isEmpty()) {
            return Map.of();
        }
        for (PatientReferralRecord record : patientReferralRecordMapper.selectByPatientIds(new ArrayList<>(affectedIds))) {
            Long referrerPatientId = record == null ? null : normalizePositiveId(record.getReferrer_patient_id());
            if (referrerPatientId != null) {
                affectedIds.add(referrerPatientId);
            }
        }

        Map<Long, PatientInsightSummary> result = new LinkedHashMap<>();
        for (Long patientId : affectedIds) {
            PatientInsightSummary summary = buildSingleSummary(patientId);
            if (summary != null) {
                patientInsightSummaryMapper.upsert(summary);
                result.put(patientId, summary);
            }
        }
        recalculateHighValueFlags();
        Map<Long, PatientInsightSummary> refreshedResult = new LinkedHashMap<>();
        for (PatientInsightSummary summary : selectByPatientIds(new ArrayList<>(affectedIds))) {
            if (summary != null && summary.getPatient_id() != null) {
                refreshedResult.put(summary.getPatient_id(), summary);
            }
        }
        return refreshedResult;
    }

    public void deleteByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        patientInsightSummaryMapper.deleteByPatientId(patientId);
    }

    @Scheduled(cron = "${patient-insights.refresh-cron:0 15 2 * * *}", zone = "Asia/Shanghai")
    public void refreshAllScheduled() {
        refreshAll();
    }

    public void refreshAll() {
        List<Patient> patients = patientMapper.selectAll();
        List<Long> patientIds = patients.stream()
                .filter(Objects::nonNull)
                .map(patient -> (long) patient.getId())
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (patientIds.isEmpty()) {
            return;
        }

        Map<Long, List<MedicalRecord>> medicalRecordsByPatientId = groupByPatientId(medicalRecordMapper.selectAll(), MedicalRecord::getPatient_id);
        Map<Long, List<Treatment>> treatmentsByPatientId = groupByPatientId(treatmentMapper.selectAll(), Treatment::getPatient_id);
        Map<Long, List<Finance>> financesByPatientId = groupByPatientId(financeMapper.getAllFinances(), Finance::getPatient_id);
        Map<Long, List<PatientFollowup>> followupsByPatientId = groupByPatientId(patientFollowupMapper.selectAll(), PatientFollowup::getPatient_id);
        ConsultationQuery consultationQuery = new ConsultationQuery();
        Map<Long, List<ConsultationRecord>> consultationsByPatientId = groupByPatientId(
                consultationRecordMapper.search(consultationQuery),
                ConsultationRecord::getPatient_id
        );
        List<PatientReferralRecord> allReferralRecords = patientReferralRecordMapper.selectAll();
        Map<Long, List<PatientReferralRecord>> outgoingReferralByReferrerId = groupByPatientId(allReferralRecords, PatientReferralRecord::getReferrer_patient_id);

        Map<Long, PatientInsightSummary> summaryByPatientId = new LinkedHashMap<>();
        for (Long patientId : patientIds) {
            PatientInsightSummary summary = buildCoreSummary(
                    patientId,
                    medicalRecordsByPatientId.getOrDefault(patientId, List.of()),
                    treatmentsByPatientId.getOrDefault(patientId, List.of()),
                    financesByPatientId.getOrDefault(patientId, List.of()),
                    consultationsByPatientId.getOrDefault(patientId, List.of()),
                    followupsByPatientId.getOrDefault(patientId, List.of())
            );
            summaryByPatientId.put(patientId, summary);
        }
        for (Long patientId : patientIds) {
            applyOutgoingReferralMetrics(
                    summaryByPatientId.get(patientId),
                    outgoingReferralByReferrerId.getOrDefault(patientId, List.of()),
                    summaryByPatientId
            );
        }
        applyHighValueFlags(summaryByPatientId.values());
        for (PatientInsightSummary summary : summaryByPatientId.values()) {
            patientInsightSummaryMapper.upsert(summary);
        }
    }

    private PatientInsightSummary buildSingleSummary(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return null;
        }
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients == null || patients.isEmpty() || patients.get(0) == null) {
            patientInsightSummaryMapper.deleteByPatientId(patientId);
            return null;
        }
        PatientInsightSummary summary = buildCoreSummary(
                patientId,
                medicalRecordMapper.selectByPatientId(patientId),
                treatmentMapper.selectByPatientId(patientId),
                financeMapper.selectByPatientIds(List.of(patientId)),
                consultationRecordMapper.selectByPatientId(patientId),
                patientFollowupMapper.selectByPatientId(patientId)
        );
        List<PatientReferralRecord> outgoingReferrals = patientReferralRecordMapper.selectByReferrerPatientId(patientId);
        Map<Long, PatientInsightSummary> referredSummaries = new HashMap<>();
        List<Long> referredPatientIds = outgoingReferrals.stream()
                .filter(Objects::nonNull)
                .map(PatientReferralRecord::getPatient_id)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        for (PatientInsightSummary item : selectByPatientIds(referredPatientIds)) {
            if (item != null && item.getPatient_id() != null) {
                referredSummaries.put(item.getPatient_id(), item);
            }
        }
        applyOutgoingReferralMetrics(summary, outgoingReferrals, referredSummaries);
        return summary;
    }

    private PatientInsightSummary buildCoreSummary(Long patientId,
                                                   List<MedicalRecord> medicalRecords,
                                                   List<Treatment> treatments,
                                                   List<Finance> finances,
                                                   List<ConsultationRecord> consultations,
                                                   List<PatientFollowup> followups) {
        PatientInsightSummary summary = new PatientInsightSummary();
        summary.setPatient_id(patientId);

        List<Date> actualVisitTimes = collectActualVisitTimes(medicalRecords, treatments, consultations);
        Date lastVisitDate = actualVisitTimes.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
        summary.setLast_visit_date(lastVisitDate);

        Set<LocalDate> visitDates = new LinkedHashSet<>();
        for (Date visitTime : actualVisitTimes) {
            LocalDate visitDate = toLocalDate(visitTime);
            if (visitDate != null) {
                visitDates.add(visitDate);
            }
        }
        summary.setTotal_visit_count(visitDates.size());

        LocalDate sixMonthsAgo = LocalDate.now(ZONE_ID).minusMonths(6);
        int visitCountLast6m = 0;
        for (LocalDate visitDate : visitDates) {
            if (visitDate != null && !visitDate.isBefore(sixMonthsAgo)) {
                visitCountLast6m++;
            }
        }
        summary.setVisit_count_last_6m(visitCountLast6m);

        LocalDate lastTreatmentDate = treatments == null ? null : treatments.stream()
                .filter(Objects::nonNull)
                .filter(this::isActiveTreatment)
                .map(Treatment::getTreatment_date)
                .filter(Objects::nonNull)
                .map(java.sql.Date::toLocalDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
        summary.setLast_treatment_date(lastTreatmentDate == null ? null : java.sql.Date.valueOf(lastTreatmentDate));

        summary.setTotal_spent(computeTotalSpent(treatments, finances));

        boolean hasFutureFollowupPlan = hasFutureFollowupPlan(followups);
        boolean lostRiskFlag = false;
        if (lastVisitDate != null) {
            LocalDate lastVisitLocalDate = toLocalDate(lastVisitDate);
            lostRiskFlag = lastVisitLocalDate != null
                    && lastVisitLocalDate.isBefore(LocalDate.now(ZONE_ID).minusDays(180))
                    && !hasFutureFollowupPlan;
        }
        summary.setLost_risk_flag(lostRiskFlag);
        summary.setHigh_value_flag(false);
        summary.setReferred_count(0);
        summary.setReferred_revenue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setWord_of_mouth_flag(false);
        return summary;
    }

    private void applyOutgoingReferralMetrics(PatientInsightSummary summary,
                                              List<PatientReferralRecord> outgoingReferrals,
                                              Map<Long, PatientInsightSummary> summaryByPatientId) {
        if (summary == null) {
            return;
        }
        int referredCount = 0;
        BigDecimal referredRevenue = BigDecimal.ZERO;
        for (PatientReferralRecord referralRecord : safeList(outgoingReferrals)) {
            Long referredPatientId = referralRecord == null ? null : normalizePositiveId(referralRecord.getPatient_id());
            if (referredPatientId == null) {
                continue;
            }
            referredCount++;
            PatientInsightSummary referredSummary = summaryByPatientId == null ? null : summaryByPatientId.get(referredPatientId);
            BigDecimal totalSpent = referredSummary == null || referredSummary.getTotal_spent() == null
                    ? BigDecimal.ZERO
                    : normalizeMoney(referredSummary.getTotal_spent());
            referredRevenue = referredRevenue.add(totalSpent);
        }
        summary.setReferred_count(referredCount);
        summary.setReferred_revenue(referredRevenue.setScale(2, RoundingMode.HALF_UP));
        summary.setWord_of_mouth_flag(referredCount >= 3);
    }

    private void recalculateHighValueFlags() {
        List<PatientInsightSummary> summaries = patientInsightSummaryMapper.selectAll();
        if (summaries == null || summaries.isEmpty()) {
            return;
        }
        applyHighValueFlags(summaries);
        for (PatientInsightSummary summary : summaries) {
            patientInsightSummaryMapper.upsert(summary);
        }
    }

    private void applyHighValueFlags(Collection<PatientInsightSummary> summaries) {
        List<BigDecimal> positiveSpentValues = safeCollection(summaries).stream()
                .filter(Objects::nonNull)
                .map(PatientInsightSummary::getTotal_spent)
                .filter(Objects::nonNull)
                .map(this::normalizeMoney)
                .filter(value -> value.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .toList();
        BigDecimal threshold = BigDecimal.ZERO;
        if (!positiveSpentValues.isEmpty()) {
            int index = Math.max(0, (int) Math.ceil(positiveSpentValues.size() * 0.8D) - 1);
            threshold = positiveSpentValues.get(Math.min(index, positiveSpentValues.size() - 1));
        }
        for (PatientInsightSummary summary : safeCollection(summaries)) {
            BigDecimal totalSpent = summary == null ? BigDecimal.ZERO : normalizeMoney(summary.getTotal_spent());
            boolean highValueFlag = totalSpent.compareTo(BigDecimal.ZERO) > 0 && totalSpent.compareTo(threshold) >= 0;
            if (summary != null) {
                summary.setHigh_value_flag(highValueFlag);
            }
        }
    }

    private BigDecimal computeTotalSpent(List<Treatment> treatments, List<Finance> finances) {
        Map<Long, List<Finance>> financeByTreatmentId = new HashMap<>();
        for (Finance finance : safeList(finances)) {
            Long treatmentId = finance == null ? null : normalizePositiveId(finance.getTreatment_id());
            if (treatmentId == null) {
                continue;
            }
            financeByTreatmentId.computeIfAbsent(treatmentId, key -> new ArrayList<>()).add(finance);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Treatment treatment : safeList(treatments)) {
            if (treatment == null || treatment.getId() == null || treatment.getId() <= 0 || !isActiveTreatment(treatment)) {
                continue;
            }
            List<Finance> treatmentFinances = financeByTreatmentId.getOrDefault(treatment.getId(), List.of());
            boolean hasBillingFinance = false;
            BigDecimal netAmount = BigDecimal.ZERO;
            for (Finance finance : treatmentFinances) {
                if (isChargeFinance(finance)) {
                    hasBillingFinance = true;
                    netAmount = netAmount.add(BigDecimal.valueOf(round2(finance.getAmount())));
                } else if (isRefundFinance(finance)) {
                    hasBillingFinance = true;
                    netAmount = netAmount.subtract(BigDecimal.valueOf(round2(finance.getAmount())));
                }
            }
            if (hasBillingFinance) {
                total = total.add(netAmount);
            } else {
                total = total.add(parseMoney(treatment.getTreatment_fee()));
            }
        }
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isChargeFinance(Finance finance) {
        if (finance == null) {
            return false;
        }
        String bizType = trim(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if ("TREATMENT_CHARGE".equals(bizType)) {
            return true;
        }
        if ("TREATMENT_REFUND".equals(bizType)) {
            return false;
        }
        return trim(finance.getType()).contains("收费");
    }

    private boolean isRefundFinance(Finance finance) {
        if (finance == null) {
            return false;
        }
        String bizType = trim(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if ("TREATMENT_REFUND".equals(bizType)) {
            return true;
        }
        if ("TREATMENT_CHARGE".equals(bizType)) {
            return false;
        }
        return trim(finance.getType()).contains("退款");
    }

    private boolean hasFutureFollowupPlan(List<PatientFollowup> followups) {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        for (PatientFollowup followup : safeList(followups)) {
            if (followup == null || followup.getNext_followup_date() == null) {
                continue;
            }
            LocalDateTime nextFollowup = LocalDateTime.ofInstant(followup.getNext_followup_date().toInstant(), ZONE_ID);
            if (nextFollowup.isAfter(now)) {
                return true;
            }
        }
        return false;
    }

    private List<Date> collectActualVisitTimes(List<MedicalRecord> medicalRecords,
                                               List<Treatment> treatments,
                                               List<ConsultationRecord> consultations) {
        List<Date> result = new ArrayList<>();
        for (MedicalRecord medicalRecord : safeList(medicalRecords)) {
            if (medicalRecord != null && medicalRecord.getVisit_date() != null) {
                result.add(medicalRecord.getVisit_date());
            }
        }
        for (Treatment treatment : safeList(treatments)) {
            if (treatment != null && treatment.getTreatment_date() != null && isActiveTreatment(treatment)) {
                LocalDate treatmentDate = treatment.getTreatment_date().toLocalDate();
                result.add(toUtilDate(treatmentDate.atStartOfDay()));
            }
        }
        for (ConsultationRecord consultation : safeList(consultations)) {
            if (consultation != null && consultation.getArrived_at() != null) {
                result.add(consultation.getArrived_at());
            }
        }
        return result;
    }

    private boolean isActiveTreatment(Treatment treatment) {
        String status = trim(treatment == null ? null : treatment.getStatus());
        return !"取消".equals(status) && !"已取消".equals(status);
    }

    private <T> Map<Long, List<T>> groupByPatientId(List<T> rows, java.util.function.Function<T, Long> patientIdExtractor) {
        Map<Long, List<T>> result = new LinkedHashMap<>();
        for (T row : safeList(rows)) {
            Long patientId = row == null ? null : normalizePositiveId(patientIdExtractor.apply(row));
            if (patientId == null) {
                continue;
            }
            result.computeIfAbsent(patientId, key -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(trim(value)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }
        return value.toInstant().atZone(ZONE_ID).toLocalDate();
    }

    private Date toUtilDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private <T> Collection<T> safeCollection(Collection<T> rows) {
        return rows == null ? List.of() : rows;
    }
}
