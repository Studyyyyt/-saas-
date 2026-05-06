package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientArrearsSummary;
import com.example.springboot.entity.PaymentChannel;
import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.TreatmentBillingChannelSplit;
import com.example.springboot.entity.TreatmentBillingRequest;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.TreatmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class TreatmentBillingService {

    private static final String BIZ_TYPE_TREATMENT_CHARGE = "TREATMENT_CHARGE";
    private static final String BIZ_TYPE_TREATMENT_REFUND = "TREATMENT_REFUND";
    private static final Logger log = LoggerFactory.getLogger(TreatmentBillingService.class);

    private final TreatmentMapper treatmentMapper;
    private final FinanceMapper financeMapper;
    private final PaymentChannelService paymentChannelService;

    @Autowired
    private ConsultationRecordService consultationRecordService;

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    @Autowired
    public TreatmentBillingService(TreatmentMapper treatmentMapper,
                                   FinanceMapper financeMapper,
                                   PaymentChannelService paymentChannelService) {
        this.treatmentMapper = treatmentMapper;
        this.financeMapper = financeMapper;
        this.paymentChannelService = paymentChannelService;
    }

    public void enrichTreatments(List<Treatment> treatments) {
        if (treatments == null || treatments.isEmpty()) {
            return;
        }
        List<Long> treatmentIds = treatments.stream()
                .filter(item -> item != null && item.getId() != null && item.getId() > 0)
                .map(Treatment::getId)
                .distinct()
                .toList();
        Map<Long, List<Finance>> financeByTreatmentId = buildFinanceByTreatmentId(
                treatmentIds.isEmpty() ? List.of() : financeMapper.selectByTreatmentIds(treatmentIds)
        );
        for (Treatment treatment : treatments) {
            if (treatment == null) {
                continue;
            }
            BillingMetrics metrics = buildMetrics(treatment, financeByTreatmentId.get(treatment.getId()));
            applyMetrics(treatment, metrics);
        }
    }

    public void enrichPatients(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            return;
        }
        List<Long> patientIds = patients.stream()
                .filter(item -> item != null && item.getId() > 0)
                .map(item -> (long) item.getId())
                .distinct()
                .toList();
        Map<Long, Double> arrearsByPatientId = summarizePatientArrearsByPatientIds(patientIds);
        for (Patient patient : patients) {
            if (patient == null) {
                continue;
            }
            double amount = arrearsByPatientId.getOrDefault((long) patient.getId(), 0D);
            patient.setHas_arrears(amount > 0.0001);
            patient.setArrears_amount(round2(amount));
        }
    }

    public Map<Long, Double> summarizePatientArrearsByPatientIds(List<Long> patientIds) {
        List<Long> normalizedIds = (patientIds == null ? List.<Long>of() : patientIds).stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Double> result = new HashMap<>();
        for (PatientArrearsSummary item : treatmentMapper.selectPatientArrearsByPatientIds(normalizedIds)) {
            if (item == null || item.getPatient_id() == null || item.getPatient_id() <= 0) {
                continue;
            }
            result.put(item.getPatient_id(), round2(item.getArrears_amount() == null ? 0D : item.getArrears_amount()));
        }
        return result;
    }

    public void enrichAppointments(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return;
        }
        List<Long> patientIds = appointments.stream()
                .filter(item -> item != null && item.getPatient_id() != null && item.getPatient_id() > 0)
                .map(Appointment::getPatient_id)
                .distinct()
                .toList();
        Map<Long, Double> arrearsByPatientId = summarizePatientArrearsByPatientIds(patientIds);
        for (Appointment appointment : appointments) {
            if (appointment == null) {
                continue;
            }
            Long patientId = appointment.getPatient_id();
            double amount = patientId != null && patientId > 0
                    ? arrearsByPatientId.getOrDefault(patientId, 0D)
                    : 0D;
            appointment.setHas_arrears(amount > 0.0001);
            appointment.setArrears_amount(round2(amount));
        }
    }

    @Transactional
    public Finance chargeTreatment(Long treatmentId, TreatmentBillingRequest request) {
        Treatment treatment = getTreatmentOrThrow(treatmentId);
        BillingMetrics metrics = validateChargeableTreatment(treatment);
        double requestedAmount = request != null && request.getAmount() != null
                ? round2(request.getAmount())
                : metrics.feeAmount;
        if (requestedAmount <= 0) {
            throw new IllegalArgumentException("收费金额必须大于0");
        }
        if (Math.abs(requestedAmount - metrics.feeAmount) > 0.0001) {
            treatment.setTreatment_fee(formatAmount(requestedAmount));
            treatmentMapper.editTreatment(treatment);
        }
        List<Finance> finances = createChargeFinances(List.of(treatment), List.of(requestedAmount), request, "治疗收费");
        return finances.isEmpty() ? null : finances.get(0);
    }

    @Transactional
    public List<Finance> chargeTreatmentBatch(String batchNo, TreatmentBillingRequest request) {
        String normalizedBatchNo = normalizeText(batchNo);
        if (normalizedBatchNo.isEmpty()) {
            throw new IllegalArgumentException("处置批次号不能为空");
        }
        List<Treatment> treatments = treatmentMapper.selectByBatchNo(normalizedBatchNo);
        if (treatments == null || treatments.isEmpty()) {
            throw new IllegalArgumentException("处置批次不存在");
        }
        List<Double> treatmentAmounts = new ArrayList<>();
        double totalAmount = 0D;
        for (Treatment treatment : treatments) {
            BillingMetrics metrics = validateChargeableTreatment(treatment);
            treatmentAmounts.add(metrics.feeAmount);
            totalAmount += metrics.feeAmount;
        }
        totalAmount = round2(totalAmount);
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("批量收费金额必须大于0");
        }
        double requestedAmount = request != null && request.getAmount() != null
                ? round2(request.getAmount())
                : totalAmount;
        if (Math.abs(requestedAmount - totalAmount) > 0.0001) {
            throw new IllegalArgumentException("批量收费金额必须等于本批次汇总待收金额");
        }
        return createChargeFinances(treatments, treatmentAmounts, request, "批量治疗收费");
    }

    @Transactional
    public Finance refundTreatment(Long treatmentId, TreatmentBillingRequest request) {
        Treatment treatment = getTreatmentOrThrow(treatmentId);
        BillingMetrics metrics = buildMetrics(treatment, financeMapper.getFinancesByTreatmentId(treatmentId));
        double refundable = round2(metrics.chargeAmount - metrics.refundAmount);
        if (refundable <= 0.0001) {
            throw new IllegalArgumentException("当前处置没有可退款金额");
        }
        double requestedAmount = request != null && request.getAmount() != null ? request.getAmount() : refundable;
        if (requestedAmount <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        if (requestedAmount - refundable > 0.0001) {
            throw new IllegalArgumentException("退款金额不能大于已收未退金额");
        }

        Finance finance = new Finance();
        finance.setPatient_id(treatment.getPatient_id());
        finance.setTreatment_id(treatmentId);
        finance.setName(buildFinanceName(treatment));
        finance.setAmount(round2(requestedAmount));
        finance.setDate(resolveFinanceDate(request));
        finance.setType("治疗退款");
        finance.setBiz_type(BIZ_TYPE_TREATMENT_REFUND);
        finance.setRemark(resolveRemark(request, "治疗退款"));
        financeMapper.addFinance(finance);
        refreshPatientInsight(treatment.getPatient_id());
        return finance;
    }

    private Treatment getTreatmentOrThrow(Long treatmentId) {
        if (treatmentId == null || treatmentId <= 0) {
            throw new IllegalArgumentException("处置记录ID不能为空");
        }
        List<Treatment> treatments = treatmentMapper.selectById(treatmentId);
        if (treatments == null || treatments.isEmpty() || treatments.get(0) == null) {
            throw new IllegalArgumentException("处置记录不存在");
        }
        return treatments.get(0);
    }

    private String buildFinanceName(Treatment treatment) {
        String patientName = normalizeText(treatment.getPatient_name());
        String purpose = normalizeText(treatment.getAppointment_purpose());
        if (!purpose.isEmpty()) {
            return patientName + " - " + purpose;
        }
        return patientName.isEmpty() ? "治疗收费" : patientName;
    }

    private String resolveFinanceDate(TreatmentBillingRequest request) {
        if (request != null && request.getDate() != null && !request.getDate().trim().isEmpty()) {
            return request.getDate().trim();
        }
        return LocalDate.now().toString();
    }

    private String resolveRemark(TreatmentBillingRequest request, String prefix) {
        String extra = request == null || request.getRemark() == null ? "" : request.getRemark().trim();
        if (extra.isEmpty()) {
            return prefix;
        }
        return prefix + "｜" + extra;
    }

    private BillingMetrics validateChargeableTreatment(Treatment treatment) {
        BillingMetrics metrics = buildMetrics(treatment, financeMapper.getFinancesByTreatmentId(treatment.getId()));
        if (metrics.cancelled) {
            throw new IllegalArgumentException("已取消的处置不可收费");
        }
        if (metrics.netPaidAmount > 0.0001) {
            throw new IllegalArgumentException("当前处置已存在收费记录，系统不支持分次收费");
        }
        if (metrics.feeAmount <= 0) {
            throw new IllegalArgumentException("收费金额必须大于0");
        }
        return metrics;
    }

    private List<Finance> createChargeFinances(List<Treatment> treatments,
                                               List<Double> treatmentAmounts,
                                               TreatmentBillingRequest request,
                                               String remarkPrefix) {
        if (treatments == null || treatments.isEmpty()) {
            return List.of();
        }
        double totalAmount = round2(treatmentAmounts.stream().mapToDouble(Double::doubleValue).sum());
        List<ResolvedChannelSplit> channelSplits = resolveChannelSplits(request, totalAmount);
        List<Finance> created = new ArrayList<>();
        for (int index = 0; index < treatments.size(); index++) {
            Treatment treatment = treatments.get(index);
            double treatmentAmount = round2(treatmentAmounts.get(index));
            if (treatmentAmount <= 0) {
                continue;
            }
            List<Double> allocatedChannelAmounts = allocateByWeight(channelSplits.stream()
                    .map(item -> item.amount)
                    .toList(), treatmentAmount);
            for (int splitIndex = 0; splitIndex < channelSplits.size(); splitIndex++) {
                double amount = round2(allocatedChannelAmounts.get(splitIndex));
                if (amount <= 0) {
                    continue;
                }
                ResolvedChannelSplit split = channelSplits.get(splitIndex);
                Finance finance = new Finance();
                finance.setPatient_id(treatment.getPatient_id());
                finance.setTreatment_id(treatment.getId());
                finance.setPayment_channel_id(split.paymentChannelId);
                finance.setPayment_channel_name(split.paymentChannelName);
                finance.setName(buildFinanceName(treatment));
                finance.setAmount(amount);
                finance.setDate(resolveFinanceDate(request));
                finance.setType("治疗收费");
                finance.setBiz_type(BIZ_TYPE_TREATMENT_CHARGE);
                finance.setRemark(resolveRemark(request, remarkPrefix));
                financeMapper.addFinance(finance);
                created.add(finance);
            }
        }
        markFirstDeals(treatments, resolveDealAt(request));
        refreshPatientInsights(treatments);
        return created;
    }

    private void refreshPatientInsights(List<Treatment> treatments) {
        if (patientInsightSummaryService == null || treatments == null || treatments.isEmpty()) {
            return;
        }
        List<Long> patientIds = treatments.stream()
                .filter(Objects::nonNull)
                .map(Treatment::getPatient_id)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (!patientIds.isEmpty()) {
            patientInsightSummaryService.refreshPatients(patientIds);
        }
    }

    private void refreshPatientInsight(Long patientId) {
        if (patientInsightSummaryService != null && patientId != null && patientId > 0) {
            patientInsightSummaryService.refreshPatients(List.of(patientId));
        }
    }

    private Date resolveDealAt(TreatmentBillingRequest request) {
      if (request == null || !StringUtils.hasText(request.getDate())) {
          return new Date();
      }
      try {
          LocalDate dealDate = LocalDate.parse(request.getDate().trim());
          return Date.from(dealDate.atTime(LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault()).toInstant());
      } catch (Exception ignored) {
          return new Date();
      }
    }

    private List<ResolvedChannelSplit> resolveChannelSplits(TreatmentBillingRequest request, double totalAmount) {
        List<TreatmentBillingChannelSplit> rawSplits = request == null ? null : request.getChannel_splits();
        if (rawSplits == null || rawSplits.isEmpty()) {
            return List.of(new ResolvedChannelSplit(null, "", round2(totalAmount)));
        }
        List<ResolvedChannelSplit> resolved = new ArrayList<>();
        double sum = 0D;
        for (TreatmentBillingChannelSplit split : rawSplits) {
            if (split == null) {
                continue;
            }
            double amount = round2(split.getAmount() == null ? 0D : split.getAmount());
            if (amount <= 0) {
                throw new IllegalArgumentException("每个收款渠道金额都必须大于0");
            }
            Long channelId = split.getPayment_channel_id() != null && split.getPayment_channel_id() > 0
                    ? split.getPayment_channel_id()
                    : null;
            String channelName = normalizeText(split.getPayment_channel_name());
            if (channelId != null) {
                PaymentChannel channel = paymentChannelService == null ? null : paymentChannelService.selectById(channelId);
                if (channel == null) {
                    throw new IllegalArgumentException("收款渠道不存在");
                }
                channelName = normalizeText(channel.getChannel_name());
                if (channel.getStatus() == null || channel.getStatus() != 1) {
                    throw new IllegalArgumentException("收款渠道未启用");
                }
            }
            if (!StringUtils.hasText(channelName)) {
                throw new IllegalArgumentException("请选择收款渠道");
            }
            resolved.add(new ResolvedChannelSplit(channelId, channelName, amount));
            sum += amount;
        }
        sum = round2(sum);
        if (resolved.isEmpty()) {
            throw new IllegalArgumentException("请至少填写一条收款渠道");
        }
        if (Math.abs(sum - round2(totalAmount)) > 0.0001) {
            throw new IllegalArgumentException("收款渠道金额合计必须等于收费金额");
        }
        return resolved;
    }

    private List<Double> allocateByWeight(List<Double> weights, double totalAmount) {
        List<Double> result = new ArrayList<>();
        if (weights == null || weights.isEmpty()) {
            return result;
        }
        if (weights.size() == 1) {
            result.add(round2(totalAmount));
            return result;
        }
        double weightTotal = weights.stream().mapToDouble(item -> item == null ? 0D : item).sum();
        if (weightTotal <= 0) {
            double evenAmount = round2(totalAmount / weights.size());
            double allocated = 0D;
            for (int index = 0; index < weights.size(); index++) {
                double amount = index == weights.size() - 1 ? round2(totalAmount - allocated) : evenAmount;
                result.add(amount);
                allocated = round2(allocated + amount);
            }
            return result;
        }
        double allocated = 0D;
        for (int index = 0; index < weights.size(); index++) {
            double amount;
            if (index == weights.size() - 1) {
                amount = round2(totalAmount - allocated);
            } else {
                double weight = weights.get(index) == null ? 0D : weights.get(index);
                amount = round2(totalAmount * weight / weightTotal);
                allocated = round2(allocated + amount);
            }
            result.add(Math.max(0D, amount));
        }
        return result;
    }

    private Map<Long, List<Finance>> buildFinanceByTreatmentId(List<Finance> finances) {
        Map<Long, List<Finance>> result = new HashMap<>();
        if (finances == null) {
            return result;
        }
        for (Finance finance : finances) {
            if (finance == null || finance.getTreatment_id() == null || finance.getTreatment_id() <= 0) {
                continue;
            }
            result.computeIfAbsent(finance.getTreatment_id(), key -> new ArrayList<>()).add(finance);
        }
        return result;
    }

    private BillingMetrics buildMetrics(Treatment treatment, List<Finance> linkedFinances) {
        BillingMetrics metrics = new BillingMetrics();
        metrics.feeAmount = parseAmount(treatment.getTreatment_fee());
        metrics.cancelled = isCancelledTreatment(treatment.getStatus());
        if (linkedFinances != null) {
            for (Finance finance : linkedFinances) {
                if (finance == null) {
                    continue;
                }
                double amount = round2(finance.getAmount());
                if (amount <= 0) {
                    continue;
                }
                if (isRefundFinance(finance)) {
                    metrics.refundAmount += amount;
                } else if (isChargeFinance(finance)) {
                    metrics.chargeAmount += amount;
                }
            }
        }
        metrics.chargeAmount = round2(metrics.chargeAmount);
        metrics.refundAmount = round2(metrics.refundAmount);
        metrics.netPaidAmount = round2(Math.max(0D, metrics.chargeAmount - metrics.refundAmount));

        if (!metrics.cancelled && metrics.feeAmount > 0) {
            metrics.arrearsAmount = round2(Math.max(0D, metrics.feeAmount - metrics.netPaidAmount));
        } else {
            metrics.arrearsAmount = 0D;
        }

        if (metrics.cancelled) {
            metrics.billingStatus = "已取消";
            metrics.canCharge = false;
            metrics.canRefund = metrics.netPaidAmount > 0.0001;
            return metrics;
        }

        if (metrics.feeAmount <= 0) {
            metrics.billingStatus = "无需收费";
            metrics.canCharge = false;
            metrics.canRefund = metrics.netPaidAmount > 0.0001;
            return metrics;
        }

        metrics.canCharge = metrics.netPaidAmount <= 0.0001;
        metrics.canRefund = metrics.netPaidAmount > 0.0001;

        if (metrics.netPaidAmount <= 0.0001 && metrics.chargeAmount <= 0.0001) {
            metrics.billingStatus = "待收费";
        } else if (metrics.arrearsAmount > 0.0001) {
            metrics.billingStatus = "欠费";
        } else if (metrics.refundAmount >= metrics.chargeAmount - 0.0001) {
            metrics.billingStatus = "已退款";
        } else {
            metrics.billingStatus = "已收费";
        }
        return metrics;
    }

    private void applyMetrics(Treatment treatment, BillingMetrics metrics) {
        treatment.setCharged_amount(round2(metrics.chargeAmount));
        treatment.setRefunded_amount(round2(metrics.refundAmount));
        treatment.setArrears_amount(round2(metrics.arrearsAmount));
        treatment.setBilling_status(metrics.billingStatus);
        treatment.setCan_charge(metrics.canCharge);
        treatment.setCan_refund(metrics.canRefund);
    }

    private boolean isCancelledTreatment(String status) {
        String value = normalizeText(status);
        return "取消".equals(value) || "已取消".equals(value);
    }

    private boolean isChargeFinance(Finance finance) {
        String bizType = normalizeText(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if (BIZ_TYPE_TREATMENT_CHARGE.equals(bizType)) {
            return true;
        }
        if (BIZ_TYPE_TREATMENT_REFUND.equals(bizType)) {
            return false;
        }
        String type = normalizeText(finance.getType());
        return type.contains("收入") || type.contains("收费");
    }

    private boolean isRefundFinance(Finance finance) {
        String bizType = normalizeText(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if (BIZ_TYPE_TREATMENT_REFUND.equals(bizType)) {
            return true;
        }
        if (BIZ_TYPE_TREATMENT_CHARGE.equals(bizType)) {
            return false;
        }
        return normalizeText(finance.getType()).contains("退款");
    }

    private double parseAmount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return round2(Double.parseDouble(value.trim()));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String formatAmount(double value) {
        return String.format(Locale.ROOT, "%.2f", round2(value));
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private void markFirstDeals(List<Treatment> treatments, Date dealAt) {
        if (consultationRecordService == null || treatments == null || treatments.isEmpty()) {
            return;
        }
        Date effectiveDealAt = dealAt == null ? new Date() : dealAt;
        Set<Long> patientIds = new LinkedHashSet<>();
        for (Treatment treatment : treatments) {
            if (treatment != null && treatment.getPatient_id() != null && treatment.getPatient_id() > 0) {
                patientIds.add(treatment.getPatient_id());
            }
        }
        for (Long patientId : patientIds) {
            if (patientId == null || patientId <= 0) {
                continue;
            }
            if (consultationRecordService.markFirstDealByPatientId(patientId, effectiveDealAt) == null) {
                log.warn("No consultation record found for first-deal writeback, patientId={}", patientId);
            }
        }
    }

    private static class BillingMetrics {
        private double feeAmount;
        private double chargeAmount;
        private double refundAmount;
        private double netPaidAmount;
        private double arrearsAmount;
        private boolean cancelled;
        private String billingStatus;
        private boolean canCharge;
        private boolean canRefund;
    }

    private static class ResolvedChannelSplit {
        private final Long paymentChannelId;
        private final String paymentChannelName;
        private final double amount;

        private ResolvedChannelSplit(Long paymentChannelId, String paymentChannelName, double amount) {
            this.paymentChannelId = paymentChannelId;
            this.paymentChannelName = paymentChannelName;
            this.amount = amount;
        }
    }

}
