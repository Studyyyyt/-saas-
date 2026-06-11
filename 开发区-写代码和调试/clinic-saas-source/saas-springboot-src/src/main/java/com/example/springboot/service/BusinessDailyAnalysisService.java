package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.BusinessDailyAnalysis;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Treatment;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.BusinessDailyAnalysisMapper;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.MedicalRecordMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import com.example.springboot.util.FinanceExpenseClassifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BusinessDailyAnalysisService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FALLBACK = "FALLBACK";
    private static final String STATUS_FAILED = "FAILED";
    private static final String TRIGGER_SCHEDULED = "SCHEDULED";
    private static final String TRIGGER_MANUAL = "MANUAL";
    private static final long STALE_PENDING_SECONDS = 90L;

    private final BusinessDailyAnalysisMapper analysisMapper;
    private final AppointmentMapper appointmentMapper;
    private final FinanceMapper financeMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final TreatmentMapper treatmentMapper;
    private final PatientMapper patientMapper;
    private final ObjectMapper objectMapper;

    public BusinessDailyAnalysisService(BusinessDailyAnalysisMapper analysisMapper,
                                        AppointmentMapper appointmentMapper,
                                        FinanceMapper financeMapper,
                                        MedicalRecordMapper medicalRecordMapper,
                                        TreatmentMapper treatmentMapper,
                                        PatientMapper patientMapper,
                                        ObjectMapper objectMapper) {
        this.analysisMapper = analysisMapper;
        this.appointmentMapper = appointmentMapper;
        this.financeMapper = financeMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.treatmentMapper = treatmentMapper;
        this.patientMapper = patientMapper;
        this.objectMapper = objectMapper;
    }

    public LocalDate resolveAnalysisDate(LocalDate analysisDate) {
        return analysisDate == null ? LocalDate.now(DEFAULT_ZONE).minusDays(1) : analysisDate;
    }

    public Map<String, Object> getAnalysisByDate(LocalDate analysisDate) {
        if (analysisDate == null) {
            return null;
        }
        return buildView(normalizeStalePending(analysisMapper.selectByAnalysisDate(Date.valueOf(analysisDate))));
    }

    public Map<String, Object> prepareDailyAnalysisTask(LocalDate analysisDate, String triggerType) {
        LocalDate targetDate = resolveAnalysisDate(analysisDate);
        BusinessDailyAnalysis entity = analysisMapper.selectByAnalysisDate(Date.valueOf(targetDate));
        if (entity == null) {
            entity = new BusinessDailyAnalysis();
            entity.setAnalysis_date(java.util.Date.from(targetDate.atStartOfDay(DEFAULT_ZONE).toInstant()));
        }
        entity.setAnalysis_status(STATUS_PENDING);
        entity.setSource_type("EXTERNAL");
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name("n8n");
        entity.setOperating_score(null);
        entity.setTrend("");
        entity.setHeadline("日报生成中");
        entity.setSummary("任务已提交，系统正在后台生成日报，请稍后查看。");
        entity.setMetrics_json("");
        entity.setAnalysis_json("");
        entity.setRaw_response("");
        entity.setError_message("");
        save(entity);
        return buildView(entity);
    }

    public Map<String, Object> markDailyAnalysisFailed(LocalDate analysisDate, String triggerType, String errorMessage) {
        LocalDate targetDate = resolveAnalysisDate(analysisDate);
        BusinessDailyAnalysis entity = analysisMapper.selectByAnalysisDate(Date.valueOf(targetDate));
        if (entity == null) {
            entity = new BusinessDailyAnalysis();
            entity.setAnalysis_date(java.util.Date.from(targetDate.atStartOfDay(DEFAULT_ZONE).toInstant()));
        }
        entity.setAnalysis_status(STATUS_FAILED);
        entity.setSource_type("EXTERNAL");
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name("n8n");
        entity.setOperating_score(null);
        entity.setTrend("");
        entity.setHeadline("日报生成失败");
        entity.setSummary("后台任务执行失败，请稍后重试。");
        entity.setMetrics_json("");
        entity.setAnalysis_json("");
        entity.setRaw_response("");
        entity.setError_message(trimToLimit(errorMessage, 1000));
        save(entity);
        return buildView(entity);
    }

    public Map<String, Object> runDailyAnalysis(LocalDate analysisDate, String triggerType) {
        LocalDate targetDate = resolveAnalysisDate(analysisDate);
        String finalTriggerType = normalizeTriggerType(triggerType);

        DailyBusinessMetrics metrics = buildDailyMetricsForDate(targetDate);
        BusinessDailyAnalysis entity = analysisMapper.selectByAnalysisDate(Date.valueOf(targetDate));
        if (entity == null) {
            entity = new BusinessDailyAnalysis();
            entity.setAnalysis_date(java.util.Date.from(targetDate.atStartOfDay(DEFAULT_ZONE).toInstant()));
        }
        entity.setTrigger_type(finalTriggerType);
        entity.setModel_name("n8n");
        entity.setMetrics_json(writeJson(metrics));

        entity.setAnalysis_status(STATUS_SUCCESS);
        entity.setSource_type("EXTERNAL");
        entity.setHeadline("");
        entity.setSummary("数据已就绪，请通过 n8n 外部工作流进行 AI 分析");
        entity.setOperating_score(null);
        entity.setTrend("");
        entity.setAnalysis_json("");
        entity.setRaw_response("");
        entity.setError_message("");

        save(entity);
        return buildView(entity);
    }

    public Map<String, Object> getLatestAnalysis() {
        Map<String, Object> full = buildView(normalizeStalePending(analysisMapper.selectLatest()));
        return simplifyAnalysisView(full);
    }

    public Map<String, Object> getAnalysisById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return simplifyAnalysisView(buildView(normalizeStalePending(analysisMapper.selectById(id))));
    }

    public List<Map<String, Object>> getRecentAnalyses(Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 90);
        return analysisMapper.selectRecent(safeLimit).stream()
                .map(this::normalizeStalePending)
                .map(this::buildView)
                .map(this::simplifyAnalysisView)
                .collect(Collectors.toList());
    }

    public Map<String, Object> testModelConnection() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("connected", false);
        result.put("message", "内置 AI 分析已停用，所有分析请通过 n8n 外部工作流完成");
        result.put("checked_at", formatDateTime(java.util.Date.from(Instant.now())));
        return result;
    }

    public DailyBusinessMetrics buildDailyMetricsForDate(LocalDate targetDate) {
        return buildMetrics(targetDate);
    }

    /**
     * 构建精简版每日经营指标，仅保留核心业务字段，供 n8n 等外部系统调用
     */
    public Map<String, Object> buildSimplifiedDailyMetrics(LocalDate targetDate) {
        DailyBusinessMetrics m = buildMetrics(targetDate);
        Map<String, Object> result = new LinkedHashMap<>();
        // 日期
        result.put("date", m.analysis_date);
        result.put("day_of_week", resolveDayOfWeek(m.analysis_date));
        // 预约
        result.put("appointment_count", m.today_appointments);
        result.put("appointment_unique_patient_count", m.today_unique_patients);
        result.put("appointment_completed_count", m.completed_treatment_count);
        result.put("appointment_cancelled_count",
                m.appointment_status_breakdown == null ? 0 : m.appointment_status_breakdown.getOrDefault("已取消", 0));
        // 财务
        result.put("total_income", m.today_income);
        result.put("total_expense", m.today_expense);
        result.put("net_profit", m.today_net_income);
        result.put("new_patient_income", 0D); // 当前模型未拆分新老患者收入，占位
        result.put("old_patient_income", 0D);
        result.put("arrears_amount", m.today_treatment_unreceived_amount);
        // 患者
        result.put("visit_patient_count", m.today_unique_patients);
        result.put("returning_visit_count", m.today_appointments - m.today_unique_patients);
        result.put("total_visit_count", m.today_medical_records);
        result.put("registration_count", m.today_appointments);
        // 医生/项目
        result.put("top_doctors", m.top_doctors);
        result.put("top_projects", m.top_projects);
        // 其他
        result.put("consultation_count", 0); // 当前模型未包含咨询数，占位
        result.put("deal_count", 0);         // 当前模型未包含成交数，占位
        return result;
    }

    private String resolveDayOfWeek(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return "";
        }
        try {
            return LocalDate.parse(dateStr).getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.CHINA);
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isOpenAiReady() {
        return false;
    }

    /**
     * 内置 AI 分析已停用，请通过 n8n 外部工作流完成分析
     */
    public BusinessAnalysisOutput requestAiAnalysis(String instructions, String prompt) {
        throw new UnsupportedOperationException("内置 AI 分析已停用，请使用 n8n 外部工作流");
    }

    private void save(BusinessDailyAnalysis entity) {
        if (entity.getId() == null) {
            analysisMapper.insert(entity);
        } else {
            analysisMapper.update(entity);
        }
    }

    private DailyBusinessMetrics buildMetrics(LocalDate targetDate) {
        DailyBusinessMetrics metrics = new DailyBusinessMetrics();
        metrics.analysis_date = targetDate.toString();

        List<Patient> allPatients = safeList(patientMapper.selectAll());
        List<Appointment> allAppointments = safeList(appointmentMapper.selectAll());
        List<MedicalRecord> allRecords = safeList(medicalRecordMapper.selectAll());
        List<Treatment> allTreatments = safeList(treatmentMapper.selectAll());
        List<Finance> allFinances = safeList(financeMapper.getAllFinances());
        List<Finance> dayFinances = safeList(financeMapper.getFinanceBydate(targetDate.toString()));
        List<Finance> currentMonthFinances = safeList(financeMapper.getFinancesByMonth(targetDate.getYear(), targetDate.getMonthValue()));
        LocalDate previousMonthDate = targetDate.minusMonths(1);
        List<Finance> previousMonthFinances = safeList(financeMapper.getFinancesByMonth(previousMonthDate.getYear(), previousMonthDate.getMonthValue()));
        Map<Long, List<Finance>> financeByTreatmentId = buildFinanceByTreatmentId(allFinances);

        List<Appointment> todayAppointments = allAppointments.stream()
                .filter(item -> isSameDate(item.getAppointment_date(), targetDate))
                .sorted(Comparator.comparing(Appointment::getAppointment_time, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        List<MedicalRecord> todayRecords = allRecords.stream()
                .filter(item -> isSameDate(item.getVisit_date(), targetDate))
                .collect(Collectors.toList());
        List<Treatment> todayTreatments = allTreatments.stream()
                .filter(item -> isSameDate(item.getTreatment_date(), targetDate))
                .collect(Collectors.toList());
        List<Appointment> future7DayAppointments = allAppointments.stream()
                .filter(item -> isWithinNextDays(item.getAppointment_date(), targetDate, 7))
                .filter(item -> !"已取消".equals(trim(item.getStatus())))
                .sorted(Comparator.comparing(Appointment::getAppointment_date, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Appointment::getAppointment_time, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        metrics.total_patients = allPatients.size();
        metrics.today_appointments = todayAppointments.size();
        metrics.today_medical_records = todayRecords.size();
        metrics.today_treatments = todayTreatments.size();
        metrics.today_unique_patients = countUniquePatients(todayAppointments, todayRecords, todayTreatments);
        metrics.future_7_day_appointments = future7DayAppointments.size();

        metrics.appointment_status_breakdown = new LinkedHashMap<>();
        metrics.appointment_status_breakdown.put("待治疗", 0);
        metrics.appointment_status_breakdown.put("已预约", 0);
        metrics.appointment_status_breakdown.put("待就诊", 0);
        metrics.appointment_status_breakdown.put("已取消", 0);
        metrics.appointment_status_breakdown.put("其他", 0);
        for (Appointment appointment : todayAppointments) {
            String status = trim(appointment.getStatus());
            if (!metrics.appointment_status_breakdown.containsKey(status)) {
                status = "其他";
            }
            metrics.appointment_status_breakdown.put(status, metrics.appointment_status_breakdown.get(status) + 1);
        }
        int cancelledCount = metrics.appointment_status_breakdown.getOrDefault("已取消", 0);
        metrics.cancellation_rate = metrics.today_appointments == 0 ? 0D : round2(cancelledCount * 100D / metrics.today_appointments);

        double income = 0D;
        double expense = 0D;
        for (Finance finance : dayFinances) {
            if (isIncomeType(finance.getType())) {
                income += finance.getAmount();
            } else {
                expense += finance.getAmount();
            }
        }
        metrics.today_income = round2(income);
        metrics.today_expense = round2(expense);
        metrics.today_net_income = round2(income - expense);
        ExpenseBreakdown todayOperatingExpense = buildOperatingExpenseBreakdown(dayFinances);
        metrics.today_operating_expense = todayOperatingExpense.total();
        metrics.today_material_expense = todayOperatingExpense.material();
        metrics.today_lab_expense = todayOperatingExpense.lab();
        metrics.today_other_expense = todayOperatingExpense.other();

        double monthIncome = sumByType(currentMonthFinances, true);
        double monthExpense = sumByType(currentMonthFinances, false);
        double previousMonthIncome = sumByType(previousMonthFinances, true);
        double previousMonthExpense = sumByType(previousMonthFinances, false);
        metrics.current_month_income = round2(monthIncome);
        metrics.current_month_expense = round2(monthExpense);
        metrics.current_month_net_income = round2(monthIncome - monthExpense);
        ExpenseBreakdown currentMonthOperatingExpense = buildOperatingExpenseBreakdown(currentMonthFinances);
        metrics.current_month_operating_expense = currentMonthOperatingExpense.total();
        metrics.current_month_material_expense = currentMonthOperatingExpense.material();
        metrics.current_month_lab_expense = currentMonthOperatingExpense.lab();
        metrics.current_month_other_expense = currentMonthOperatingExpense.other();
        metrics.previous_month_net_income = round2(previousMonthIncome - previousMonthExpense);
        metrics.month_net_change_rate = calculateRate(metrics.current_month_net_income, metrics.previous_month_net_income);

        double treatmentRevenue = todayTreatments.stream()
                .map(Treatment::getTreatment_fee)
                .map(this::parseAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
        metrics.today_treatment_revenue = round2(treatmentRevenue);
        double receivedTreatmentAmount = todayTreatments.stream()
                .mapToDouble(item -> resolveNetPaidAmount(financeByTreatmentId.get(item.getId())))
                .sum();
        metrics.today_treatment_received_amount = round2(receivedTreatmentAmount);
        metrics.today_treatment_unreceived_amount = round2(Math.max(0D, metrics.today_treatment_revenue - metrics.today_treatment_received_amount));

        int completedTreatmentCount = (int) todayTreatments.stream()
                .filter(item -> "完成".equals(trim(item.getStatus())))
                .count();
        metrics.completed_treatment_count = completedTreatmentCount;
        metrics.avg_income_per_appointment = metrics.today_appointments == 0 ? 0D : round2(metrics.today_income / metrics.today_appointments);
        metrics.record_completion_rate = metrics.today_appointments == 0 ? 0D : round2(metrics.today_medical_records * 100D / metrics.today_appointments);

        metrics.top_doctors = buildTopDoctorMetrics(todayAppointments, todayTreatments);
        metrics.top_projects = buildTopProjectMetrics(todayTreatments);
        metrics.data_limitations = new ArrayList<>();
        return metrics;
    }

    private List<DoctorMetric> buildTopDoctorMetrics(List<Appointment> todayAppointments, List<Treatment> todayTreatments) {
        Map<String, DoctorMetric> metricsByDoctor = new HashMap<>();
        for (Appointment appointment : todayAppointments) {
            String doctorName = trim(appointment.getDoctor_name());
            if (!StringUtils.hasText(doctorName)) {
                doctorName = "未指定医生";
            }
            DoctorMetric metric = metricsByDoctor.computeIfAbsent(doctorName, key -> new DoctorMetric());
            metric.doctor_name = doctorName;
            metric.appointment_count += 1;
        }
        for (Treatment treatment : todayTreatments) {
            String doctorName = trim(treatment.getDoctor_name());
            if (!StringUtils.hasText(doctorName)) {
                doctorName = "未指定医生";
            }
            DoctorMetric metric = metricsByDoctor.computeIfAbsent(doctorName, key -> new DoctorMetric());
            metric.doctor_name = doctorName;
            metric.treatment_count += 1;
            metric.treatment_revenue = round2(metric.treatment_revenue + defaultDouble(parseAmount(treatment.getTreatment_fee())));
        }
        return metricsByDoctor.values().stream()
                .sorted(Comparator.comparingInt((DoctorMetric item) -> item.appointment_count).reversed()
                        .thenComparing(Comparator.comparingDouble((DoctorMetric item) -> item.treatment_revenue).reversed())
                        .thenComparing(item -> item.doctor_name, Comparator.nullsLast(String::compareTo)))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<ProjectMetric> buildTopProjectMetrics(List<Treatment> todayTreatments) {
        Map<String, ProjectMetric> metricsByProject = new HashMap<>();
        for (Treatment treatment : todayTreatments) {
            String projectName = normalizeProjectName(treatment);
            ProjectMetric metric = metricsByProject.computeIfAbsent(projectName, key -> new ProjectMetric());
            metric.project_name = projectName;
            metric.case_count += 1;
            metric.revenue = round2(metric.revenue + defaultDouble(parseAmount(treatment.getTreatment_fee())));
        }
        return metricsByProject.values().stream()
                .sorted(Comparator.comparingDouble((ProjectMetric item) -> item.revenue).reversed()
                        .thenComparing(Comparator.comparingInt((ProjectMetric item) -> item.case_count).reversed())
                        .thenComparing(item -> item.project_name, Comparator.nullsLast(String::compareTo)))
                .limit(5)
                .collect(Collectors.toList());
    }

    private int countUniquePatients(List<Appointment> todayAppointments,
                                    List<MedicalRecord> todayRecords,
                                    List<Treatment> todayTreatments) {
        Set<Long> patientIds = new HashSet<>();
        todayAppointments.stream().map(Appointment::getPatient_id).filter(Objects::nonNull).forEach(patientIds::add);
        todayRecords.stream().map(MedicalRecord::getPatient_id).filter(Objects::nonNull).forEach(patientIds::add);
        todayTreatments.stream().map(Treatment::getPatient_id).filter(Objects::nonNull).forEach(patientIds::add);
        return patientIds.size();
    }

    // 内置AI分析相关方法已移除，所有AI分析请通过n8n外部工作流完成

    /**
     * 精简日报视图，过滤掉元数据字段，仅保留业务字段
     */
    private Map<String, Object> simplifyAnalysisView(Map<String, Object> view) {
        if (view == null) {
            return null;
        }
        Map<String, Object> simplified = new LinkedHashMap<>();
        copyIfPresent(view, simplified, "analysis_date");
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

    private Map<String, Object> buildView(BusinessDailyAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", analysis.getId());
        view.put("analysis_date", formatDate(analysis.getAnalysis_date()));
        view.put("analysis_status", analysis.getAnalysis_status());
        view.put("source_type", analysis.getSource_type());
        view.put("trigger_type", analysis.getTrigger_type());
        view.put("model_name", analysis.getModel_name());
        view.put("operating_score", analysis.getOperating_score());
        view.put("trend", analysis.getTrend());
        view.put("headline", analysis.getHeadline());
        view.put("summary", analysis.getSummary());
        view.put("metrics", parseJsonToObject(analysis.getMetrics_json()));
        view.put("analysis", parseJsonToObject(analysis.getAnalysis_json()));
        view.put("error_message", analysis.getError_message());
        view.put("created_at", formatDateTime(analysis.getCreated_at()));
        view.put("updated_at", formatDateTime(analysis.getUpdated_at()));
        return view;
    }

    private BusinessDailyAnalysis normalizeStalePending(BusinessDailyAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        if (!STATUS_PENDING.equals(trim(analysis.getAnalysis_status()))) {
            return analysis;
        }
        java.util.Date updatedAt = analysis.getUpdated_at();
        if (updatedAt == null) {
            return analysis;
        }
        Instant staleAt = updatedAt.toInstant().plusSeconds(STALE_PENDING_SECONDS);
        if (staleAt.isAfter(Instant.now())) {
            return analysis;
        }
        LocalDate analysisDate = analysis.getAnalysis_date() == null
                ? null
                : Instant.ofEpochMilli(analysis.getAnalysis_date().getTime()).atZone(DEFAULT_ZONE).toLocalDate();
        if (analysisDate == null) {
            return analysis;
        }
        markDailyAnalysisFailed(analysisDate, analysis.getTrigger_type(), "后台任务超时未完成，系统已自动标记失败，请重新提交。");
        return analysisMapper.selectById(analysis.getId());
    }

    private Object parseJsonToObject(String json) {
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

    private double sumByType(List<Finance> finances, boolean income) {
        return round2(finances.stream()
                .filter(item -> income == isIncomeType(item.getType()))
                .mapToDouble(Finance::getAmount)
                .sum());
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

    private double resolveNetPaidAmount(List<Finance> linkedFinances) {
        if (linkedFinances == null || linkedFinances.isEmpty()) {
            return 0D;
        }
        double chargeAmount = 0D;
        double refundAmount = 0D;
        for (Finance finance : linkedFinances) {
            if (finance == null) {
                continue;
            }
            double amount = round2(finance.getAmount());
            if (amount <= 0) {
                continue;
            }
            if (isRefundFinance(finance)) {
                refundAmount += amount;
            } else if (isChargeFinance(finance)) {
                chargeAmount += amount;
            }
        }
        return round2(Math.max(0D, chargeAmount - refundAmount));
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
        return type.contains("收入") || type.contains("收费");
    }

    private boolean isRefundFinance(Finance finance) {
        String bizType = trim(finance.getBiz_type()).toUpperCase(Locale.ROOT);
        if ("TREATMENT_REFUND".equals(bizType)) {
            return true;
        }
        if ("TREATMENT_CHARGE".equals(bizType)) {
            return false;
        }
        return trim(finance.getType()).contains("退款");
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

    private boolean isSameDate(java.util.Date value, LocalDate targetDate) {
        if (value == null || targetDate == null) {
            return false;
        }
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDate().equals(targetDate);
    }

    private boolean isWithinNextDays(java.util.Date value, LocalDate targetDate, int days) {
        if (value == null || targetDate == null || days <= 0) {
            return false;
        }
        LocalDate itemDate = Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDate();
        long diff = ChronoUnit.DAYS.between(targetDate, itemDate);
        return diff >= 1 && diff <= days;
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

    private String normalizeTriggerType(String triggerType) {
        return StringUtils.hasText(triggerType) ? triggerType.trim().toUpperCase(Locale.ROOT) : TRIGGER_MANUAL;
    }

    private String trimToLimit(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String text = value.trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String formatDate(java.util.Date value) {
        if (value == null) {
            return "";
        }
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDate().toString();
    }

    private String formatDateTime(java.util.Date value) {
        if (value == null) {
            return "";
        }
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDateTime().toString().replace('T', ' ');
    }

    public static class DailyBusinessMetrics {
        public String analysis_date;
        public int total_patients;
        public int today_appointments;
        public int today_medical_records;
        public int today_treatments;
        public int today_unique_patients;
        public int future_7_day_appointments;
        public Map<String, Integer> appointment_status_breakdown;
        public double cancellation_rate;
        public double today_income;
        public double today_expense;
        public double today_net_income;
        public double today_operating_expense;
        public double today_material_expense;
        public double today_lab_expense;
        public double today_other_expense;
        public double today_treatment_revenue;
        public double today_treatment_received_amount;
        public double today_treatment_unreceived_amount;
        public int completed_treatment_count;
        public double avg_income_per_appointment;
        public double record_completion_rate;
        public double current_month_income;
        public double current_month_expense;
        public double current_month_net_income;
        public double current_month_operating_expense;
        public double current_month_material_expense;
        public double current_month_lab_expense;
        public double current_month_other_expense;
        public double previous_month_net_income;
        public double month_net_change_rate;
        public List<DoctorMetric> top_doctors;
        public List<ProjectMetric> top_projects;
        public List<String> data_limitations;
    }

    private record ExpenseBreakdown(double material, double lab, double other) {
        double total() {
            return Math.round((material + lab + other) * 100D) / 100D;
        }
    }

    public static class DoctorMetric {
        public String doctor_name;
        public int appointment_count;
        public int treatment_count;
        public double treatment_revenue;
    }

    public static class ProjectMetric {
        public String project_name;
        public int case_count;
        public double revenue;
    }

    public static class BusinessAnalysisOutput {
        public String headline;
        public String summary;
        public Integer operating_score;
        public String trend;
        public List<String> highlights = new ArrayList<>();
        public List<RiskOutput> risks = new ArrayList<>();
        public List<OpportunityOutput> opportunities = new ArrayList<>();
        public List<ActionOutput> actions = new ArrayList<>();
        public String management_brief;
    }

    public static class RiskOutput {
        public String title;
        public String severity;
        public String finding;
        public String recommendation;
    }

    public static class OpportunityOutput {
        public String title;
        public String impact;
        public String finding;
        public String recommendation;
    }

    public static class ActionOutput {
        public String priority;
        public String action;
        public String owner;
        public String due;
        public String expected_result;
    }
}
