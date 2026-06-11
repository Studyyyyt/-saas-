package com.example.springboot.service;

import com.example.springboot.entity.ConsultationQuery;
import com.example.springboot.entity.ConsultationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ConsultationDashboardService {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ConsultationRecordService consultationRecordService;

    public Map<String, Object> buildOverview(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);
        List<ConsultationRecord> previousRecords = fetchRecords(rangeWindow.previousStart, rangeWindow.previousEnd);

        OverviewStat current = buildOverviewStat(currentRecords, rangeWindow.currentStart, rangeWindow.currentEnd);
        OverviewStat previous = buildOverviewStat(previousRecords, rangeWindow.previousStart, rangeWindow.previousEnd);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", buildRangeMap(rangeWindow));
        data.put("consultationCount", buildMetricCard(current.consultationCount, previous.consultationCount));
        data.put("arrivalRate", buildMetricCard(current.arrivalRate, previous.arrivalRate));
        data.put("dealRate", buildMetricCard(current.dealRate, previous.dealRate));
        data.put("highIntentPendingCount", buildMetricCard(current.highIntentPendingCount, previous.highIntentPendingCount));
        data.put("summary", Map.of(
                "currentConsultationCount", current.consultationCount,
                "currentArrivedCount", current.arrivedCount,
                "currentDealCount", current.dealCount,
                "currentHighIntentPendingCount", current.highIntentPendingCount,
                "previousConsultationCount", previous.consultationCount,
                "previousArrivedCount", previous.arrivedCount,
                "previousDealCount", previous.dealCount,
                "previousHighIntentPendingCount", previous.highIntentPendingCount
        ));
        return data;
    }

    public Map<String, Object> buildFunnel(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);
        List<ConsultationRecord> previousRecords = fetchRecords(rangeWindow.previousStart, rangeWindow.previousEnd);

        OverviewStat current = buildOverviewStat(currentRecords, rangeWindow.currentStart, rangeWindow.currentEnd);
        OverviewStat previous = buildOverviewStat(previousRecords, rangeWindow.previousStart, rangeWindow.previousEnd);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", buildRangeMap(rangeWindow));
        data.put("current", buildFunnelMap(current));
        data.put("previous", buildFunnelMap(previous));
        return data;
    }

    public Map<String, Object> buildChannelAnalysis(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);
        List<ConsultationRecord> previousRecords = fetchRecords(rangeWindow.previousStart, rangeWindow.previousEnd);

        Map<String, ChannelProjectStat> currentStats = buildChannelStats(currentRecords, true);
        Map<String, ChannelProjectStat> previousStats = buildChannelStats(previousRecords, true);

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(currentStats.keySet());
        keys.addAll(previousStats.keySet());

        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : keys) {
            ChannelProjectStat current = currentStats.getOrDefault(key, new ChannelProjectStat(key));
            ChannelProjectStat previous = previousStats.getOrDefault(key, new ChannelProjectStat(key));
            list.add(buildChannelProjectRow(current, previous, true));
        }
        list.sort(Comparator.comparingInt((Map<String, Object> item) -> ((Number) item.get("consultation_count")).intValue()).reversed()
                .thenComparing(item -> String.valueOf(item.get("name"))));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", buildRangeMap(rangeWindow));
        data.put("list", list);
        return data;
    }

    public Map<String, Object> buildProjectAnalysis(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);
        List<ConsultationRecord> previousRecords = fetchRecords(rangeWindow.previousStart, rangeWindow.previousEnd);

        Map<String, ChannelProjectStat> currentStats = buildProjectStats(currentRecords);
        Map<String, ChannelProjectStat> previousStats = buildProjectStats(previousRecords);

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(currentStats.keySet());
        keys.addAll(previousStats.keySet());

        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : keys) {
            ChannelProjectStat current = currentStats.getOrDefault(key, new ChannelProjectStat(key));
            ChannelProjectStat previous = previousStats.getOrDefault(key, new ChannelProjectStat(key));
            list.add(buildChannelProjectRow(current, previous, false));
        }
        list.sort(Comparator.comparingInt((Map<String, Object> item) -> ((Number) item.get("consultation_count")).intValue()).reversed()
                .thenComparing(item -> String.valueOf(item.get("name"))));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", buildRangeMap(rangeWindow));
        data.put("list", list);
        return data;
    }

    public Map<String, Object> buildHourHeatmap(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);
        List<ConsultationRecord> previousRecords = fetchRecords(rangeWindow.previousStart, rangeWindow.previousEnd);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", buildRangeMap(rangeWindow));
        data.put("current", buildHeatmapGrid(currentRecords));
        data.put("previous", buildHeatmapGrid(previousRecords));
        return data;
    }

    public Map<String, Object> buildNursePerformance(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);
        List<ConsultationRecord> previousRecords = fetchRecords(rangeWindow.previousStart, rangeWindow.previousEnd);

        Map<String, NurseStat> currentStats = buildNurseStats(currentRecords);
        Map<String, NurseStat> previousStats = buildNurseStats(previousRecords);
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(currentStats.keySet());
        keys.addAll(previousStats.keySet());

        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : keys) {
            NurseStat current = currentStats.getOrDefault(key, new NurseStat(key, null, ""));
            NurseStat previous = previousStats.getOrDefault(key, new NurseStat(key, null, ""));
            list.add(buildNurseRow(current, previous));
        }
        list.sort(Comparator.comparingInt((Map<String, Object> item) -> ((Number) item.get("consultation_count")).intValue()).reversed()
                .thenComparing(item -> String.valueOf(item.get("created_by_name"))));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", buildRangeMap(rangeWindow));
        data.put("list", list);
        return data;
    }

    public Map<String, Object> buildReferralAnalysis(String startTime, String endTime, String rangePreset) {
        RangeWindow rangeWindow = resolveRange(startTime, endTime, rangePreset);
        List<ConsultationRecord> currentRecords = fetchRecords(rangeWindow.currentStart, rangeWindow.currentEnd);

        List<ConsultationRecord> referralRecords = currentRecords.stream()
                .filter(item -> item != null && "转介绍".equals(trim(item.getConsultation_channel())))
                .toList();
        List<ConsultationRecord> nonReferralRecords = currentRecords.stream()
                .filter(item -> item != null && !"转介绍".equals(trim(item.getConsultation_channel())))
                .toList();

        ReferralStat referralStat = buildReferralStat("转介绍", referralRecords);
        ReferralStat nonReferralStat = buildReferralStat("非转介绍", nonReferralRecords);

        Map<String, ReferralStat> detailStats = new LinkedHashMap<>();
        for (ConsultationRecord record : referralRecords) {
            String key = resolveReferralDetailKey(record);
            String label = resolveReferralDetailLabel(record);
            ReferralStat stat = detailStats.computeIfAbsent(key, ignored -> new ReferralStat(label));
            stat.accept(record);
        }

        List<Map<String, Object>> detailList = detailStats.values().stream()
                .sorted(Comparator.comparingInt(ReferralStat::getConsultationCount).reversed()
                        .thenComparing(ReferralStat::getName))
                .map(ReferralStat::toMap)
                .toList();

        List<Map<String, Object>> topReferrers = buildTopReferrerRows(referralRecords);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_consultation_count", currentRecords.size());
        summary.put("referral_consultation_count", referralStat.getConsultationCount());
        summary.put("referral_ratio", calcRate(referralStat.getConsultationCount(), currentRecords.size()));
        summary.put("referral_arrival_rate", referralStat.arrivalRate());
        summary.put("non_referral_arrival_rate", nonReferralStat.arrivalRate());
        summary.put("referral_deal_rate", referralStat.dealRate());
        summary.put("non_referral_deal_rate", nonReferralStat.dealRate());
        summary.put("referral_deal_amount", referralStat.getDealAmount());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("range", buildRangeMap(rangeWindow));
        result.put("summary", summary);
        result.put("typeComparison", List.of(referralStat.toMap(), nonReferralStat.toMap()));
        result.put("detailList", detailList);
        result.put("topReferrers", topReferrers);
        return result;
    }

    private List<ConsultationRecord> fetchRecords(LocalDateTime start, LocalDateTime end) {
        ConsultationQuery query = new ConsultationQuery();
        query.setStartTime(DATE_TIME_FORMATTER.format(start));
        query.setEndTime(DATE_TIME_FORMATTER.format(end));
        return consultationRecordService.search(query);
    }

    private OverviewStat buildOverviewStat(List<ConsultationRecord> records, LocalDateTime start, LocalDateTime end) {
        OverviewStat stat = new OverviewStat();
        stat.consultationCount = safeSize(records);
        stat.arrivedCount = (int) records.stream().filter(item -> item != null && item.getArrived_at() != null).count();
        stat.dealCount = (int) records.stream().filter(item -> item != null && item.getDeal_at() != null).count();
        stat.highIntentPendingCount = (int) records.stream()
                .filter(item -> item != null
                        && "高".equals(trim(item.getIntent_level()))
                        && "待跟进".equals(trim(item.getHandling_result())))
                .count();
        stat.arrivalRate = calcRate(stat.arrivedCount, stat.consultationCount);
        stat.dealRate = calcRate(stat.dealCount, stat.consultationCount);
        return stat;
    }

    private Map<String, Object> buildFunnelMap(OverviewStat stat) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("consultation_count", stat.consultationCount);
        map.put("arrived_count", stat.arrivedCount);
        map.put("deal_count", stat.dealCount);
        map.put("arrival_rate", round2(stat.arrivalRate));
        map.put("deal_rate", round2(stat.dealRate));
        return map;
    }

    private Map<String, ChannelProjectStat> buildChannelStats(List<ConsultationRecord> records, boolean withAvgTicket) {
        Map<String, ChannelProjectStat> result = new LinkedHashMap<>();
        for (ConsultationRecord record : safeRecords(records)) {
            String key = trim(record.getConsultation_channel());
            if (key.isEmpty()) {
                key = "未分类";
            }
            ChannelProjectStat stat = result.computeIfAbsent(key, ChannelProjectStat::new);
            stat.consultationCount++;
            if (record.getArrived_at() != null) {
                stat.arrivedCount++;
            }
            if (record.getDeal_at() != null) {
                stat.dealCount++;
            }
            if (withAvgTicket && record.getDeal_at() != null && record.getPatient_id() != null && record.getPatient_id() > 0) {
                stat.dealPatientIds.add(record.getPatient_id());
                stat.patientDealAmount.putIfAbsent(record.getPatient_id(), round2(record.getTotal_deal_amount() == null ? 0D : record.getTotal_deal_amount()));
            }
        }
        return result;
    }

    private Map<String, ChannelProjectStat> buildProjectStats(List<ConsultationRecord> records) {
        Map<String, ChannelProjectStat> result = new LinkedHashMap<>();
        for (ConsultationRecord record : safeRecords(records)) {
            String key = trim(record.getChief_project());
            if (key.isEmpty()) {
                key = "未分类";
            }
            ChannelProjectStat stat = result.computeIfAbsent(key, ChannelProjectStat::new);
            stat.consultationCount++;
            if (record.getArrived_at() != null) {
                stat.arrivedCount++;
            }
            if (record.getDeal_at() != null) {
                stat.dealCount++;
            }
        }
        return result;
    }

    private List<Map<String, Object>> buildHeatmapGrid(List<ConsultationRecord> records) {
        String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int[][] counts = new int[7][24];
        for (ConsultationRecord record : safeRecords(records)) {
            if (record.getConsultation_time() == null) {
                continue;
            }
            LocalDateTime localDateTime = LocalDateTime.ofInstant(record.getConsultation_time().toInstant(), ZONE_ID);
            int dayIndex = localDateTime.getDayOfWeek().getValue() - 1;
            int hour = localDateTime.getHour();
            counts[dayIndex][hour] += 1;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < weekdays.length; dayIndex++) {
            for (int hour = 0; hour < 24; hour++) {
                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("weekday", weekdays[dayIndex]);
                cell.put("weekday_index", dayIndex + 1);
                cell.put("hour", hour);
                cell.put("count", counts[dayIndex][hour]);
                list.add(cell);
            }
        }
        return list;
    }

    private Map<String, NurseStat> buildNurseStats(List<ConsultationRecord> records) {
        Map<String, NurseStat> result = new LinkedHashMap<>();
        for (ConsultationRecord record : safeRecords(records)) {
            Long createdBy = record.getCreated_by();
            String key = createdBy != null && createdBy > 0 ? "id:" + createdBy : "name:" + trim(record.getCreated_by_name());
            NurseStat stat = result.computeIfAbsent(key, ignored ->
                    new NurseStat(key, createdBy, trim(record.getCreated_by_name())));
            stat.consultationCount++;
            if (record.getArrived_at() != null) {
                stat.arrivedCount++;
            }
            if (record.getDeal_at() != null) {
                stat.dealCount++;
            }
        }
        return result;
    }

    private Map<String, Object> buildChannelProjectRow(ChannelProjectStat current,
                                                       ChannelProjectStat previous,
                                                       boolean withAvgTicket) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", current.name);
        row.put("consultation_count", current.consultationCount);
        row.put("consultation_count_previous", previous.consultationCount);
        row.put("consultation_count_change_rate", calcChangeRate(current.consultationCount, previous.consultationCount));
        row.put("arrival_rate", calcRate(current.arrivedCount, current.consultationCount));
        row.put("arrival_rate_previous", calcRate(previous.arrivedCount, previous.consultationCount));
        row.put("deal_rate", calcRate(current.dealCount, current.consultationCount));
        row.put("deal_rate_previous", calcRate(previous.dealCount, previous.consultationCount));
        if (withAvgTicket) {
            row.put("avg_ticket", current.avgTicket());
            row.put("avg_ticket_previous", previous.avgTicket());
        }
        return row;
    }

    private Map<String, Object> buildNurseRow(NurseStat current, NurseStat previous) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("created_by", current.createdBy);
        row.put("created_by_name", current.createdByName);
        row.put("consultation_count", current.consultationCount);
        row.put("consultation_count_previous", previous.consultationCount);
        row.put("consultation_count_change_rate", calcChangeRate(current.consultationCount, previous.consultationCount));
        row.put("arrival_rate", calcRate(current.arrivedCount, current.consultationCount));
        row.put("arrival_rate_previous", calcRate(previous.arrivedCount, previous.consultationCount));
        row.put("deal_rate", calcRate(current.dealCount, current.consultationCount));
        row.put("deal_rate_previous", calcRate(previous.dealCount, previous.consultationCount));
        return row;
    }

    private ReferralStat buildReferralStat(String name, List<ConsultationRecord> records) {
        ReferralStat stat = new ReferralStat(name);
        for (ConsultationRecord record : safeRecords(records)) {
            stat.accept(record);
        }
        return stat;
    }

    private List<Map<String, Object>> buildTopReferrerRows(List<ConsultationRecord> referralRecords) {
        Map<String, ReferralStat> statsByReferrer = new LinkedHashMap<>();
        for (ConsultationRecord record : safeRecords(referralRecords)) {
            String key = resolveReferrerKey(record);
            String name = resolveReferrerName(record);
            if (key.isEmpty() || name.isEmpty()) {
                continue;
            }
            ReferralStat stat = statsByReferrer.computeIfAbsent(key, ignored -> new ReferralStat(name));
            stat.accept(record);
        }
        return statsByReferrer.values().stream()
                .sorted(Comparator.comparingInt(ReferralStat::getConsultationCount).reversed()
                        .thenComparing(ReferralStat::getName))
                .limit(10)
                .map(stat -> {
                    Map<String, Object> row = stat.toMap();
                    row.put("referrer_name", stat.getName());
                    return row;
                })
                .toList();
    }

    private String resolveReferralDetailKey(ConsultationRecord record) {
        if (record == null) {
            return "unknown";
        }
        if ("patient".equals(trim(record.getReferrer_type()))) {
            return "patient";
        }
        if ("external".equals(trim(record.getReferrer_type()))) {
            String externalType = trim(record.getExternal_referrer_type());
            return externalType.isEmpty() ? "external" : "external:" + externalType;
        }
        return "unknown";
    }

    private String resolveReferralDetailLabel(ConsultationRecord record) {
        if (record == null) {
            return "未细分";
        }
        if ("patient".equals(trim(record.getReferrer_type()))) {
            return "现有客户介绍";
        }
        if ("external".equals(trim(record.getReferrer_type()))) {
            String externalType = trim(record.getExternal_referrer_type());
            return externalType.isEmpty() ? "外部介绍人" : "外部介绍人 - " + externalType;
        }
        return "未细分";
    }

    private String resolveReferrerKey(ConsultationRecord record) {
        if (record == null) {
            return "";
        }
        if (record.getReferrer_patient_id() != null && record.getReferrer_patient_id() > 0) {
            return "patient:" + record.getReferrer_patient_id();
        }
        if (StringUtils.hasText(record.getExternal_referrer_name())) {
            return "external:" + record.getExternal_referrer_name().trim();
        }
        return "";
    }

    private String resolveReferrerName(ConsultationRecord record) {
        if (record == null) {
            return "";
        }
        if (StringUtils.hasText(record.getReferrer_patient_name())) {
            return record.getReferrer_patient_name().trim();
        }
        if (StringUtils.hasText(record.getExternal_referrer_name())) {
            return record.getExternal_referrer_name().trim();
        }
        return "";
    }

    private Map<String, Object> buildMetricCard(double currentValue, double previousValue) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("current_value", round2(currentValue));
        metric.put("previous_value", round2(previousValue));
        metric.put("change_value", round2(currentValue - previousValue));
        metric.put("change_rate", calcChangeRate(currentValue, previousValue));
        metric.put("direction", resolveDirection(currentValue, previousValue));
        return metric;
    }

    private Map<String, Object> buildRangeMap(RangeWindow rangeWindow) {
        Map<String, Object> range = new LinkedHashMap<>();
        range.put("rangePreset", rangeWindow.rangePreset);
        range.put("currentStart", DATE_TIME_FORMATTER.format(rangeWindow.currentStart));
        range.put("currentEnd", DATE_TIME_FORMATTER.format(rangeWindow.currentEnd));
        range.put("previousStart", DATE_TIME_FORMATTER.format(rangeWindow.previousStart));
        range.put("previousEnd", DATE_TIME_FORMATTER.format(rangeWindow.previousEnd));
        return range;
    }

    private RangeWindow resolveRange(String startTime, String endTime, String rangePreset) {
        String normalizedPreset = normalizeRangePreset(rangePreset);
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        if ("custom".equals(normalizedPreset)) {
            LocalDateTime customStart = parseFlexibleDateTime(startTime, true, now.minusDays(6).with(LocalTime.MIN));
            LocalDateTime customEnd = parseFlexibleDateTime(endTime, false, now);
            if (customEnd.isBefore(customStart)) {
                throw new IllegalArgumentException("结束时间不能早于开始时间");
            }
            Duration duration = Duration.between(customStart, customEnd);
            return new RangeWindow(
                    customStart,
                    customEnd,
                    customStart.minus(duration).minusSeconds(1),
                    customEnd.minus(duration).minusSeconds(1),
                    normalizedPreset
            );
        }

        LocalDateTime currentStart;
        LocalDateTime previousStart;
        switch (normalizedPreset) {
            case "today":
                currentStart = LocalDate.now(ZONE_ID).atStartOfDay();
                previousStart = currentStart.minusDays(1);
                break;
            case "week":
                currentStart = LocalDate.now(ZONE_ID).with(DayOfWeek.MONDAY).atStartOfDay();
                previousStart = currentStart.minusWeeks(1);
                break;
            case "month":
                currentStart = LocalDate.now(ZONE_ID).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                previousStart = currentStart.minusMonths(1);
                break;
            case "quarter":
                currentStart = firstDayOfQuarter(LocalDate.now(ZONE_ID)).atStartOfDay();
                previousStart = currentStart.minusMonths(3);
                break;
            default:
                currentStart = LocalDate.now(ZONE_ID).with(DayOfWeek.MONDAY).atStartOfDay();
                previousStart = currentStart.minusWeeks(1);
                normalizedPreset = "week";
                break;
        }
        LocalDateTime currentEnd = now;
        Duration duration = Duration.between(currentStart, currentEnd);
        LocalDateTime previousEnd = previousStart.plus(duration);
        return new RangeWindow(currentStart, currentEnd, previousStart, previousEnd, normalizedPreset);
    }

    private LocalDate firstDayOfQuarter(LocalDate date) {
        int currentMonth = date.getMonthValue();
        int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), quarterStartMonth, 1);
    }

    private LocalDateTime parseFlexibleDateTime(String value, boolean startOfDay, LocalDateTime defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        String text = value.trim();
        try {
            if (text.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                return LocalDate.parse(text).atTime(startOfDay ? LocalTime.MIN : LocalTime.of(23, 59, 59));
            }
            if (text.matches("^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}$")) {
                return LocalDateTime.parse(text + ":00", DATE_TIME_FORMATTER);
            }
            if (text.matches("^\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}$")) {
                return LocalDateTime.parse(text.replace('T', ' '), DATE_TIME_FORMATTER);
            }
        } catch (Exception ignored) {
            // fall through
        }
        throw new IllegalArgumentException("时间格式应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
    }

    private String normalizeRangePreset(String rangePreset) {
        String preset = trim(rangePreset).toLowerCase(Locale.ROOT);
        if (preset.isEmpty()) {
            return "week";
        }
        if (List.of("today", "week", "month", "quarter", "custom").contains(preset)) {
            return preset;
        }
        return "week";
    }

    private Collection<ConsultationRecord> safeRecords(List<ConsultationRecord> records) {
        return records == null ? List.of() : records;
    }

    private int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private double calcRate(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return round2((numerator * 100D) / denominator);
    }

    private double calcChangeRate(double currentValue, double previousValue) {
        if (Math.abs(previousValue) <= 0.0001D) {
            return currentValue > 0 ? 100D : 0D;
        }
        return round2(((currentValue - previousValue) * 100D) / previousValue);
    }

    private String resolveDirection(double currentValue, double previousValue) {
        double diff = round2(currentValue - previousValue);
        if (Math.abs(diff) <= 0.0001D) {
            return "flat";
        }
        return diff > 0 ? "up" : "down";
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private static class RangeWindow {
        private final LocalDateTime currentStart;
        private final LocalDateTime currentEnd;
        private final LocalDateTime previousStart;
        private final LocalDateTime previousEnd;
        private final String rangePreset;

        private RangeWindow(LocalDateTime currentStart,
                            LocalDateTime currentEnd,
                            LocalDateTime previousStart,
                            LocalDateTime previousEnd,
                            String rangePreset) {
            this.currentStart = currentStart;
            this.currentEnd = currentEnd;
            this.previousStart = previousStart;
            this.previousEnd = previousEnd;
            this.rangePreset = rangePreset;
        }
    }

    private static class OverviewStat {
        private int consultationCount;
        private int arrivedCount;
        private int dealCount;
        private int highIntentPendingCount;
        private double arrivalRate;
        private double dealRate;
    }

    private static class ChannelProjectStat {
        private final String name;
        private int consultationCount;
        private int arrivedCount;
        private int dealCount;
        private final Set<Long> dealPatientIds = new LinkedHashSet<>();
        private final Map<Long, Double> patientDealAmount = new LinkedHashMap<>();

        private ChannelProjectStat(String name) {
            this.name = name;
        }

        private double avgTicket() {
            if (dealPatientIds.isEmpty()) {
                return 0D;
            }
            double total = 0D;
            for (Long patientId : dealPatientIds) {
                total += patientDealAmount.getOrDefault(patientId, 0D);
            }
            return Math.round(total * 100D / dealPatientIds.size()) / 100D;
        }
    }

    private static class NurseStat {
        private final String key;
        private final Long createdBy;
        private final String createdByName;
        private int consultationCount;
        private int arrivedCount;
        private int dealCount;

        private NurseStat(String key, Long createdBy, String createdByName) {
            this.key = key;
            this.createdBy = createdBy;
            this.createdByName = createdByName;
        }
    }

    private static class ReferralStat {
        private final String name;
        private int consultationCount;
        private int arrivedCount;
        private int dealCount;
        private final Set<Long> dealPatientIds = new LinkedHashSet<>();
        private final Map<Long, Double> patientDealAmount = new LinkedHashMap<>();

        private ReferralStat(String name) {
            this.name = name;
        }

        private void accept(ConsultationRecord record) {
            if (record == null) {
                return;
            }
            consultationCount++;
            if (record.getArrived_at() != null) {
                arrivedCount++;
            }
            if (record.getDeal_at() != null) {
                dealCount++;
            }
            if (record.getDeal_at() != null && record.getPatient_id() != null && record.getPatient_id() > 0) {
                dealPatientIds.add(record.getPatient_id());
                patientDealAmount.putIfAbsent(record.getPatient_id(), Math.round((record.getTotal_deal_amount() == null ? 0D : record.getTotal_deal_amount()) * 100D) / 100D);
            }
        }

        private String getName() {
            return name;
        }

        private int getConsultationCount() {
            return consultationCount;
        }

        private double arrivalRate() {
            return consultationCount <= 0 ? 0D : Math.round(arrivedCount * 10000D / consultationCount) / 100D;
        }

        private double dealRate() {
            return consultationCount <= 0 ? 0D : Math.round(dealCount * 10000D / consultationCount) / 100D;
        }

        private double getDealAmount() {
            double total = 0D;
            for (Long patientId : dealPatientIds) {
                total += patientDealAmount.getOrDefault(patientId, 0D);
            }
            return Math.round(total * 100D) / 100D;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("consultation_count", consultationCount);
            map.put("arrived_count", arrivedCount);
            map.put("deal_count", dealCount);
            map.put("arrival_rate", arrivalRate());
            map.put("deal_rate", dealRate());
            map.put("deal_amount", getDealAmount());
            return map;
        }
    }
}
