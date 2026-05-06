package com.example.springboot.service;

import com.example.springboot.entity.DoctorPerformanceReport;
import com.example.springboot.entity.DoctorPerformanceStat;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.TreatmentOperationAllocation;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.TreatmentMapper;
import com.example.springboot.util.FinanceExpenseClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private TreatmentMapper treatmentMapper;

    @Autowired
    private TreatmentBillingService treatmentBillingService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TreatmentOperationAllocationService treatmentOperationAllocationService;

    public List<Finance> getAllFinances() {
        return financeMapper.getAllFinances();
    }

    public List<Finance> getRecentFinancesByPatientId(Long patientId, Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return financeMapper.getRecentFinancesByPatientId(patientId, safeLimit);
    }

    public List<Finance> getFinancesByTreatmentId(Long treatmentId) {
        return financeMapper.getFinancesByTreatmentId(treatmentId);
    }

    public List<Finance> getFinanceByid(Long id) {
        return financeMapper.getFinanceByid(id);
    }

    public List<Finance> getFinanceByname(String name) {
        return financeMapper.getFinanceByname(name);
    }

    public List<Finance> getFinanceByamount(int amount) {
        return financeMapper.getFinanceByamount(amount);
    }

    public List<Finance> getFinanceBytype(String type) {
        return financeMapper.getFinanceBytype(type);
    }

    public void addFinance(Finance finance) {
        financeMapper.addFinance(finance);
    }

    public Map<String, Object> buildExpenseOverview(String startDate, String endDate) {
        LocalDate[] range = resolveExpenseDateRange(startDate, endDate);
        LocalDate rangeStart = range[0];
        LocalDate rangeEnd = range[1];
        List<Finance> scopedExpenses = financeMapper.getAllFinances().stream()
                .filter(FinanceExpenseClassifier::isOperatingExpense)
                .filter(item -> isFinanceDateWithinRange(item, rangeStart, rangeEnd))
                .sorted(Comparator.comparing(Finance::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Finance::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        double materialExpense = sumExpenseByScope(scopedExpenses, FinanceExpenseClassifier.ExpenseScope.MATERIAL);
        double labExpense = sumExpenseByScope(scopedExpenses, FinanceExpenseClassifier.ExpenseScope.LAB);
        double otherExpense = sumExpenseByScope(scopedExpenses, FinanceExpenseClassifier.ExpenseScope.OTHER);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("start_date", rangeStart.toString());
        result.put("end_date", rangeEnd.toString());
        result.put("total_expense", round2(materialExpense + labExpense + otherExpense));
        result.put("material_expense", materialExpense);
        result.put("lab_expense", labExpense);
        result.put("other_expense", otherExpense);
        result.put("material_count", countExpenseByScope(scopedExpenses, FinanceExpenseClassifier.ExpenseScope.MATERIAL));
        result.put("lab_count", countExpenseByScope(scopedExpenses, FinanceExpenseClassifier.ExpenseScope.LAB));
        result.put("other_count", countExpenseByScope(scopedExpenses, FinanceExpenseClassifier.ExpenseScope.OTHER));
        result.put("trend", buildExpenseTrend(scopedExpenses, rangeStart, rangeEnd));
        return result;
    }

    public List<Finance> searchManualExpenses(String startDate, String endDate, String keyword) {
        LocalDate[] range = resolveExpenseDateRange(startDate, endDate);
        LocalDate rangeStart = range[0];
        LocalDate rangeEnd = range[1];
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        return financeMapper.getAllFinances().stream()
                .filter(FinanceExpenseClassifier::isManualExpenseEditable)
                .filter(item -> isFinanceDateWithinRange(item, rangeStart, rangeEnd))
                .filter(item -> normalizedKeyword.isEmpty()
                        || normalizeText(item.getName()).toLowerCase().contains(normalizedKeyword)
                        || normalizeText(item.getRemark()).toLowerCase().contains(normalizedKeyword))
                .sorted(Comparator.comparing(Finance::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Finance::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Finance addManualExpense(Finance finance) {
        Finance normalized = normalizeManualExpense(finance);
        financeMapper.addFinance(normalized);
        return normalized;
    }

    public Finance editManualExpense(Finance finance) {
        if (finance == null || finance.getId() <= 0) {
            throw new IllegalArgumentException("支出记录ID不能为空");
        }
        Finance existing = requireFinance(finance.getId());
        if (!FinanceExpenseClassifier.isManualExpenseEditable(existing)) {
            throw new IllegalArgumentException("该支出来源于业务模块，不允许在此编辑");
        }
        Finance normalized = normalizeManualExpense(finance);
        normalized.setId(existing.getId());
        financeMapper.editFinance(normalized);
        return requireFinance(existing.getId());
    }

    public void deleteManualExpense(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("支出记录ID不能为空");
        }
        Finance existing = requireFinance(id.intValue());
        if (!FinanceExpenseClassifier.isManualExpenseEditable(existing)) {
            throw new IllegalArgumentException("该支出来源于业务模块，不允许在此删除");
        }
        financeMapper.deleteFinance(existing.getId());
    }

    /**
     * 记录一笔支出到 finances 表
     * 供其他业务模块调用，统一支出写入入口
     *
     * @param patientId 关联患者ID（可空，非患者直接关联的支出可空）
     * @param treatmentId 关联治疗ID（可空）
     * @param amount 支出金额（正数）
     * @param expenseCategory 支出分类，如"义齿加工"、"耗材采购"、"其他"
     * @param remark 备注
     * @param bizType 业务类型（用于追溯来源，如"lab_order"、"material_purchase"）
     * @param bizId 业务来源ID（如义齿加工订单ID、耗材采购单ID）
     * @return 写入的 finances 记录ID
     */
    public Long recordExpense(Long patientId,
                              Long treatmentId,
                              BigDecimal amount,
                              String expenseCategory,
                              String remark,
                              String bizType,
                              String bizId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支出金额必须大于0");
        }

        Finance finance = new Finance();
        finance.setPatient_id(normalizeReferenceId(patientId));
        finance.setTreatment_id(normalizeReferenceId(treatmentId));
        finance.setName(StringUtils.hasText(expenseCategory) ? expenseCategory.trim() : "其他");
        finance.setAmount(amount.setScale(2, RoundingMode.HALF_UP).doubleValue());
        finance.setDate(LocalDate.now(ZoneId.systemDefault()).toString());
        finance.setType("支出");
        finance.setBiz_type(trimToNull(bizType));
        finance.setRemark(buildExpenseRemark(remark, bizId));
        financeMapper.addFinance(finance);
        return (long) finance.getId();
    }

    public void editFinance(Finance finance) {
        financeMapper.editFinance(finance);
    }

    public void updateFinance(Finance finance) {
        financeMapper.updateFinance(finance);
    }

    public void deleteFinance(int id) {
        financeMapper.deleteFinance(id);
    }

    public void deleteByTreatmentId(Long treatmentId) {
        financeMapper.deleteByTreatmentId(treatmentId);
    }

    public void deleteByPatientId(Long patientId) {
        financeMapper.deleteByPatientId(patientId);
    }

    public List<Finance> getFinancesByMonth(Integer year, Integer month) {
        return financeMapper.getFinancesByMonth(year, month);
    }

    public List<Finance> getFinanceByidAndMonth(Long id, Integer year, Integer month) {
        return financeMapper.getFinancesByidAndMonth(id, year, month);
    }

    public List<Finance> getFinanceBynameAndMonth(String name, Integer year, Integer month) {
        return financeMapper.getFinancesBynameAndMonth(name, year, month);
    }

    public List<Finance> getFinanceByamountAndMonth(String amount, Integer year, Integer month) {
        return financeMapper.getFinancesByamountAndMonth(amount, year, month);
    }

    public List<Finance> getFinanceBytypeAndMonth(String type, Integer year, Integer month) {
        return financeMapper.getFinancesBytypeAndMonth(type, year, month);
    }

    public List<Finance> getFinanceBydateAndMonth(String date, Integer year, Integer month) {
        return financeMapper.getFinancesBydateAndMonth(date, year, month);
    }

    public List<Finance> getFinanceBydate(String date) {
        return financeMapper.getFinanceBydate(date);
    }

    public DoctorPerformanceReport getDoctorPerformance(String startDate,
                                                        String endDate,
                                                        Long doctorAccountId,
                                                        String doctorName) {
        LocalDate[] dateRange = resolveDateRange(startDate, endDate);
        LocalDate rangeStart = dateRange[0];
        LocalDate rangeEnd = dateRange[1];
        Long targetDoctorAccountId = normalizeDoctorAccountId(doctorAccountId);
        String targetDoctorName = normalizeDoctorNameForFilter(targetDoctorAccountId, doctorName);

        List<Treatment> treatments = new ArrayList<>(treatmentMapper.selectAll());
        treatmentBillingService.enrichTreatments(treatments);
        List<Long> treatmentIds = treatments.stream()
                .filter(item -> item != null && item.getId() != null && item.getId() > 0)
                .map(Treatment::getId)
                .distinct()
                .toList();
        Map<Long, List<TreatmentOperationAllocation>> allocationsByTreatmentId = buildAllocationsByTreatmentId(
                treatmentOperationAllocationService == null ? List.of() : treatmentOperationAllocationService.selectByTreatmentIds(treatmentIds)
        );

        Map<String, DoctorPerformanceStat> statsByDoctor = new LinkedHashMap<>();
        DoctorPerformanceStat summary = createEmptyStat(null, "合计");
        for (Treatment treatment : treatments) {
            if (treatment == null || !isTreatmentInRange(treatment, rangeStart, rangeEnd) || isCancelledTreatment(treatment)) {
                continue;
            }
            if (!matchesDoctorFilter(treatment, targetDoctorAccountId, targetDoctorName)) {
                continue;
            }

            double turnoverAmount = parseAmount(treatment.getTreatment_fee());
            double refundedAmount = safeAmount(treatment.getRefunded_amount());
            double arrearsAmount = safeAmount(treatment.getArrears_amount());
            double receivedAmount = round2(Math.max(0D, safeAmount(treatment.getCharged_amount()) - refundedAmount));
            List<TreatmentOperationAllocation> allocations = allocationsByTreatmentId.getOrDefault(treatment.getId(), List.of());
            if (!allocations.isEmpty()) {
                List<Double> ratios = allocations.stream()
                        .map(item -> safeAmount(item.getAllocation_ratio()))
                        .toList();
                List<Double> receivedAllocations = allocateByRatios(ratios, receivedAmount);
                List<Double> refundedAllocations = allocateByRatios(ratios, refundedAmount);
                List<Double> arrearsAllocations = allocateByRatios(ratios, arrearsAmount);
                boolean handledByAllocations = false;
                for (int index = 0; index < allocations.size(); index++) {
                    TreatmentOperationAllocation allocation = allocations.get(index);
                    if (allocation == null || safeAmount(allocation.getAllocation_ratio()) <= 0.0000001) {
                        continue;
                    }
                    if (!matchesDoctorFilter(allocation.getDoctor_account_id(), allocation.getDoctor_name(), targetDoctorAccountId, targetDoctorName)) {
                        continue;
                    }
                    Long allocationDoctorAccountId = normalizeDoctorAccountId(allocation.getDoctor_account_id());
                    String normalizedDoctorName = normalizeDoctorName(allocation.getDoctor_name());
                    String doctorKey = buildDoctorStatKey(allocationDoctorAccountId, normalizedDoctorName);
                    DoctorPerformanceStat stat = statsByDoctor.computeIfAbsent(doctorKey,
                            key -> createEmptyStat(allocationDoctorAccountId, normalizedDoctorName));
                    accumulateStat(
                            stat,
                            safeAmount(allocation.getAllocated_turnover_amount()),
                            receivedAllocations.get(index),
                            refundedAllocations.get(index),
                            arrearsAllocations.get(index)
                    );
                    accumulateStat(
                            summary,
                            safeAmount(allocation.getAllocated_turnover_amount()),
                            receivedAllocations.get(index),
                            refundedAllocations.get(index),
                            arrearsAllocations.get(index)
                    );
                    handledByAllocations = true;
                }
                if (!handledByAllocations) {
                    continue;
                }
            } else {
                Long treatmentDoctorAccountId = normalizeDoctorAccountId(treatment.getDoctor_account_id());
                String normalizedDoctorName = normalizeDoctorName(treatment.getDoctor_name());
                String doctorKey = buildDoctorStatKey(treatmentDoctorAccountId, normalizedDoctorName);
                DoctorPerformanceStat stat = statsByDoctor.computeIfAbsent(doctorKey,
                        key -> createEmptyStat(treatmentDoctorAccountId, normalizedDoctorName));
                accumulateStat(stat, turnoverAmount, receivedAmount, refundedAmount, arrearsAmount);
                accumulateStat(summary, turnoverAmount, receivedAmount, refundedAmount, arrearsAmount);
            }
        }

        List<DoctorPerformanceStat> list = statsByDoctor.values().stream()
                .sorted(Comparator.comparingDouble((DoctorPerformanceStat item) -> safeAmount(item.getTurnover_amount())).reversed()
                        .thenComparing(Comparator.comparingDouble((DoctorPerformanceStat item) -> safeAmount(item.getReceived_amount())).reversed())
                        .thenComparing(Comparator.comparingInt((DoctorPerformanceStat item) -> item.getProject_count() == null ? 0 : item.getProject_count()).reversed()))
                .toList();

        DoctorPerformanceReport report = new DoctorPerformanceReport();
        report.setStart_date(rangeStart.toString());
        report.setEnd_date(rangeEnd.toString());
        report.setDoctor_count(list.size());
        report.setList(list);
        report.setSummary(summary);
        return report;
    }

    private LocalDate[] resolveDateRange(String startDate, String endDate) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate defaultStart = today.withDayOfMonth(1);
        LocalDate rangeStart = parseDateOrDefault(startDate, defaultStart);
        LocalDate rangeEnd = parseDateOrDefault(endDate, today);
        if (rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        return new LocalDate[]{rangeStart, rangeEnd};
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd");
        }
    }

    private boolean isTreatmentInRange(Treatment treatment, LocalDate rangeStart, LocalDate rangeEnd) {
        if (treatment.getTreatment_date() == null) {
            return false;
        }
        LocalDate treatmentDate = treatment.getTreatment_date().toLocalDate();
        return !treatmentDate.isBefore(rangeStart) && !treatmentDate.isAfter(rangeEnd);
    }

    private boolean isCancelledTreatment(Treatment treatment) {
        String status = trim(treatment.getStatus());
        return "取消".equals(status) || "已取消".equals(status);
    }

    private Long normalizeReferenceId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String buildExpenseRemark(String remark, String bizId) {
        String normalizedRemark = StringUtils.hasText(remark) ? remark.trim() : "";
        String normalizedBizId = StringUtils.hasText(bizId) ? bizId.trim() : "";
        if (normalizedBizId.isEmpty()) {
            return normalizedRemark;
        }
        if (normalizedRemark.isEmpty()) {
            return "bizId=" + normalizedBizId;
        }
        return "bizId=" + normalizedBizId + " | " + normalizedRemark;
    }

    private LocalDate[] resolveExpenseDateRange(String startDate, String endDate) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate rangeStart = parseDateOrDefault(startDate, monthStart);
        LocalDate rangeEnd = parseDateOrDefault(endDate, today);
        if (rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        return new LocalDate[]{rangeStart, rangeEnd};
    }

    private boolean isFinanceDateWithinRange(Finance finance, LocalDate startDate, LocalDate endDate) {
        if (finance == null || !StringUtils.hasText(finance.getDate())) {
            return false;
        }
        try {
            LocalDate current = LocalDate.parse(finance.getDate().trim());
            return !current.isBefore(startDate) && !current.isAfter(endDate);
        } catch (Exception exception) {
            return false;
        }
    }

    private double sumExpenseByScope(List<Finance> finances, FinanceExpenseClassifier.ExpenseScope scope) {
        return round2(finances.stream()
                .filter(item -> FinanceExpenseClassifier.resolveOperatingExpenseScope(item) == scope)
                .mapToDouble(Finance::getAmount)
                .sum());
    }

    private int countExpenseByScope(List<Finance> finances, FinanceExpenseClassifier.ExpenseScope scope) {
        return (int) finances.stream()
                .filter(item -> FinanceExpenseClassifier.resolveOperatingExpenseScope(item) == scope)
                .count();
    }

    private List<Map<String, Object>> buildExpenseTrend(List<Finance> finances, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, double[]> grouped = new LinkedHashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            grouped.put(cursor, new double[]{0D, 0D, 0D});
            cursor = cursor.plusDays(1);
        }
        for (Finance finance : finances) {
            try {
                LocalDate current = LocalDate.parse(finance.getDate().trim());
                double[] totals = grouped.get(current);
                if (totals == null) {
                    continue;
                }
                FinanceExpenseClassifier.ExpenseScope scope = FinanceExpenseClassifier.resolveOperatingExpenseScope(finance);
                if (scope == FinanceExpenseClassifier.ExpenseScope.MATERIAL) {
                    totals[0] += finance.getAmount();
                } else if (scope == FinanceExpenseClassifier.ExpenseScope.LAB) {
                    totals[1] += finance.getAmount();
                } else if (scope == FinanceExpenseClassifier.ExpenseScope.OTHER) {
                    totals[2] += finance.getAmount();
                }
            } catch (Exception ignored) {
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> entry : grouped.entrySet()) {
            double[] totals = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", entry.getKey().toString());
            row.put("material_expense", round2(totals[0]));
            row.put("lab_expense", round2(totals[1]));
            row.put("other_expense", round2(totals[2]));
            row.put("total_expense", round2(totals[0] + totals[1] + totals[2]));
            trend.add(row);
        }
        return trend;
    }

    private Finance normalizeManualExpense(Finance finance) {
        if (finance == null) {
            throw new IllegalArgumentException("支出信息不能为空");
        }
        String name = trimToNull(finance.getName());
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("支出项目不能为空");
        }
        double amount = round2(finance.getAmount());
        if (amount <= 0D) {
            throw new IllegalArgumentException("支出金额必须大于0");
        }
        LocalDate date = parseDateOrDefault(finance.getDate(), LocalDate.now(ZoneId.systemDefault()));

        Finance normalized = new Finance();
        normalized.setId(finance.getId());
        normalized.setPatient_id(null);
        normalized.setTreatment_id(null);
        normalized.setPayment_channel_id(null);
        normalized.setPayment_channel_name(null);
        normalized.setName(name);
        normalized.setAmount(amount);
        normalized.setDate(date.toString());
        normalized.setType("支出");
        normalized.setBiz_type(FinanceExpenseClassifier.BIZ_TYPE_MANUAL_EXPENSE);
        normalized.setRemark(trimToNull(finance.getRemark()));
        return normalized;
    }

    private Finance requireFinance(int id) {
        List<Finance> rows = financeMapper.getFinanceByid((long) id);
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            throw new IllegalArgumentException("财务记录不存在");
        }
        return rows.get(0);
    }

    private boolean matchesDoctorFilter(Treatment treatment,
                                        Long targetDoctorAccountId,
                                        String targetDoctorName) {
        if (targetDoctorAccountId == null && !StringUtils.hasText(targetDoctorName)) {
            return true;
        }
        Long treatmentDoctorAccountId = normalizeDoctorAccountId(treatment.getDoctor_account_id());
        String treatmentDoctorName = normalizeDoctorName(treatment.getDoctor_name());
        if (targetDoctorAccountId != null && targetDoctorAccountId.equals(treatmentDoctorAccountId)) {
            return true;
        }
        return StringUtils.hasText(targetDoctorName) && targetDoctorName.equals(treatmentDoctorName);
    }

    private Long normalizeDoctorAccountId(Long doctorAccountId) {
        return doctorAccountId != null && doctorAccountId > 0 ? doctorAccountId : null;
    }

    private String normalizeDoctorNameForFilter(Long doctorAccountId, String doctorName) {
        if (doctorAccountId != null) {
            String displayName = accountService.findDoctorDisplayNameByAccountId(doctorAccountId);
            if (StringUtils.hasText(displayName)) {
                return displayName.trim();
            }
        }
        return StringUtils.hasText(doctorName) ? doctorName.trim() : "";
    }

    private String buildDoctorStatKey(Long doctorAccountId, String doctorName) {
        if (doctorAccountId != null && doctorAccountId > 0) {
            return "id:" + doctorAccountId;
        }
        return "name:" + doctorName;
    }

    private String normalizeDoctorName(String doctorName) {
        String normalized = trim(doctorName);
        return normalized.isEmpty() ? "未指定医生" : normalized;
    }

    private boolean matchesDoctorFilter(Long doctorAccountId,
                                        String doctorName,
                                        Long targetDoctorAccountId,
                                        String targetDoctorName) {
        if (targetDoctorAccountId != null) {
            return targetDoctorAccountId.equals(normalizeDoctorAccountId(doctorAccountId));
        }
        if (StringUtils.hasText(targetDoctorName)) {
            return targetDoctorName.equalsIgnoreCase(normalizeDoctorName(doctorName));
        }
        return true;
    }

    private DoctorPerformanceStat createEmptyStat(Long doctorAccountId, String doctorName) {
        DoctorPerformanceStat stat = new DoctorPerformanceStat();
        stat.setDoctor_account_id(doctorAccountId);
        stat.setDoctor_name(doctorName);
        stat.setProject_count(0);
        stat.setTurnover_amount(0D);
        stat.setReceived_amount(0D);
        stat.setRefunded_amount(0D);
        stat.setArrears_amount(0D);
        return stat;
    }

    private void accumulateStat(DoctorPerformanceStat stat,
                                double turnoverAmount,
                                double receivedAmount,
                                double refundedAmount,
                                double arrearsAmount) {
        stat.setProject_count((stat.getProject_count() == null ? 0 : stat.getProject_count()) + 1);
        stat.setTurnover_amount(round2(safeAmount(stat.getTurnover_amount()) + turnoverAmount));
        stat.setReceived_amount(round2(safeAmount(stat.getReceived_amount()) + receivedAmount));
        stat.setRefunded_amount(round2(safeAmount(stat.getRefunded_amount()) + refundedAmount));
        stat.setArrears_amount(round2(safeAmount(stat.getArrears_amount()) + arrearsAmount));
    }

    private double parseAmount(String value) {
        if (!StringUtils.hasText(value)) {
            return 0D;
        }
        try {
            return round2(Double.parseDouble(value.trim()));
        } catch (Exception exception) {
            return 0D;
        }
    }

    private Map<Long, List<TreatmentOperationAllocation>> buildAllocationsByTreatmentId(List<TreatmentOperationAllocation> allocations) {
        Map<Long, List<TreatmentOperationAllocation>> result = new LinkedHashMap<>();
        if (allocations == null) {
            return result;
        }
        for (TreatmentOperationAllocation allocation : allocations) {
            if (allocation == null || allocation.getTreatment_id() == null || allocation.getTreatment_id() <= 0) {
                continue;
            }
            result.computeIfAbsent(allocation.getTreatment_id(), key -> new ArrayList<>()).add(allocation);
        }
        return result;
    }

    private List<Double> allocateByRatios(List<Double> ratios, double totalAmount) {
        List<Double> result = new ArrayList<>();
        if (ratios == null || ratios.isEmpty()) {
            return result;
        }
        if (totalAmount <= 0) {
            for (int index = 0; index < ratios.size(); index++) {
                result.add(0D);
            }
            return result;
        }
        double allocated = 0D;
        int lastPositiveIndex = -1;
        for (int index = 0; index < ratios.size(); index++) {
            if (safeAmount(ratios.get(index)) > 0.0000001) {
                lastPositiveIndex = index;
            }
        }
        if (lastPositiveIndex < 0) {
            for (int index = 0; index < ratios.size(); index++) {
                result.add(0D);
            }
            return result;
        }
        for (int index = 0; index < ratios.size(); index++) {
            double ratio = safeAmount(ratios.get(index));
            double amount;
            if (ratio <= 0.0000001) {
                amount = 0D;
            } else if (index == lastPositiveIndex) {
                amount = round2(totalAmount - allocated);
            } else {
                amount = round2(totalAmount * ratio);
                allocated = round2(allocated + amount);
            }
            result.add(Math.max(0D, amount));
        }
        return result;
    }

    private double safeAmount(Double value) {
        return value == null ? 0D : round2(value);
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
