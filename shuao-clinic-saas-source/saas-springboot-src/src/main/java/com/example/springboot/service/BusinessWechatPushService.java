package com.example.springboot.service;

import com.example.springboot.entity.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessWechatPushService {

    private final AccountService accountService;
    private final WechatService wechatService;
    private final WechatOAuthService wechatOAuthService;

    public BusinessWechatPushService(AccountService accountService,
                                     WechatService wechatService,
                                     WechatOAuthService wechatOAuthService) {
        this.accountService = accountService;
        this.wechatService = wechatService;
        this.wechatOAuthService = wechatOAuthService;
    }

    public void pushDailyReportToAdmins(Map<String, Object> reportView) {
        if (reportView == null) {
            return;
        }
        String periodLabel = String.valueOf(reportView.getOrDefault("analysis_date", "-"));
        pushReportToAdmins("日报", periodLabel, reportView);
    }

    public void pushPeriodicReportToAdmins(String reportTypeLabel, Map<String, Object> reportView) {
        if (reportView == null) {
            return;
        }
        String periodLabel = String.valueOf(reportView.getOrDefault("period_label", "-"));
        pushReportToAdmins(reportTypeLabel, periodLabel, reportView);
    }

    public void pushAlertSummaryToAdmins(List<Map<String, Object>> alerts, String alertDate) {
        if (alerts == null || alerts.isEmpty()) {
            return;
        }
        List<Account> adminAccounts = accountService.findAdminAccountsWithWechatBinding();
        if (adminAccounts.isEmpty()) {
            System.out.println("[BUSINESS_ALERT_PUSH_SKIP] no admin wechat bindings");
            return;
        }
        String topLevel = String.valueOf(alerts.get(0).getOrDefault("alert_level", "MEDIUM"));
        String summary = buildBossAlertSummary(alerts, alertDate);
        for (Account account : adminAccounts) {
            wechatService.sendAdminBusinessAlertNotification(
                    account,
                    "经营异常波动告警",
                    topLevel,
                    summary,
                    wechatOAuthService.buildAdminReportUrl((long) account.getId(), "alerts")
            );
        }
    }

    private void pushReportToAdmins(String reportTypeLabel, String periodLabel, Map<String, Object> reportView) {
        List<Account> adminAccounts = accountService.findAdminAccountsWithWechatBinding();
        if (adminAccounts.isEmpty()) {
            System.out.println("[BUSINESS_REPORT_PUSH_SKIP] no admin wechat bindings, reportType=" + reportTypeLabel);
            return;
        }
        String headline = buildBossHeadline(reportTypeLabel, reportView);
        String summary = buildBossSummary(reportTypeLabel, reportView);
        Integer operatingScore = null;
        Object scoreValue = reportView.get("operating_score");
        if (scoreValue instanceof Number) {
            operatingScore = ((Number) scoreValue).intValue();
        }
        for (Account account : adminAccounts) {
            String focus = "日报".equals(reportTypeLabel) ? "daily" : ("周报".equals(reportTypeLabel) ? "weekly" : "monthly");
            wechatService.sendAdminBusinessReportNotification(
                    account,
                    reportTypeLabel,
                    periodLabel,
                    headline,
                    summary,
                    operatingScore,
                    wechatOAuthService.buildAdminReportUrl((long) account.getId(), focus)
            );
        }
    }

    private String buildBossHeadline(String reportTypeLabel, Map<String, Object> reportView) {
        String trend = String.valueOf(reportView.getOrDefault("trend", ""));
        Integer score = readInt(reportView.get("operating_score"));
        Map<String, Object> metrics = readMap(reportView.get("metrics"));
        String trendLabel = "up".equals(trend) ? "向上" : ("down".equals(trend) ? "承压" : "平稳");

        if ("日报".equals(reportTypeLabel)) {
            double income = readDouble(metrics.get("today_income"));
            double netIncome = readDouble(metrics.get("today_net_income"));
            int appointments = readInt(metrics.get("today_appointments"), 0);
            if (netIncome < 0) {
                return "当日现金流承压，先盯收费闭环";
            }
            if ("up".equals(trend) && income > 0) {
                return "当日经营向上，收入与到诊表现健康";
            }
            if (appointments <= 2) {
                return "当日客流偏少，先看预约来源与到诊";
            }
            return "当日经营" + trendLabel + "，重点看收入与到诊";
        }

        double netIncome = readDouble(metrics.get("net_income"));
        double netChangeRate = readDouble(metrics.get("net_income_change_rate"));
        if (netChangeRate <= -15) {
            return reportTypeLabel + "净收入下滑，先做原因复盘";
        }
        if ("up".equals(trend) && netIncome > 0) {
            return reportTypeLabel + "经营向上，可继续放大优势项目";
        }
        if (score != null && score >= 75) {
            return reportTypeLabel + "总体健康，重点守住兑现率";
        }
        return reportTypeLabel + "经营" + trendLabel + "，先抓关键短板";
    }

    private String buildBossSummary(String reportTypeLabel, Map<String, Object> reportView) {
        Map<String, Object> metrics = readMap(reportView.get("metrics"));
        String trend = String.valueOf(reportView.getOrDefault("trend", ""));

        if ("日报".equals(reportTypeLabel)) {
            double income = readDouble(metrics.get("today_income"));
            double netIncome = readDouble(metrics.get("today_net_income"));
            int appointments = readInt(metrics.get("today_appointments"), 0);
            double cancelRate = readDouble(metrics.get("cancellation_rate"));
            if (cancelRate >= 20) {
                return "收入¥" + formatMoney(income) + "，取消率" + formatPercent(cancelRate) + "，先稳住到诊。";
            }
            if (netIncome < 0) {
                return "净现金流¥" + formatMoney(netIncome) + "，优先查未收费与异常支出。";
            }
            return "预约" + appointments + "人，收入¥" + formatMoney(income) + "，建议继续盯高值项目。";
        }

        double netIncome = readDouble(metrics.get("net_income"));
        double netChangeRate = readDouble(metrics.get("net_income_change_rate"));
        int totalAppointments = readInt(metrics.get("total_appointments"), 0);
        if ("down".equals(trend) || netChangeRate < 0) {
            return "净收入¥" + formatMoney(netIncome) + "，较上期" + formatPercent(netChangeRate) + "，先复盘预约与客单价。";
        }
        return "预约" + totalAppointments + "人次，净收入¥" + formatMoney(netIncome) + "，建议放大有效打法。";
    }

    private Integer readInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private int readInt(Object value, int fallback) {
        Integer parsed = readInt(value);
        return parsed == null ? fallback : parsed;
    }

    private double readDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0D;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }

    private String formatMoney(double value) {
        return String.format("%.0f", value);
    }

    private String formatPercent(double value) {
        String prefix = value > 0 ? "+" : "";
        return prefix + String.format("%.0f%%", value);
    }

    private String buildBossAlertSummary(List<Map<String, Object>> alerts, String alertDate) {
        if (alerts == null || alerts.isEmpty()) {
            return alertDate + " 未发现明显异常。";
        }
        Map<String, Object> first = alerts.get(0);
        String title = String.valueOf(first.getOrDefault("alert_title", "经营波动"));
        String metricName = String.valueOf(first.getOrDefault("metric_name", ""));
        double changeRate = readDouble(first.get("change_rate"));
        if ("today_income".equals(metricName)) {
            return alertDate + " 收入明显走弱，较基线" + formatPercent(changeRate) + "，建议优先看收费与到诊。";
        }
        if ("today_appointments".equals(metricName)) {
            return alertDate + " 预约量明显回落，较基线" + formatPercent(changeRate) + "，建议先查渠道与确认。";
        }
        if ("cancellation_rate".equals(metricName)) {
            return alertDate + " 取消率突然抬头，建议马上确认未来3日预约。";
        }
        if ("record_completion_rate".equals(metricName)) {
            return alertDate + " 病历完成率偏低，建议当天补齐留痕。";
        }
        return alertDate + " 发现" + alerts.size() + "条异常，重点：" + title + "。";
    }
}
