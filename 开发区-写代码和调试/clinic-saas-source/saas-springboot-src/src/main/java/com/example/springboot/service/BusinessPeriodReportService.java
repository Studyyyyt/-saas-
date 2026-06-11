package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.BusinessPeriodReport;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Treatment;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.BusinessPeriodReportMapper;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import com.example.springboot.util.FinanceExpenseClassifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class BusinessPeriodReportService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FAILED = "FAILED";
    private static final long STALE_PENDING_SECONDS = 90L;

    private final BusinessPeriodReportMapper reportMapper;
    private final AppointmentMapper appointmentMapper;
    private final FinanceMapper financeMapper;
    private final TreatmentMapper treatmentMapper;
    private final PatientMapper patientMapper;
    private final ObjectMapper objectMapper;

    public BusinessPeriodReportService(BusinessPeriodReportMapper reportMapper,
                                       AppointmentMapper appointmentMapper,
                                       FinanceMapper financeMapper,
                                       TreatmentMapper treatmentMapper,
                                       PatientMapper patientMapper,
                                       ObjectMapper objectMapper) {
        this.reportMapper = reportMapper;
        this.appointmentMapper = appointmentMapper;
        this.financeMapper = financeMapper;
        this.treatmentMapper = treatmentMapper;
        this.patientMapper = patientMapper;
        this.objectMapper = objectMapper;
    }

    public PeriodTarget resolveWeeklyTarget(LocalDate anchorDate) {
        LocalDate anchor = anchorDate == null ? LocalDate.now(DEFAULT_ZONE).minusWeeks(1) : anchorDate;
        LocalDate periodStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate periodEnd = anchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        int week = periodStart.get(WeekFields.ISO.weekOfWeekBasedYear());
        String periodKey = periodStart.getYear() + "-W" + String.format(Locale.US, "%02d", week);
        return new PeriodTarget("WEEKLY", "周报", anchor, periodKey, periodStart, periodEnd);
    }

    public PeriodTarget resolveMonthlyTarget(LocalDate anchorDate) {
        LocalDate anchor = anchorDate == null ? LocalDate.now(DEFAULT_ZONE).minusMonths(1) : anchorDate;
        LocalDate periodStart = anchor.withDayOfMonth(1);
        LocalDate periodEnd = anchor.withDayOfMonth(anchor.lengthOfMonth());
        String periodKey = String.format(Locale.US, "%04d-%02d", periodStart.getYear(), periodStart.getMonthValue());
        return new PeriodTarget("MONTHLY", "月报", anchor, periodKey, periodStart, periodEnd);
    }

    public Map<String, Object> getReportByTypeAndPeriodKey(String reportType, String periodKey) {
        if (!StringUtils.hasText(reportType) || !StringUtils.hasText(periodKey)) {
            return null;
        }
        return buildView(normalizeStalePending(reportMapper.selectByTypeAndPeriodKey(reportType.trim().toUpperCase(Locale.ROOT), periodKey.trim())));
    }

    public Map<String, Object> prepareWeeklyReportTask(LocalDate anchorDate, String triggerType) {
        return preparePeriodReportTask(resolveWeeklyTarget(anchorDate), triggerType);
    }

    public Map<String, Object> prepareMonthlyReportTask(LocalDate anchorDate, String triggerType) {
        return preparePeriodReportTask(resolveMonthlyTarget(anchorDate), triggerType);
    }

    public Map<String, Object> markReportTaskFailed(PeriodTarget target, String triggerType, String errorMessage) {
        BusinessPeriodReport entity = reportMapper.selectByTypeAndPeriodKey(target.reportType, target.periodKey);
        if (entity == null) {
            entity = new BusinessPeriodReport();
            entity.setReport_type(target.reportType);
            entity.setPeriod_key(target.periodKey);
        }
        entity.setPeriod_start(java.util.Date.from(target.periodStart.atStartOfDay(DEFAULT_ZONE).toInstant()));
        entity.setPeriod_end(java.util.Date.from(target.periodEnd.atStartOfDay(DEFAULT_ZONE).toInstant()));
        entity.setReport_status(STATUS_FAILED);
        entity.setSource_type("EXTERNAL");
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name("n8n");
        entity.setOperating_score(null);
        entity.setTrend("");
        entity.setHeadline(target.reportTypeLabel + "生成失败");
        entity.setSummary("后台任务执行失败，请稍后重试。");
        entity.setMetrics_json("");
        entity.setAnalysis_json("");
        entity.setRaw_response("");
        entity.setError_message(trimToLimit(errorMessage, 1000));
        save(entity);
        return buildView(entity);
    }

    public Map<String, Object> runWeeklyReport(LocalDate anchorDate, String triggerType) {
        PeriodTarget target = resolveWeeklyTarget(anchorDate);
        return runPeriodReport(target.reportType, target.reportTypeLabel, target.periodKey, target.periodStart, target.periodEnd, triggerType);
    }

    public Map<String, Object> runMonthlyReport(LocalDate anchorDate, String triggerType) {
        PeriodTarget target = resolveMonthlyTarget(anchorDate);
        return runPeriodReport(target.reportType, target.reportTypeLabel, target.periodKey, target.periodStart, target.periodEnd, triggerType);
    }

    public Map<String, Object> getLatestWeeklyReport() {
        Map<String, Object> full = buildView(normalizeStalePending(reportMapper.selectLatestByType("WEEKLY")));
        return simplifyReportView(full);
    }

    public Map<String, Object> getLatestMonthlyReport() {
        Map<String, Object> full = buildView(normalizeStalePending(reportMapper.selectLatestByType("MONTHLY")));
        return simplifyReportView(full);
    }

    public List<Map<String, Object>> getRecentByType(String reportType, Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 12 : Math.min(limit, 36);
        return reportMapper.selectRecentByType(reportType, safeLimit).stream()
                .map(this::normalizeStalePending)
                .map(this::buildView)
                .collect(Collectors.toList());
    }

    private Map<String, Object> preparePeriodReportTask(PeriodTarget target, String triggerType) {
        BusinessPeriodReport entity = reportMapper.selectByTypeAndPeriodKey(target.reportType, target.periodKey);
        if (entity == null) {
            entity = new BusinessPeriodReport();
            entity.setReport_type(target.reportType);
            entity.setPeriod_key(target.periodKey);
        }
        entity.setPeriod_start(java.util.Date.from(target.periodStart.atStartOfDay(DEFAULT_ZONE).toInstant()));
        entity.setPeriod_end(java.util.Date.from(target.periodEnd.atStartOfDay(DEFAULT_ZONE).toInstant()));
        entity.setReport_status(STATUS_PENDING);
        entity.setSource_type("EXTERNAL");
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name("n8n");
        entity.setOperating_score(null);
        entity.setTrend("");
        entity.setHeadline(target.reportTypeLabel + "生成中");
        entity.setSummary("任务已提交，系统正在后台生成" + target.reportTypeLabel + "，请稍后查看。");
        entity.setMetrics_json("");
        entity.setAnalysis_json("");
        entity.setRaw_response("");
        entity.setError_message("");
        save(entity);
        return buildView(entity);
    }

    private Map<String, Object> runPeriodReport(String reportType,
                                                String reportTypeLabel,
                                                String periodKey,
                                                LocalDate periodStart,
                                                LocalDate periodEnd,
                                                String triggerType) {
        PeriodMetrics metrics = buildPeriodMetrics(reportType, reportTypeLabel, periodStart, periodEnd);
        BusinessPeriodReport entity = reportMapper.selectByTypeAndPeriodKey(reportType, periodKey);
        if (entity == null) {
            entity = new BusinessPeriodReport();
            entity.setReport_type(reportType);
            entity.setPeriod_key(periodKey);
        }
        entity.setPeriod_start(java.util.Date.from(periodStart.atStartOfDay(DEFAULT_ZONE).toInstant()));
        entity.setPeriod_end(java.util.Date.from(periodEnd.atStartOfDay(DEFAULT_ZONE).toInstant()));
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name("n8n");
        entity.setMetrics_json(writeJson(metrics));

        entity.setReport_status("SUCCESS");
        entity.setSource_type("EXTERNAL");
        entity.setOperating_score(null);
        entity.setTrend("");
        entity.setHeadline("");
        entity.setSummary("数据已就绪，请通过 n8n 外部工作流进行 AI 分析");
        entity.setAnalysis_json("");
        entity.setRaw_response("");
        entity.setError_message("");
        save(entity);

        return buildView(entity);
    }

    private void save(BusinessPeriodReport entity) {
        if (entity.getId() == null) {
            reportMapper.insert(entity);
        } else {
            reportMapper.update(entity);
        }
    }

    private PeriodMetrics buildPeriodMetrics(String reportTypeLabel,
                                             String reportDisplayLabel,
                                             LocalDate periodStart,
                                             LocalDate periodEnd) {
        List<Appointment> appointments = safeList(appointmentMapper.selectAll()).stream()
                .filter(item -> isBetween(item.getAppointment_date(), periodStart, periodEnd))
                .collect(Collectors.toList());
        List<Treatment> treatments = safeList(treatmentMapper.selectAll()).stream()
                .filter(item -> isBetween(item.getTreatment_date(), periodStart, periodEnd))
                .collect(Collectors.toList());
        List<Finance> monthFinances = new ArrayList<>();
        LocalDate cursor = periodStart.withDayOfMonth(1);
        LocalDate endMonth = periodEnd.withDayOfMonth(1);
        while (!cursor.isAfter(endMonth)) {
            monthFinances.addAll(safeList(financeMapper.getFinancesByMonth(cursor.getYear(), cursor.getMonthValue())));
            cursor = cursor.plusMonths(1);
        }
        List<Finance> finances = monthFinances.stream()
                .filter(item -> isStringDateBetween(item.getDate(), periodStart, periodEnd))
                .collect(Collectors.toList());

        PeriodMetrics metrics = new PeriodMetrics();
        metrics.report_type = reportTypeLabel;
        metrics.report_type_label = reportDisplayLabel;
        metrics.period_start = periodStart.toString();
        metrics.period_end = periodEnd.toString();
        metrics.period_label = periodStart + " 至 " + periodEnd;
        metrics.model_name = "";
        metrics.total_patients = safeList(patientMapper.selectAll()).size();
        metrics.total_appointments = appointments.size();
        metrics.total_treatments = treatments.size();
        metrics.total_income = round2(finances.stream().filter(item -> isIncomeType(item.getType())).mapToDouble(Finance::getAmount).sum());
        metrics.total_expense = round2(finances.stream().filter(item -> !isIncomeType(item.getType())).mapToDouble(Finance::getAmount).sum());
        metrics.net_income = round2(metrics.total_income - metrics.total_expense);
        ExpenseBreakdown currentOperatingExpense = buildOperatingExpenseBreakdown(finances);
        metrics.total_operating_expense = currentOperatingExpense.total();
        metrics.material_expense = currentOperatingExpense.material();
        metrics.lab_expense = currentOperatingExpense.lab();
        metrics.other_expense = currentOperatingExpense.other();
        metrics.total_unique_patients = countUniquePatients(appointments, treatments);
        metrics.completed_treatments = (int) treatments.stream().filter(item -> "完成".equals(trim(item.getStatus()))).count();
        metrics.cancellation_rate = appointments.isEmpty() ? 0D : round2(appointments.stream().filter(item -> "已取消".equals(trim(item.getStatus()))).count() * 100D / appointments.size());
        metrics.avg_daily_income = round2(metrics.total_income / Math.max(1, periodEnd.toEpochDay() - periodStart.toEpochDay() + 1));
        metrics.avg_daily_appointments = round2(metrics.total_appointments * 1D / Math.max(1, periodEnd.toEpochDay() - periodStart.toEpochDay() + 1));
        metrics.top_doctors = buildTopDoctors(appointments, treatments);
        metrics.top_projects = buildTopProjects(treatments);

        LocalDate compareStart;
        LocalDate compareEnd;
        if ("WEEKLY".equals(reportTypeLabel)) {
            compareStart = periodStart.minusWeeks(1);
            compareEnd = periodEnd.minusWeeks(1);
        } else {
            compareStart = periodStart.minusMonths(1);
            compareEnd = compareStart.withDayOfMonth(compareStart.lengthOfMonth());
        }
        PeriodMetrics previousMetrics = buildComparisonMetrics(compareStart, compareEnd);
        metrics.previous_net_income = previousMetrics.net_income;
        metrics.previous_appointments = previousMetrics.total_appointments;
        metrics.net_income_change_rate = calculateRate(metrics.net_income, previousMetrics.net_income);
        metrics.appointment_change_rate = calculateRate(metrics.total_appointments, previousMetrics.total_appointments);
        metrics.model_name = "n8n";
        metrics.data_limitations = new ArrayList<>();
        return metrics;
    }

    private PeriodMetrics buildComparisonMetrics(LocalDate periodStart, LocalDate periodEnd) {
        List<Appointment> appointments = safeList(appointmentMapper.selectAll()).stream()
                .filter(item -> isBetween(item.getAppointment_date(), periodStart, periodEnd))
                .collect(Collectors.toList());
        List<Finance> monthFinances = new ArrayList<>();
        LocalDate cursor = periodStart.withDayOfMonth(1);
        LocalDate endMonth = periodEnd.withDayOfMonth(1);
        while (!cursor.isAfter(endMonth)) {
            monthFinances.addAll(safeList(financeMapper.getFinancesByMonth(cursor.getYear(), cursor.getMonthValue())));
            cursor = cursor.plusMonths(1);
        }
        List<Finance> finances = monthFinances.stream()
                .filter(item -> isStringDateBetween(item.getDate(), periodStart, periodEnd))
                .collect(Collectors.toList());
        PeriodMetrics metrics = new PeriodMetrics();
        metrics.total_appointments = appointments.size();
        metrics.total_income = round2(finances.stream().filter(item -> isIncomeType(item.getType())).mapToDouble(Finance::getAmount).sum());
        metrics.total_expense = round2(finances.stream().filter(item -> !isIncomeType(item.getType())).mapToDouble(Finance::getAmount).sum());
        metrics.net_income = round2(metrics.total_income - metrics.total_expense);
        ExpenseBreakdown currentOperatingExpense = buildOperatingExpenseBreakdown(finances);
        metrics.total_operating_expense = currentOperatingExpense.total();
        metrics.material_expense = currentOperatingExpense.material();
        metrics.lab_expense = currentOperatingExpense.lab();
        metrics.other_expense = currentOperatingExpense.other();
        return metrics;
    }

    private List<BusinessDailyAnalysisService.DoctorMetric> buildTopDoctors(List<Appointment> appointments, List<Treatment> treatments) {
        Map<String, BusinessDailyAnalysisService.DoctorMetric> byDoctor = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            String doctorName = trim(appointment.getDoctor_name());
            if (!StringUtils.hasText(doctorName)) {
                doctorName = "未指定医生";
            }
            BusinessDailyAnalysisService.DoctorMetric metric = byDoctor.computeIfAbsent(doctorName, key -> new BusinessDailyAnalysisService.DoctorMetric());
            metric.doctor_name = doctorName;
            metric.appointment_count += 1;
        }
        for (Treatment treatment : treatments) {
            String doctorName = trim(treatment.getDoctor_name());
            if (!StringUtils.hasText(doctorName)) {
                doctorName = "未指定医生";
            }
            BusinessDailyAnalysisService.DoctorMetric metric = byDoctor.computeIfAbsent(doctorName, key -> new BusinessDailyAnalysisService.DoctorMetric());
            metric.doctor_name = doctorName;
            metric.treatment_count += 1;
            metric.treatment_revenue = round2(metric.treatment_revenue + defaultDouble(parseAmount(treatment.getTreatment_fee())));
        }
        return byDoctor.values().stream()
                .sorted(Comparator.comparingDouble((BusinessDailyAnalysisService.DoctorMetric item) -> item.treatment_revenue).reversed()
                        .thenComparing(Comparator.comparingInt((BusinessDailyAnalysisService.DoctorMetric item) -> item.appointment_count).reversed())
                        .thenComparing(item -> item.doctor_name, Comparator.nullsLast(String::compareTo)))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<BusinessDailyAnalysisService.ProjectMetric> buildTopProjects(List<Treatment> treatments) {
        Map<String, BusinessDailyAnalysisService.ProjectMetric> byProject = new LinkedHashMap<>();
        for (Treatment treatment : treatments) {
            String projectName = normalizeProjectName(treatment);
            BusinessDailyAnalysisService.ProjectMetric metric = byProject.computeIfAbsent(projectName, key -> new BusinessDailyAnalysisService.ProjectMetric());
            metric.project_name = projectName;
            metric.case_count += 1;
            metric.revenue = round2(metric.revenue + defaultDouble(parseAmount(treatment.getTreatment_fee())));
        }
        return byProject.values().stream()
                .sorted(Comparator.comparingDouble((BusinessDailyAnalysisService.ProjectMetric item) -> item.revenue).reversed()
                        .thenComparing(Comparator.comparingInt((BusinessDailyAnalysisService.ProjectMetric item) -> item.case_count).reversed())
                        .thenComparing(item -> item.project_name, Comparator.nullsLast(String::compareTo)))
                .limit(5)
                .collect(Collectors.toList());
    }

    private int countUniquePatients(List<Appointment> appointments, List<Treatment> treatments) {
        Set<Long> ids = new HashSet<>();
        appointments.stream().map(Appointment::getPatient_id).filter(Objects::nonNull).forEach(ids::add);
        treatments.stream().map(Treatment::getPatient_id).filter(Objects::nonNull).forEach(ids::add);
        return ids.size();
    }

    private ExpenseBreakdown buildOperatingExpenseBreakdown(List<Finance> finances) {
        double material = 0D;
        double lab = 0D;
        double other = 0D;
        for (Finance finance : safeList(finances)) {
            FinanceExpenseClassifier.ExpenseScope scope = FinanceExpenseClassifier.resolveOperatingExpenseScope(finance);
            if (scope == FinanceExpenseClassifier.ExpenseScope.MATERIAL) {
                material += finance.getAmount();
            } else if (scope == FinanceExpenseClassifier.ExpenseScope.LAB) {
                lab += finance.getAmount();
            } else if (scope == FinanceExpenseClassifier.ExpenseScope.OTHER) {
                other += finance.getAmount();
            }
        }
        return new ExpenseBreakdown(round2(material), round2(lab), round2(other));
    }

    /**
     * 精简周期报告视图，过滤掉元数据字段，仅保留业务字段
     */
    private Map<String, Object> simplifyReportView(Map<String, Object> view) {
        if (view == null) {
            return null;
        }
        Map<String, Object> simplified = new LinkedHashMap<>();
        copyIfPresent(view, simplified, "report_type");
        copyIfPresent(view, simplified, "period_key");
        copyIfPresent(view, simplified, "period_start");
        copyIfPresent(view, simplified, "period_end");
        copyIfPresent(view, simplified, "period_label");
        copyIfPresent(view, simplified, "operating_score");
        copyIfPresent(view, simplified, "trend");
        copyIfPresent(view, simplified, "headline");
        copyIfPresent(view, simplified, "summary");
        copyIfPresent(view, simplified, "metrics");
        copyIfPresent(view, simplified, "analysis");
        return simplified;
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    private Map<String, Object> buildView(BusinessPeriodReport report) {
        if (report == null) {
            return null;
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", report.getId());
        view.put("report_type", report.getReport_type());
        view.put("period_key", report.getPeriod_key());
        view.put("period_start", formatDate(report.getPeriod_start()));
        view.put("period_end", formatDate(report.getPeriod_end()));
        view.put("period_label", formatDate(report.getPeriod_start()) + " 至 " + formatDate(report.getPeriod_end()));
        view.put("report_status", report.getReport_status());
        view.put("source_type", report.getSource_type());
        view.put("trigger_type", report.getTrigger_type());
        view.put("model_name", report.getModel_name());
        view.put("operating_score", report.getOperating_score());
        view.put("trend", report.getTrend());
        view.put("headline", report.getHeadline());
        view.put("summary", report.getSummary());
        view.put("metrics", parseJson(report.getMetrics_json()));
        view.put("analysis", parseJson(report.getAnalysis_json()));
        view.put("error_message", report.getError_message());
        view.put("updated_at", formatDateTime(report.getUpdated_at()));
        return view;
    }

    private BusinessPeriodReport normalizeStalePending(BusinessPeriodReport report) {
        if (report == null) {
            return null;
        }
        if (!STATUS_PENDING.equals(trim(report.getReport_status()))) {
            return report;
        }
        java.util.Date updatedAt = report.getUpdated_at();
        if (updatedAt == null || updatedAt.toInstant().plusSeconds(STALE_PENDING_SECONDS).isAfter(Instant.now())) {
            return report;
        }
        PeriodTarget target = new PeriodTarget(
                trim(report.getReport_type()),
                "WEEKLY".equals(trim(report.getReport_type())) ? "周报" : "月报",
                report.getPeriod_start() == null ? LocalDate.now(DEFAULT_ZONE) : Instant.ofEpochMilli(report.getPeriod_start().getTime()).atZone(DEFAULT_ZONE).toLocalDate(),
                trim(report.getPeriod_key()),
                report.getPeriod_start() == null ? LocalDate.now(DEFAULT_ZONE) : Instant.ofEpochMilli(report.getPeriod_start().getTime()).atZone(DEFAULT_ZONE).toLocalDate(),
                report.getPeriod_end() == null ? LocalDate.now(DEFAULT_ZONE) : Instant.ofEpochMilli(report.getPeriod_end().getTime()).atZone(DEFAULT_ZONE).toLocalDate()
        );
        markReportTaskFailed(target, report.getTrigger_type(), "后台任务超时未完成，系统已自动标记失败，请重新提交。");
        return reportMapper.selectByTypeAndPeriodKey(target.reportType, target.periodKey);
    }

    private Object parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception exception) {
            return json;
        }
    }

    private String writeJson(Object value) {
        if (value == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return "";
        }
    }

    private <T> List<T> safeList(Collection<T> list) {
        return list == null ? List.of() : new ArrayList<>(list);
    }

    private boolean isBetween(java.util.Date value, LocalDate start, LocalDate end) {
        if (value == null) {
            return false;
        }
        LocalDate date = Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private boolean isStringDateBetween(String value, LocalDate start, LocalDate end) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return !date.isBefore(start) && !date.isAfter(end);
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isIncomeType(String type) {
        String value = trim(type);
        return value.contains("收入") || value.contains("收费");
    }

    private Double parseAmount(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception exception) {
            return null;
        }
    }

    private double defaultDouble(Double value) {
        return value == null ? 0D : value;
    }

    private double calculateRate(double currentValue, double previousValue) {
        if (Math.abs(previousValue) < 0.0001D) {
            return currentValue > 0 ? 100D : 0D;
        }
        return round2((currentValue - previousValue) * 100D / Math.abs(previousValue));
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToLimit(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String text = value.trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String normalizeTriggerType(String triggerType) {
        return StringUtils.hasText(triggerType) ? triggerType.trim().toUpperCase(Locale.ROOT) : "MANUAL";
    }

    private String formatDate(java.util.Date value) {
        if (value == null) return "";
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDate().toString();
    }

    private String formatDateTime(java.util.Date value) {
        if (value == null) return "";
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDateTime().toString().replace('T', ' ');
    }

    private String normalizeProjectName(Treatment treatment) {
        String appointmentPurpose = extractProjectNameCandidate(treatment == null ? null : treatment.getAppointment_purpose());
        if (isMeaningfulProjectName(appointmentPurpose)) {
            return trimToLimit(appointmentPurpose, 40);
        }
        String treatmentContent = extractProjectNameCandidate(treatment == null ? null : treatment.getTreatment_content());
        if (isMeaningfulProjectName(treatmentContent)) {
            return trimToLimit(treatmentContent, 40);
        }
        return "未规范项目";
    }

    private String extractProjectNameCandidate(String rawValue) {
        String text = trim(rawValue);
        if (!StringUtils.hasText(text)) {
            return "";
        }
        if ((text.startsWith("{") && text.endsWith("}")) || (text.startsWith("[") && text.endsWith("]"))) {
            try {
                JsonNode root = objectMapper.readTree(text);
                LinkedHashSet<String> names = new LinkedHashSet<>();
                collectProjectNames(root, names);
                return names.stream().filter(StringUtils::hasText).collect(Collectors.joining(" / "));
            } catch (Exception ignored) {
                return text;
            }
        }
        return text;
    }

    private void collectProjectNames(JsonNode node, Set<String> names) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual()) {
            String text = trim(node.asText());
            if (StringUtils.hasText(text)) {
                names.add(text);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                collectProjectNames(item, names);
            }
            return;
        }
        if (node.isObject()) {
            String[] preferredKeys = {"appointment_purpose", "project_name", "item_name", "name", "治疗方案", "treatment_content", "treatment"};
            for (String key : preferredKeys) {
                JsonNode child = node.get(key);
                if (child != null && !child.isNull()) {
                    collectProjectNames(child, names);
                }
            }
        }
    }

    private boolean isMeaningfulProjectName(String value) {
        String text = trim(value);
        if (!StringUtils.hasText(text)) {
            return false;
        }
        if (text.matches("^\\d+$")) {
            return false;
        }
        if (text.matches("^历史异常值-\\d+$")) {
            return false;
        }
        return !text.contains("删除级联")
                && !text.contains("异常预约目的")
                && !text.contains("待人工核对")
                && !text.contains("测试");
    }

    public static class PeriodMetrics {
        public String report_type;
        public String report_type_label;
        public String period_start;
        public String period_end;
        public String period_label;
        public String model_name;
        public int total_patients;
        public int total_appointments;
        public int total_treatments;
        public int total_unique_patients;
        public int completed_treatments;
        public double total_income;
        public double total_expense;
        public double total_operating_expense;
        public double material_expense;
        public double lab_expense;
        public double other_expense;
        public double net_income;
        public double avg_daily_income;
        public double avg_daily_appointments;
        public double cancellation_rate;
        public double previous_net_income;
        public int previous_appointments;
        public double net_income_change_rate;
        public double appointment_change_rate;
        public List<BusinessDailyAnalysisService.DoctorMetric> top_doctors;
        public List<BusinessDailyAnalysisService.ProjectMetric> top_projects;
        public List<String> data_limitations;
    }

    private record ExpenseBreakdown(double material, double lab, double other) {
        double total() {
            return Math.round((material + lab + other) * 100D) / 100D;
        }
    }

    public static class PeriodTarget {
        public final String reportType;
        public final String reportTypeLabel;
        public final LocalDate anchorDate;
        public final String periodKey;
        public final LocalDate periodStart;
        public final LocalDate periodEnd;

        public PeriodTarget(String reportType,
                            String reportTypeLabel,
                            LocalDate anchorDate,
                            String periodKey,
                            LocalDate periodStart,
                            LocalDate periodEnd) {
            this.reportType = reportType;
            this.reportTypeLabel = reportTypeLabel;
            this.anchorDate = anchorDate;
            this.periodKey = periodKey;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }
    }
}
