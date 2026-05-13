package com.example.springboot.service;

import com.example.springboot.config.OpenAiAnalysisProperties;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Duration;
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
    private static final String SOURCE_OPENAI = "OPENAI";
    private static final String SOURCE_RULE_BASED = "RULE_BASED";
    private static final String TRIGGER_SCHEDULED = "SCHEDULED";
    private static final String TRIGGER_MANUAL = "MANUAL";
    private static final Duration OPENAI_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration OPENAI_REQUEST_TIMEOUT = Duration.ofSeconds(45);
    private static final long STALE_PENDING_SECONDS = 90L;
    private static final int OPENAI_MAX_RETRIES = 3;

    private final BusinessDailyAnalysisMapper analysisMapper;
    private final AppointmentMapper appointmentMapper;
    private final FinanceMapper financeMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final TreatmentMapper treatmentMapper;
    private final PatientMapper patientMapper;
    private final OpenAiAnalysisProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public BusinessDailyAnalysisService(BusinessDailyAnalysisMapper analysisMapper,
                                        AppointmentMapper appointmentMapper,
                                        FinanceMapper financeMapper,
                                        MedicalRecordMapper medicalRecordMapper,
                                        TreatmentMapper treatmentMapper,
                                        PatientMapper patientMapper,
                                        OpenAiAnalysisProperties openAiProperties,
                                        ObjectMapper objectMapper) {
        this.analysisMapper = analysisMapper;
        this.appointmentMapper = appointmentMapper;
        this.financeMapper = financeMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.treatmentMapper = treatmentMapper;
        this.patientMapper = patientMapper;
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(OPENAI_CONNECT_TIMEOUT)
                .build();
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
        entity.setSource_type(canUseOpenAi() ? SOURCE_OPENAI : SOURCE_RULE_BASED);
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name(openAiProperties.getBusinessAnalysis().getModel());
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
        entity.setSource_type(canUseOpenAi() ? SOURCE_OPENAI : SOURCE_RULE_BASED);
        entity.setTrigger_type(normalizeTriggerType(triggerType));
        entity.setModel_name(openAiProperties.getBusinessAnalysis().getModel());
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
        entity.setModel_name(openAiProperties.getBusinessAnalysis().getModel());
        entity.setMetrics_json(writeJson(metrics));

        String rawResponse = "";
        String errorMessage = "";
        BusinessAnalysisOutput output;
        String sourceType;
        String status;

        try {
            if (canUseOpenAi()) {
                OpenAiAnalysisResult aiResult = requestOpenAiAnalysis(metrics);
                rawResponse = aiResult.rawResponse;
                output = aiResult.output;
                sourceType = SOURCE_OPENAI;
                status = STATUS_SUCCESS;
            } else {
                output = buildRuleBasedAnalysis(metrics, null);
                sourceType = SOURCE_RULE_BASED;
                status = STATUS_FALLBACK;
                errorMessage = "OpenAI 未启用或未配置 API Key，已生成规则分析";
            }
        } catch (Exception exception) {
            String rootMessage = simplifyException(exception);
            output = buildRuleBasedAnalysis(metrics, rootMessage);
            sourceType = SOURCE_RULE_BASED;
            status = STATUS_FALLBACK;
            errorMessage = "OpenAI 分析失败，已回退规则分析：" + rootMessage;
        }

        entity.setAnalysis_status(status);
        entity.setSource_type(sourceType);
        entity.setHeadline(trimToLimit(output.headline, 255));
        entity.setSummary(output.summary);
        entity.setOperating_score(output.operating_score);
        entity.setTrend(output.trend);
        entity.setAnalysis_json(writeJson(output));
        entity.setRaw_response(rawResponse);
        entity.setError_message(trimToLimit(errorMessage, 1000));

        save(entity);
        Map<String, Object> view = buildView(entity);
        return view;
    }

    public Map<String, Object> getLatestAnalysis() {
        return buildView(normalizeStalePending(analysisMapper.selectLatest()));
    }

    public Map<String, Object> getAnalysisById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return buildView(normalizeStalePending(analysisMapper.selectById(id)));
    }

    public List<Map<String, Object>> getRecentAnalyses(Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 90);
        return analysisMapper.selectRecent(safeLimit).stream()
                .map(this::normalizeStalePending)
                .map(this::buildView)
                .collect(Collectors.toList());
    }

    public Map<String, Object> testModelConnection() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", "OpenAI");
        result.put("wire_api", "responses");
        result.put("base_url", trimTrailingSlash(openAiProperties.getBaseUrl()));
        result.put("model", openAiProperties.getBusinessAnalysis().getModel());
        result.put("reasoning_effort", openAiProperties.getBusinessAnalysis().getReasoningEffort());
        result.put("response_storage_disabled", openAiProperties.isDisableResponseStorage());
        result.put("checked_at", formatDateTime(java.util.Date.from(Instant.now())));

        if (!openAiProperties.isEnabled()) {
            result.put("connected", false);
            result.put("message", "OPENAI_ENABLED 未开启");
            return result;
        }
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            result.put("connected", false);
            result.put("message", "OPENAI_API_KEY 未配置");
            return result;
        }

        try {
            String outputText = requestOpenAiProbe();
            result.put("connected", true);
            result.put("message", "模型连接成功");
            result.put("response_sample", trimToLimit(outputText, 200));
        } catch (Exception exception) {
            result.put("connected", false);
            result.put("message", simplifyException(exception));
        }
        return result;
    }

    public DailyBusinessMetrics buildDailyMetricsForDate(LocalDate targetDate) {
        return buildMetrics(targetDate);
    }

    public boolean isOpenAiReady() {
        return canUseOpenAi();
    }

    public BusinessAnalysisOutput requestAiAnalysis(String instructions, String prompt) throws IOException, InterruptedException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        applyRequestDefaults(requestBody);
        requestBody.put("instructions", instructions);
        ArrayNode input = requestBody.putArray("input");
        ObjectNode userMessage = input.addObject();
        userMessage.put("role", "user");
        ArrayNode content = userMessage.putArray("content");
        content.addObject()
                .put("type", "input_text")
                .put("text", prompt);
        requestBody.put("max_output_tokens", openAiProperties.getBusinessAnalysis().getMaxOutputTokens());

        ObjectNode text = requestBody.putObject("text");
        ObjectNode format = text.putObject("format");
        format.put("type", "json_schema");
        format.put("name", "daily_business_analysis");
        format.put("strict", true);
        format.set("schema", buildAnalysisSchema());

        JsonNode responseJson = sendResponsesRequest(requestBody);
        String outputText = extractOutputText(responseJson);
        if (!StringUtils.hasText(outputText)) {
            throw new IOException("未从 Responses API 提取到结构化输出");
        }
        return parseAnalysisOutput(outputText);
    }

    private void save(BusinessDailyAnalysis entity) {
        if (entity.getId() == null) {
            analysisMapper.insert(entity);
        } else {
            analysisMapper.update(entity);
        }
    }

    private boolean canUseOpenAi() {
        return openAiProperties.isEnabled()
                && StringUtils.hasText(openAiProperties.getApiKey())
                && StringUtils.hasText(openAiProperties.getBaseUrl())
                && StringUtils.hasText(openAiProperties.getBusinessAnalysis().getModel());
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
        List<String> limitations = new ArrayList<>();
        limitations.add("患者主数据缺少建档时间，无法准确统计当日新增患者");
        limitations.add("经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。");
        if (Math.abs(metrics.today_treatment_revenue - metrics.today_income) > 0.009D) {
            limitations.add("项目与医生维度金额按治疗记录标价汇总，不等同于当天实收；实收请以当日收入和净现金流为准。");
        }
        long dirtyProjectCount = todayTreatments.stream().filter(this::hasDirtyProjectName).count();
        if (dirtyProjectCount > 0) {
            limitations.add("检测到 " + dirtyProjectCount + " 条处置项目名称不规范，系统已按治疗详情或“未规范项目”归类。");
        }
        metrics.data_limitations = limitations;
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

    private OpenAiAnalysisResult requestOpenAiAnalysis(DailyBusinessMetrics metrics) throws IOException, InterruptedException {
        OpenAiAnalysisResult result = new OpenAiAnalysisResult();
        result.output = requestAiAnalysis(
                "你是口腔门诊经营分析顾问。你只能根据提供的数据分析，不允许编造未提供的事实。输出需要面向门诊管理者，可执行、克制、重结论。",
                buildPrompt(metrics)
        );
        result.rawResponse = writeJson(result.output);
        return result;
    }

    private String requestOpenAiProbe() throws IOException, InterruptedException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        applyRequestDefaults(requestBody);
        requestBody.put("max_output_tokens", 60);
        ArrayNode input = requestBody.putArray("input");
        ObjectNode userMessage = input.addObject();
        userMessage.put("role", "user");
        ArrayNode content = userMessage.putArray("content");
        content.addObject()
                .put("type", "input_text")
                .put("text", "请只返回一句简短中文：连接测试成功。");
        JsonNode responseJson = sendResponsesRequest(requestBody);
        String outputText = extractOutputText(responseJson);
        if (!StringUtils.hasText(outputText)) {
            throw new IOException("模型未返回可读文本");
        }
        return outputText;
    }

    private void applyRequestDefaults(ObjectNode requestBody) {
        requestBody.put("model", openAiProperties.getBusinessAnalysis().getModel());
        requestBody.put("store", !openAiProperties.isDisableResponseStorage());
        if (StringUtils.hasText(openAiProperties.getBusinessAnalysis().getReasoningEffort())) {
            requestBody.putObject("reasoning").put("effort", openAiProperties.getBusinessAnalysis().getReasoningEffort());
        }
    }

    private JsonNode sendResponsesRequest(ObjectNode requestBody) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(requestBody);
        IOException lastIOException = null;
        InterruptedException lastInterruptedException = null;
        for (int attempt = 1; attempt <= OPENAI_MAX_RETRIES; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(openAiProperties.getBaseUrl()) + "/responses"))
                    .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .timeout(OPENAI_REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readTree(response.body());
                }
                if (!isRetryableStatus(response.statusCode()) || attempt == OPENAI_MAX_RETRIES) {
                    throw new IOException("HTTP " + response.statusCode() + " " + response.body());
                }
                sleepBeforeRetry(attempt);
            } catch (IOException exception) {
                lastIOException = exception;
                if (attempt == OPENAI_MAX_RETRIES) {
                    throw exception;
                }
                sleepBeforeRetry(attempt);
            } catch (InterruptedException exception) {
                lastInterruptedException = exception;
                Thread.currentThread().interrupt();
                throw exception;
            }
        }
        if (lastIOException != null) {
            throw lastIOException;
        }
        if (lastInterruptedException != null) {
            throw lastInterruptedException;
        }
        throw new IOException("请求 OpenAI Responses API 失败");
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        long delayMillis = Math.min(4000L, 500L * attempt);
        Thread.sleep(delayMillis);
    }

    private String buildPrompt(DailyBusinessMetrics metrics) throws JsonProcessingException {
        return "请为口腔门诊生成 " + metrics.analysis_date + " 的经营日报深度分析。\n"
                + "分析重点：接诊效率、收入质量、支出结构、患者运营、风险预警、未来7日排班压力。\n"
                + "输出要求：\n"
                + "1. 只依据提供数据，不要编造。\n"
                + "2. headline 用一句话说明当天经营状态。\n"
                + "3. summary 80-160字，适合老板快速浏览。\n"
                + "4. highlights 2-4条，聚焦关键结果。\n"
                + "5. risks 2-4条，opportunities 2-4条，actions 3-6条。\n"
                + "6. operating_score 为0-100整数，trend 只能是 up/flat/down。\n"
                + "7. severity / impact 只能是 high/medium/low，priority 只能是 P0/P1/P2，due 只能是 today/3days/7days/14days。\n"
                + "8. 如数据不足，要在 summary 或 management_brief 中明确指出不确定性。\n\n"
                + "原始经营数据JSON如下：\n"
                + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metrics);
    }

    private JsonNode buildAnalysisSchema() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "object");
        root.putArray("required")
                .add("headline")
                .add("summary")
                .add("operating_score")
                .add("trend")
                .add("highlights")
                .add("risks")
                .add("opportunities")
                .add("actions")
                .add("management_brief");
        root.put("additionalProperties", false);

        ObjectNode properties = root.putObject("properties");
        properties.putObject("headline")
                .put("type", "string")
                .put("maxLength", 120);
        properties.putObject("summary")
                .put("type", "string")
                .put("maxLength", 400);
        properties.putObject("operating_score")
                .put("type", "integer")
                .put("minimum", 0)
                .put("maximum", 100);
        ObjectNode trend = properties.putObject("trend");
        trend.put("type", "string");
        trend.putArray("enum").add("up").add("flat").add("down");

        ObjectNode highlights = properties.putObject("highlights");
        highlights.put("type", "array");
        highlights.put("minItems", 2);
        highlights.put("maxItems", 4);
        highlights.set("items", objectMapper.createObjectNode().put("type", "string").put("maxLength", 120));

        ObjectNode riskItem = objectMapper.createObjectNode();
        riskItem.put("type", "object");
        riskItem.put("additionalProperties", false);
        riskItem.putArray("required").add("title").add("severity").add("finding").add("recommendation");
        ObjectNode riskProperties = riskItem.putObject("properties");
        riskProperties.putObject("title").put("type", "string").put("maxLength", 80);
        ObjectNode severity = riskProperties.putObject("severity");
        severity.put("type", "string");
        severity.putArray("enum").add("high").add("medium").add("low");
        riskProperties.putObject("finding").put("type", "string").put("maxLength", 180);
        riskProperties.putObject("recommendation").put("type", "string").put("maxLength", 180);

        ObjectNode risks = properties.putObject("risks");
        risks.put("type", "array");
        risks.put("minItems", 2);
        risks.put("maxItems", 4);
        risks.set("items", riskItem);

        ObjectNode opportunityItem = objectMapper.createObjectNode();
        opportunityItem.put("type", "object");
        opportunityItem.put("additionalProperties", false);
        opportunityItem.putArray("required").add("title").add("impact").add("finding").add("recommendation");
        ObjectNode opportunityProperties = opportunityItem.putObject("properties");
        opportunityProperties.putObject("title").put("type", "string").put("maxLength", 80);
        ObjectNode impact = opportunityProperties.putObject("impact");
        impact.put("type", "string");
        impact.putArray("enum").add("high").add("medium").add("low");
        opportunityProperties.putObject("finding").put("type", "string").put("maxLength", 180);
        opportunityProperties.putObject("recommendation").put("type", "string").put("maxLength", 180);

        ObjectNode opportunities = properties.putObject("opportunities");
        opportunities.put("type", "array");
        opportunities.put("minItems", 2);
        opportunities.put("maxItems", 4);
        opportunities.set("items", opportunityItem);

        ObjectNode actionItem = objectMapper.createObjectNode();
        actionItem.put("type", "object");
        actionItem.put("additionalProperties", false);
        actionItem.putArray("required").add("priority").add("action").add("owner").add("due").add("expected_result");
        ObjectNode actionProperties = actionItem.putObject("properties");
        ObjectNode priority = actionProperties.putObject("priority");
        priority.put("type", "string");
        priority.putArray("enum").add("P0").add("P1").add("P2");
        actionProperties.putObject("action").put("type", "string").put("maxLength", 140);
        actionProperties.putObject("owner").put("type", "string").put("maxLength", 60);
        ObjectNode due = actionProperties.putObject("due");
        due.put("type", "string");
        due.putArray("enum").add("today").add("3days").add("7days").add("14days");
        actionProperties.putObject("expected_result").put("type", "string").put("maxLength", 160);

        ObjectNode actions = properties.putObject("actions");
        actions.put("type", "array");
        actions.put("minItems", 3);
        actions.put("maxItems", 6);
        actions.set("items", actionItem);

        properties.putObject("management_brief")
                .put("type", "string")
                .put("maxLength", 300);
        return root;
    }

    private String extractOutputText(JsonNode responseJson) {
        if (responseJson == null) {
            return "";
        }
        JsonNode direct = responseJson.get("output_text");
        if (direct != null && direct.isTextual() && StringUtils.hasText(direct.asText())) {
            return direct.asText();
        }
        JsonNode output = responseJson.get("output");
        if (output != null && output.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content == null || !content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    JsonNode textNode = contentItem.get("text");
                    if (textNode != null && textNode.isTextual()) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(textNode.asText());
                    }
                }
            }
            return builder.toString().trim();
        }
        return "";
    }

    private BusinessAnalysisOutput parseAnalysisOutput(String outputText) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(outputText);
        BusinessAnalysisOutput output = new BusinessAnalysisOutput();
        output.headline = readText(root, "headline", 120);
        output.summary = readText(root, "summary", 400);
        output.operating_score = clampScore(readInt(root.get("operating_score")));
        output.trend = normalizeTrend(readText(root, "trend", 16));
        output.highlights = readStringArray(root.get("highlights"), 4, 120);
        output.risks = readRiskOutputs(root.get("risks"), 4);
        output.opportunities = readOpportunityOutputs(root.get("opportunities"), 4);
        output.actions = readActionOutputs(root.get("actions"), 6);
        output.management_brief = readText(root, "management_brief", 300);

        if (!StringUtils.hasText(output.headline)
                || !StringUtils.hasText(output.summary)
                || output.operating_score == null
                || !StringUtils.hasText(output.trend)
                || output.highlights.isEmpty()
                || output.risks.isEmpty()
                || output.opportunities.isEmpty()
                || output.actions.isEmpty()
                || !StringUtils.hasText(output.management_brief)) {
            throw new JsonProcessingException("AI 输出缺少必要字段") {};
        }
        return output;
    }

    private List<String> readStringArray(JsonNode node, int maxItems, int maxLength) {
        List<String> values = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return values;
        }
        for (JsonNode item : node) {
            String value = trimToLimit(item == null ? "" : item.asText(""), maxLength);
            if (StringUtils.hasText(value)) {
                values.add(value);
            }
            if (values.size() >= maxItems) {
                break;
            }
        }
        return values;
    }

    private List<RiskOutput> readRiskOutputs(JsonNode node, int maxItems) {
        List<RiskOutput> outputs = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return outputs;
        }
        for (JsonNode item : node) {
            RiskOutput output = new RiskOutput();
            output.title = readText(item, "title", 80);
            output.severity = normalizeLevel(readText(item, "severity", 16));
            output.finding = readText(item, "finding", 180);
            output.recommendation = readText(item, "recommendation", 180);
            if (StringUtils.hasText(output.title)
                    && StringUtils.hasText(output.severity)
                    && StringUtils.hasText(output.finding)
                    && StringUtils.hasText(output.recommendation)) {
                outputs.add(output);
            }
            if (outputs.size() >= maxItems) {
                break;
            }
        }
        return outputs;
    }

    private List<OpportunityOutput> readOpportunityOutputs(JsonNode node, int maxItems) {
        List<OpportunityOutput> outputs = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return outputs;
        }
        for (JsonNode item : node) {
            OpportunityOutput output = new OpportunityOutput();
            output.title = readText(item, "title", 80);
            output.impact = normalizeLevel(readText(item, "impact", 16));
            output.finding = readText(item, "finding", 180);
            output.recommendation = readText(item, "recommendation", 180);
            if (StringUtils.hasText(output.title)
                    && StringUtils.hasText(output.impact)
                    && StringUtils.hasText(output.finding)
                    && StringUtils.hasText(output.recommendation)) {
                outputs.add(output);
            }
            if (outputs.size() >= maxItems) {
                break;
            }
        }
        return outputs;
    }

    private List<ActionOutput> readActionOutputs(JsonNode node, int maxItems) {
        List<ActionOutput> outputs = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return outputs;
        }
        for (JsonNode item : node) {
            ActionOutput output = new ActionOutput();
            output.priority = normalizePriority(readText(item, "priority", 16));
            output.action = readText(item, "action", 140);
            output.owner = readText(item, "owner", 60);
            output.due = normalizeDue(readText(item, "due", 16));
            output.expected_result = readText(item, "expected_result", 160);
            if (StringUtils.hasText(output.priority)
                    && StringUtils.hasText(output.action)
                    && StringUtils.hasText(output.owner)
                    && StringUtils.hasText(output.due)
                    && StringUtils.hasText(output.expected_result)) {
                outputs.add(output);
            }
            if (outputs.size() >= maxItems) {
                break;
            }
        }
        return outputs;
    }

    private String readText(JsonNode node, String fieldName, int maxLength) {
        if (node == null || fieldName == null) {
            return "";
        }
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return "";
        }
        return trimToLimit(field.asText(""), maxLength);
    }

    private Integer readInt(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            long value = node.asLong();
            return (int) Math.max(0, Math.min(100, value));
        }
        if (node.isNumber()) {
            return (int) Math.round(node.asDouble());
        }
        if (node.isTextual()) {
            try {
                return (int) Math.round(Double.parseDouble(node.asText().trim()));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer clampScore(Integer value) {
        if (value == null) {
            return null;
        }
        return Math.max(0, Math.min(100, value));
    }

    private String normalizeTrend(String value) {
        String text = trim(value).toLowerCase(Locale.ROOT);
        if ("up".equals(text) || "flat".equals(text) || "down".equals(text)) {
            return text;
        }
        return "";
    }

    private String normalizeLevel(String value) {
        String text = trim(value).toLowerCase(Locale.ROOT);
        if ("high".equals(text) || "medium".equals(text) || "low".equals(text)) {
            return text;
        }
        return "";
    }

    private String normalizePriority(String value) {
        String text = trim(value).toUpperCase(Locale.ROOT);
        if ("P0".equals(text) || "P1".equals(text) || "P2".equals(text)) {
            return text;
        }
        return "";
    }

    private String normalizeDue(String value) {
        String text = trim(value).toLowerCase(Locale.ROOT);
        if ("today".equals(text) || "3days".equals(text) || "7days".equals(text) || "14days".equals(text)) {
            return text;
        }
        return "";
    }

    private BusinessAnalysisOutput buildRuleBasedAnalysis(DailyBusinessMetrics metrics, String fallbackReason) {
        BusinessAnalysisOutput output = new BusinessAnalysisOutput();
        output.operating_score = calculateOperatingScore(metrics);
        output.trend = metrics.month_net_change_rate > 8 ? "up" : (metrics.month_net_change_rate < -8 ? "down" : "flat");
        output.headline = buildFallbackHeadline(metrics, output.trend);
        output.summary = buildFallbackSummary(metrics, fallbackReason);
        output.management_brief = buildManagementBrief(metrics, fallbackReason);

        output.highlights = new ArrayList<>();
        output.highlights.add("当日预约 " + metrics.today_appointments + " 人次，病历记录 " + metrics.today_medical_records + " 份，治疗 " + metrics.today_treatments + " 例。");
        output.highlights.add("当日收入 ¥" + formatMoney(metrics.today_income) + "，支出 ¥" + formatMoney(metrics.today_expense) + "，净现金流 ¥" + formatMoney(metrics.today_net_income) + "。");
        if (metrics.today_operating_expense > 0) {
            output.highlights.add("经营支出中耗材 ¥" + formatMoney(metrics.today_material_expense) + "、加工 ¥" + formatMoney(metrics.today_lab_expense) + "、非耗材 ¥" + formatMoney(metrics.today_other_expense) + "。");
        }
        output.highlights.add("未来7日未取消预约 " + metrics.future_7_day_appointments + " 人次，需提前做好排班和回访准备。");
        if (!metrics.top_doctors.isEmpty()) {
            DoctorMetric topDoctor = metrics.top_doctors.get(0);
            output.highlights.add("当日接诊负荷最高医生为 " + topDoctor.doctor_name + "，预约 " + topDoctor.appointment_count + " 人次。");
        }
        output.highlights = output.highlights.stream().limit(4).collect(Collectors.toList());

        output.risks = new ArrayList<>();
        if (metrics.cancellation_rate >= 25) {
            output.risks.add(risk("取消率偏高", "high",
                    "当日预约取消率 " + formatPercent(metrics.cancellation_rate) + "，到诊稳定性不足。",
                    "复盘取消原因，针对次日和3日内预约患者做二次确认。"));
        }
        if (metrics.today_net_income < 0) {
            output.risks.add(risk("现金流为负", "high",
                    "当日净现金流为负，收入无法覆盖支出。",
                    "优先核对支出构成和未收费治疗，必要时调整采购与收款节奏。"));
        }
        if (metrics.record_completion_rate < 60 && metrics.today_appointments >= 3) {
            output.risks.add(risk("病历留存偏低", "medium",
                    "病历完成率仅 " + formatPercent(metrics.record_completion_rate) + "，存在医疗记录闭环不足风险。",
                    "要求接诊结束当日补齐病历，设置护士/前台复核。"));
        }
        if (metrics.future_7_day_appointments >= 18) {
            output.risks.add(risk("未来一周排班压力大", "medium",
                    "未来7日预约量较高，若临时变更将影响接诊效率。",
                    "提前锁定医生出勤并安排重点项目耗材准备。"));
        }
        if (output.risks.isEmpty()) {
            output.risks.add(risk("数据覆盖有限", "low",
                    "当前日报未纳入库存与新增患者建档数据，部分判断偏保守。",
                    "后续补充库存预警和患者建档时间字段，提升经营判断完整性。"));
        }
        if (output.risks.size() < 2) {
            output.risks.add(risk("收费与治疗口径存在时间差", "low",
                    "治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。",
                    "复盘日报时同步查看当日处置金额、已收费金额和未收费金额。"));
        }
        output.risks = output.risks.stream().limit(4).collect(Collectors.toList());

        output.opportunities = new ArrayList<>();
        if (metrics.today_income > 0 && metrics.avg_income_per_appointment >= 300) {
            output.opportunities.add(opportunity("客单价表现可放大", "high",
                    "单预约平均收入达 ¥" + formatMoney(metrics.avg_income_per_appointment) + "。",
                    "复用高客单项目的话术与组合套餐，扩展到复诊与洁牙转化场景。"));
        }
        if (!metrics.top_projects.isEmpty() && !"未规范项目".equals(metrics.top_projects.get(0).project_name)) {
            ProjectMetric topProject = metrics.top_projects.get(0);
            output.opportunities.add(opportunity("优势项目可重点推广", "medium",
                    topProject.project_name + " 当日处置金额最高，标价 ¥" + formatMoney(topProject.revenue) + "。",
                    "在预约确认、复诊建议和患者教育中强化该项目转化。"));
        }
        if (metrics.future_7_day_appointments >= 6) {
            output.opportunities.add(opportunity("未来一周到诊基础良好", "medium",
                    "未来7日已有 " + metrics.future_7_day_appointments + " 条有效预约，可形成稳定流水。",
                    "提前做分层提醒和项目预匹配，提升到诊率与转化率。"));
        }
        if (metrics.today_unique_patients > 0 && metrics.today_medical_records >= metrics.today_unique_patients) {
            output.opportunities.add(opportunity("接诊闭环基础较好", "low",
                    "当日接诊患者与病历记录匹配度较高，说明流程执行较稳定。",
                    "继续把病历、治疗、收费三张表做联动复盘，沉淀标准流程。"));
        }
        while (output.opportunities.size() < 2) {
            output.opportunities.add(opportunity("经营复盘空间明确", "low",
                    "现有预约、治疗、收费数据已具备日级分析基础。",
                    "持续沉淀日报，逐步形成周报和月报趋势管理。"));
        }
        output.opportunities = output.opportunities.stream().limit(4).collect(Collectors.toList());

        output.actions = new ArrayList<>();
        output.actions.add(action("P0", "对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。", "前台/客服", "today", "降低临时取消，稳定接诊节奏。"));
        output.actions.add(action("P1", "核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。", "护士长", "3days", "提升病历完整率，减少漏记漏收费。"));
        if (!metrics.top_doctors.isEmpty()) {
            output.actions.add(action("P1", "围绕 " + metrics.top_doctors.get(0).doctor_name + " 的接诊高峰优化排班和椅位分配。", "门诊经理", "7days", "缓解高峰拥堵，提高翻台效率。"));
        }
        if (!metrics.top_projects.isEmpty() && !"未规范项目".equals(metrics.top_projects.get(0).project_name)) {
            output.actions.add(action("P2", "复盘 " + metrics.top_projects.get(0).project_name + " 的成交路径，形成标准转化话术。", "咨询/医生", "7days", "放大高价值项目贡献。"));
        } else {
            output.actions.add(action("P2", "清洗处置项目名称和项目字典，避免异常名称进入经营日报。", "信息管理员", "7days", "提升项目分析可读性和复盘准确性。"));
        }
        while (output.actions.size() < 3) {
            output.actions.add(action("P2", "补充库存、采购和新增患者建档统计字段，完善经营分析口径。", "信息管理员", "14days", "提升AI经营日报的完整性和可解释性。"));
        }
        output.actions = output.actions.stream().limit(6).collect(Collectors.toList());
        return output;
    }

    private int calculateOperatingScore(DailyBusinessMetrics metrics) {
        double score = 55D;
        score += Math.min(metrics.today_income / 150D, 15D);
        score += Math.min(metrics.record_completion_rate / 8D, 12D);
        score += Math.min(metrics.today_treatments * 2D, 8D);
        score -= Math.min(metrics.cancellation_rate / 3D, 12D);
        if (metrics.today_net_income < 0) {
            score -= 10D;
        }
        if (metrics.month_net_change_rate > 10) {
            score += 5D;
        } else if (metrics.month_net_change_rate < -10) {
            score -= 5D;
        }
        return Math.max(0, Math.min(100, (int) Math.round(score)));
    }

    private String buildFallbackHeadline(DailyBusinessMetrics metrics, String trend) {
        if (metrics.today_net_income < 0) {
            return "当日接诊仍有产出，但现金流承压，需要优先修正收费与取消问题";
        }
        if ("up".equals(trend) && metrics.cancellation_rate < 15) {
            return "经营状态稳中向上，预约兑现和收费表现整体健康";
        }
        if (metrics.cancellation_rate >= 25) {
            return "预约量存在，但取消偏高，经营效率被明显拖累";
        }
        return "当日经营总体平稳，需继续提升病历闭环与高价值项目转化";
    }

    private String buildFallbackSummary(DailyBusinessMetrics metrics, String fallbackReason) {
        String summary = metrics.analysis_date + " 共预约 " + metrics.today_appointments + " 人次，完成病历 "
                + metrics.today_medical_records + " 份、治疗 " + metrics.today_treatments + " 例，收入 ¥"
                + formatMoney(metrics.today_income) + "、支出 ¥" + formatMoney(metrics.today_expense)
                + "、净现金流 ¥" + formatMoney(metrics.today_net_income) + "。预约取消率 "
                + formatPercent(metrics.cancellation_rate) + "，未来7日有效预约 " + metrics.future_7_day_appointments
                + " 人次。";
        if (StringUtils.hasText(fallbackReason)) {
            summary += " 本次因模型调用异常，已自动回退为规则分析。";
        } else if (!canUseOpenAi()) {
            summary += " 当前尚未启用 OpenAI，暂由系统生成规则分析。";
        }
        return summary;
    }

    private String buildManagementBrief(DailyBusinessMetrics metrics, String fallbackReason) {
        StringBuilder builder = new StringBuilder();
        builder.append("门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。");
        if (metrics.cancellation_rate >= 25) {
            builder.append(" 取消率已达到需要立即干预的水平。");
        }
        if (metrics.today_net_income < 0) {
            builder.append(" 当日现金流为负，应同步复盘支出与未收费项目。");
        }
        if (StringUtils.hasText(fallbackReason)) {
            builder.append(" 当前展示内容为规则分析，建议在配置 OpenAI Key 后启用深度分析。");
        }
        return builder.toString();
    }

    private RiskOutput risk(String title, String severity, String finding, String recommendation) {
        RiskOutput output = new RiskOutput();
        output.title = title;
        output.severity = severity;
        output.finding = finding;
        output.recommendation = recommendation;
        return output;
    }

    private OpportunityOutput opportunity(String title, String impact, String finding, String recommendation) {
        OpportunityOutput output = new OpportunityOutput();
        output.title = title;
        output.impact = impact;
        output.finding = finding;
        output.recommendation = recommendation;
        return output;
    }

    private ActionOutput action(String priority, String action, String owner, String due, String expectedResult) {
        ActionOutput output = new ActionOutput();
        output.priority = priority;
        output.action = action;
        output.owner = owner;
        output.due = due;
        output.expected_result = expectedResult;
        return output;
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
        } catch (JsonProcessingException exception) {
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

    private boolean hasDirtyProjectName(Treatment treatment) {
        return !isMeaningfulProjectName(extractProjectNameCandidate(treatment == null ? null : treatment.getAppointment_purpose()));
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

    private String simplifyException(Exception exception) {
        String message = exception.getMessage();
        if (!StringUtils.hasText(message)) {
            return exception.getClass().getSimpleName();
        }
        String compact = message.replaceAll("\\s+", " ").trim();
        return compact.length() <= 300 ? compact : compact.substring(0, 300);
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
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

    private String formatMoney(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatPercent(double value) {
        return String.format(Locale.US, "%.2f%%", value);
    }

    private static class OpenAiAnalysisResult {
        private String rawResponse;
        private BusinessAnalysisOutput output;
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
