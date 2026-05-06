package com.example.springboot.service;

import com.example.springboot.entity.BusinessAlertLog;
import com.example.springboot.mapper.BusinessAlertLogMapper;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BusinessAlertService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private final BusinessAlertLogMapper alertLogMapper;
    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessWechatPushService businessWechatPushService;

    public BusinessAlertService(BusinessAlertLogMapper alertLogMapper,
                                BusinessDailyAnalysisService businessDailyAnalysisService,
                                BusinessWechatPushService businessWechatPushService) {
        this.alertLogMapper = alertLogMapper;
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessWechatPushService = businessWechatPushService;
    }

    public List<Map<String, Object>> runDailyAlertScan(LocalDate analysisDate, String triggerType) {
        LocalDate targetDate = analysisDate == null ? LocalDate.now(DEFAULT_ZONE).minusDays(1) : analysisDate;
        BusinessDailyAnalysisService.DailyBusinessMetrics today = businessDailyAnalysisService.buildDailyMetricsForDate(targetDate);

        List<BusinessDailyAnalysisService.DailyBusinessMetrics> previousDays = new ArrayList<>();
        for (int i = 7; i >= 1; i--) {
            previousDays.add(businessDailyAnalysisService.buildDailyMetricsForDate(targetDate.minusDays(i)));
        }

        double avgIncome = average(previousDays.stream().mapToDouble(item -> item.today_income).toArray());
        double avgAppointments = average(previousDays.stream().mapToDouble(item -> item.today_appointments).toArray());
        double avgCancelRate = average(previousDays.stream().mapToDouble(item -> item.cancellation_rate).toArray());
        double avgRecordRate = average(previousDays.stream().mapToDouble(item -> item.record_completion_rate).toArray());

        List<BusinessAlertLog> alerts = new ArrayList<>();
        if (avgIncome >= 300 && today.today_income <= avgIncome * 0.6D) {
            alerts.add(buildAlert(targetDate, "INCOME_DROP", "HIGH", "收入显著下滑",
                    "today_income", today.today_income, avgIncome,
                    calculateRate(today.today_income, avgIncome),
                    "当日收入较近7日均值明显下降，请核查预约兑现、收费闭环和支出异常。", triggerType));
        }
        if (avgAppointments >= 4 && today.today_appointments <= avgAppointments * 0.5D) {
            alerts.add(buildAlert(targetDate, "APPOINTMENT_DROP", "MEDIUM", "预约量显著下滑",
                    "today_appointments", today.today_appointments, avgAppointments,
                    calculateRate(today.today_appointments, avgAppointments),
                    "请立即检查渠道流量、前台确认和医生排班变化。", triggerType));
        }
        if (today.today_appointments >= 3 && today.cancellation_rate >= avgCancelRate + 15D && today.cancellation_rate >= 20D) {
            alerts.add(buildAlert(targetDate, "CANCEL_RATE_SPIKE", "HIGH", "取消率异常升高",
                    "cancellation_rate", today.cancellation_rate, avgCancelRate,
                    calculateRate(today.cancellation_rate, avgCancelRate),
                    "优先回溯取消原因，并对未来3日预约患者做二次确认。", triggerType));
        }
        if (today.today_appointments >= 3 && today.record_completion_rate <= Math.max(45D, avgRecordRate * 0.7D)) {
            alerts.add(buildAlert(targetDate, "RECORD_COMPLETION_DROP", "MEDIUM", "病历完成率偏低",
                    "record_completion_rate", today.record_completion_rate, avgRecordRate,
                    calculateRate(today.record_completion_rate, avgRecordRate),
                    "要求接诊当日补齐病历，避免诊疗记录断档。", triggerType));
        }

        for (BusinessAlertLog alert : alerts) {
            alertLogMapper.insert(alert);
        }

        List<Map<String, Object>> view = alerts.stream().map(this::buildView).toList();
        businessWechatPushService.pushAlertSummaryToAdmins(view, targetDate.toString());
        return view;
    }

    public List<Map<String, Object>> getRecentAlerts(Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 100);
        return alertLogMapper.selectRecent(safeLimit).stream().map(this::buildView).toList();
    }

    private BusinessAlertLog buildAlert(LocalDate targetDate,
                                        String code,
                                        String level,
                                        String title,
                                        String metricName,
                                        double currentValue,
                                        double baselineValue,
                                        double changeRate,
                                        String suggestedAction,
                                        String triggerType) {
        BusinessAlertLog alert = new BusinessAlertLog();
        alert.setAlert_date(java.util.Date.from(targetDate.atStartOfDay(DEFAULT_ZONE).toInstant()));
        alert.setAlert_code(code);
        alert.setAlert_level(level);
        alert.setAlert_title(title);
        alert.setMetric_name(metricName);
        alert.setCurrent_value(round2(currentValue));
        alert.setBaseline_value(round2(baselineValue));
        alert.setChange_rate(round2(changeRate));
        alert.setAlert_message(title + "：当前值 " + format(currentValue) + "，近7日均值 " + format(baselineValue) + "。");
        alert.setSuggested_action(suggestedAction);
        alert.setSource_type("RULE_BASED");
        alert.setTrigger_type(triggerType == null ? "MANUAL" : triggerType.toUpperCase(Locale.ROOT));
        return alert;
    }

    private Map<String, Object> buildView(BusinessAlertLog alert) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", alert.getId());
        view.put("alert_date", formatDate(alert.getAlert_date()));
        view.put("alert_code", alert.getAlert_code());
        view.put("alert_level", alert.getAlert_level());
        view.put("alert_title", alert.getAlert_title());
        view.put("alert_message", alert.getAlert_message());
        view.put("metric_name", alert.getMetric_name());
        view.put("current_value", alert.getCurrent_value());
        view.put("baseline_value", alert.getBaseline_value());
        view.put("change_rate", alert.getChange_rate());
        view.put("suggested_action", alert.getSuggested_action());
        view.put("source_type", alert.getSource_type());
        view.put("trigger_type", alert.getTrigger_type());
        view.put("created_at", formatDateTime(alert.getCreated_at()));
        return view;
    }

    private double average(double[] values) {
        if (values == null || values.length == 0) {
            return 0D;
        }
        double sum = 0D;
        for (double value : values) {
            sum += value;
        }
        return round2(sum / values.length);
    }

    private double calculateRate(double currentValue, double baselineValue) {
        if (Math.abs(baselineValue) < 0.0001D) {
            return currentValue > 0 ? 100D : 0D;
        }
        return round2((currentValue - baselineValue) * 100D / Math.abs(baselineValue));
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatDate(java.util.Date value) {
        if (value == null) return "";
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDate().toString();
    }

    private String formatDateTime(java.util.Date value) {
        if (value == null) return "";
        return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE).toLocalDateTime().toString().replace('T', ' ');
    }
}
