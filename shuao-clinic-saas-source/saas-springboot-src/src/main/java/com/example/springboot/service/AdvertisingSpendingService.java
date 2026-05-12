package com.example.springboot.service;

import com.example.springboot.entity.AdvertisingSpending;
import com.example.springboot.entity.ConsultationQuery;
import com.example.springboot.entity.ConsultationRecord;
import com.example.springboot.entity.Finance;
import com.example.springboot.mapper.AdvertisingSpendingMapper;
import com.example.springboot.util.MarketingChannelCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AdvertisingSpendingService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AdvertisingSpendingMapper advertisingSpendingMapper;
    private final FinanceService financeService;
    private final ConsultationRecordService consultationRecordService;

    @Autowired
    public AdvertisingSpendingService(AdvertisingSpendingMapper advertisingSpendingMapper,
                                      FinanceService financeService,
                                      ConsultationRecordService consultationRecordService) {
        this.advertisingSpendingMapper = advertisingSpendingMapper;
        this.financeService = financeService;
        this.consultationRecordService = consultationRecordService;
    }

    public List<AdvertisingSpending> search(String platform,
                                            String keyword,
                                            String startDate,
                                            String endDate,
                                            Long createdBy) {
        String normalizedPlatform = trim(platform);
        String normalizedKeyword = trim(keyword).toLowerCase(Locale.ROOT);
        LocalDate rangeStart = parseDateOrDefault(startDate, null);
        LocalDate rangeEnd = parseDateOrDefault(endDate, null);
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        return advertisingSpendingMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> normalizedPlatform.isEmpty() || normalizedPlatform.equals(trim(item.getPlatform())))
                .filter(item -> createdBy == null || createdBy <= 0 || Objects.equals(createdBy, item.getCreated_by()))
                .filter(item -> normalizedKeyword.isEmpty() || containsKeyword(item, normalizedKeyword))
                .filter(item -> overlapsRange(item.getStart_date(), item.getEnd_date(), rangeStart, rangeEnd))
                .sorted(Comparator.comparing(AdvertisingSpending::getStart_date, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdvertisingSpending::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public AdvertisingSpending selectById(Long id) {
        return id == null || id <= 0 ? null : advertisingSpendingMapper.selectById(id);
    }

    public AdvertisingSpending add(AdvertisingSpending spending) {
        AdvertisingSpending normalized = normalizeForSave(spending, true, null);
        advertisingSpendingMapper.insert(normalized);
        syncFinance(normalized);
        advertisingSpendingMapper.update(normalized);
        return advertisingSpendingMapper.selectById(normalized.getId());
    }

    public AdvertisingSpending edit(AdvertisingSpending spending) {
        if (spending == null || spending.getId() == null || spending.getId() <= 0) {
            throw new IllegalArgumentException("广告投放记录ID不能为空");
        }
        AdvertisingSpending existing = advertisingSpendingMapper.selectById(spending.getId());
        if (existing == null) {
            throw new IllegalArgumentException("广告投放记录不存在");
        }
        AdvertisingSpending normalized = normalizeForSave(spending, false, existing);
        normalized.setId(existing.getId());
        normalized.setFinance_record_id(existing.getFinance_record_id());
        advertisingSpendingMapper.update(normalized);
        syncFinance(normalized);
        advertisingSpendingMapper.update(normalized);
        return advertisingSpendingMapper.selectById(existing.getId());
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("广告投放记录ID不能为空");
        }
        AdvertisingSpending existing = advertisingSpendingMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("广告投放记录不存在");
        }
        if (existing.getFinance_record_id() != null && existing.getFinance_record_id() > 0) {
            financeService.deleteFinance(existing.getFinance_record_id().intValue());
        }
        advertisingSpendingMapper.deleteById(id);
    }

    public Map<String, Object> buildOverview(String startDate, String endDate) {
        LocalDate rangeStart = parseDateOrDefault(startDate, LocalDate.now(ZONE_ID).withDayOfMonth(1));
        LocalDate rangeEnd = parseDateOrDefault(endDate, LocalDate.now(ZONE_ID));
        if (rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        List<AdvertisingSpending> scopedRecords = search(null, null, rangeStart.toString(), rangeEnd.toString(), null);

        Map<String, BigDecimal> spendByPlatform = new LinkedHashMap<>();
        Map<String, BigDecimal> trendByMonth = new LinkedHashMap<>();
        BigDecimal totalSpend = BigDecimal.ZERO;
        Map<String, LocalDate> platformStart = new LinkedHashMap<>();
        Map<String, LocalDate> platformEnd = new LinkedHashMap<>();
        for (AdvertisingSpending record : scopedRecords) {
            String platform = trim(record.getPlatform());
            BigDecimal amount = normalizeMoney(record.getAmount());
            spendByPlatform.merge(platform, amount, BigDecimal::add);
            totalSpend = totalSpend.add(amount);
            String monthKey = record.getStart_date() == null
                    ? rangeStart.toString().substring(0, 7)
                    : YearMonth.from(toLocalDate(record.getStart_date())).toString();
            trendByMonth.merge(monthKey, amount, BigDecimal::add);
            LocalDate itemStart = toLocalDate(record.getStart_date());
            LocalDate itemEnd = toLocalDate(record.getEnd_date()).plusDays(7);
            platformStart.merge(platform, itemStart, (left, right) -> left == null ? right : (right == null ? left : left.isAfter(right) ? right : left));
            platformEnd.merge(platform, itemEnd, (left, right) -> left == null ? right : (right == null ? left : left.isBefore(right) ? right : left));
        }

        List<Map<String, Object>> trend = trendByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("month", entry.getKey());
                    item.put("amount", roundMoney(entry.getValue()));
                    return item;
                })
                .toList();

        List<Map<String, Object>> platformShare = new ArrayList<>();
        List<Map<String, Object>> platformRoi = new ArrayList<>();
        List<ConsultationRecord> consultationPool = fetchConsultationPool(platformStart, platformEnd);
        for (Map.Entry<String, BigDecimal> entry : spendByPlatform.entrySet()) {
            String platform = entry.getKey();
            BigDecimal spend = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            BigDecimal revenue = BigDecimal.ZERO;
            int consultationCount = 0;
            int arrivedCount = 0;
            Set<Long> dealPatientIds = new LinkedHashSet<>();
            LocalDate platformWindowStart = platformStart.get(platform);
            LocalDate platformWindowEnd = platformEnd.get(platform);
            for (ConsultationRecord record : consultationPool) {
                if (record == null || !platform.equals(trim(record.getConsultation_channel()))) {
                    continue;
                }
                LocalDateTime consultationTime = toLocalDateTime(record.getConsultation_time());
                if (consultationTime == null || platformWindowStart == null || platformWindowEnd == null) {
                    continue;
                }
                if (consultationTime.toLocalDate().isBefore(platformWindowStart) || consultationTime.toLocalDate().isAfter(platformWindowEnd)) {
                    continue;
                }
                consultationCount++;
                if (record.getArrived_at() != null) {
                    arrivedCount++;
                }
                if (record.getDeal_at() != null && record.getPatient_id() != null && record.getPatient_id() > 0 && dealPatientIds.add(record.getPatient_id())) {
                    revenue = revenue.add(BigDecimal.valueOf(round2(record.getTotal_deal_amount() == null ? 0D : record.getTotal_deal_amount())));
                }
            }
            int dealCount = dealPatientIds.size();
            BigDecimal roiRatio = spend.compareTo(BigDecimal.ZERO) <= 0
                    ? BigDecimal.ZERO
                    : revenue.divide(spend, 4, RoundingMode.HALF_UP);
            platformShare.add(Map.of(
                    "platform", platform,
                    "amount", roundMoney(spend),
                    "share_percent", totalSpend.compareTo(BigDecimal.ZERO) <= 0 ? 0D : round2(spend.multiply(BigDecimal.valueOf(100D)).divide(totalSpend, 4, RoundingMode.HALF_UP).doubleValue())
            ));
            Map<String, Object> roiRow = new LinkedHashMap<>();
            roiRow.put("platform", platform);
            roiRow.put("spend_amount", roundMoney(spend));
            roiRow.put("consultation_count", consultationCount);
            roiRow.put("arrived_count", arrivedCount);
            roiRow.put("deal_count", dealCount);
            roiRow.put("deal_amount", roundMoney(revenue));
            roiRow.put("roi_ratio", round2(roiRatio.doubleValue()));
            roiRow.put("roi_percent", round2(roiRatio.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100D)).doubleValue()));
            platformRoi.add(roiRow);
        }
        platformShare.sort(Comparator.comparing(item -> -Double.parseDouble(String.valueOf(item.get("amount")))));
        platformRoi.sort(Comparator.comparing(item -> -Double.parseDouble(String.valueOf(item.get("spend_amount")))));

        // 汇总跨平台的总计数据
        int totalConsultationCount = platformRoi.stream().mapToInt(r -> (int) r.getOrDefault("consultation_count", 0)).sum();
        int totalArrivedCount = platformRoi.stream().mapToInt(r -> (int) r.getOrDefault("arrived_count", 0)).sum();
        int totalDealCount = platformRoi.stream().mapToInt(r -> (int) r.getOrDefault("deal_count", 0)).sum();
        BigDecimal totalRevenue = platformRoi.stream()
                .map(r -> BigDecimal.valueOf(((Number) r.getOrDefault("deal_amount", 0D)).doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRoiRatio = totalSpend.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(totalSpend, 4, RoundingMode.HALF_UP);

        List<Map<String, Object>> funnel = List.of(
                Map.of("name", "咨询", "value", totalConsultationCount),
                Map.of("name", "到店", "value", totalArrivedCount),
                Map.of("name", "成交", "value", totalDealCount)
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("start_date", rangeStart.toString());
        result.put("end_date", rangeEnd.toString());
        result.put("total_spend_amount", roundMoney(totalSpend));
        result.put("record_count", scopedRecords.size());
        result.put("total_roi_ratio", round2(totalRoiRatio.doubleValue()));
        result.put("total_consultation_count", totalConsultationCount);
        result.put("total_arrived_count", totalArrivedCount);
        result.put("total_deal_count", totalDealCount);
        result.put("total_deal_amount", roundMoney(totalRevenue));
        result.put("trend", trend);
        result.put("platform_share", platformShare);
        result.put("platform_roi", platformRoi);
        result.put("funnel", funnel);
        return result;
    }

    private AdvertisingSpending normalizeForSave(AdvertisingSpending spending, boolean creating, AdvertisingSpending existing) {
        if (spending == null) {
            throw new IllegalArgumentException("广告投放信息不能为空");
        }
        AdvertisingSpending normalized = new AdvertisingSpending();
        normalized.setId(spending.getId());
        normalized.setPlatform(validateOption(trim(spending.getPlatform()), MarketingChannelCatalog.ADVERTISING_PLATFORM_OPTIONS, "投放平台不合法"));
        normalized.setCampaign_name(trimToNull(spending.getCampaign_name()));
        if (spending.getStart_date() == null || spending.getEnd_date() == null) {
            throw new IllegalArgumentException("投放开始和结束日期不能为空");
        }
        LocalDate startDate = toLocalDate(spending.getStart_date());
        LocalDate endDate = toLocalDate(spending.getEnd_date());
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        if (!YearMonth.from(startDate).equals(YearMonth.from(endDate))) {
            throw new IllegalArgumentException("跨月投放请拆分成多条录入");
        }
        normalized.setStart_date(java.sql.Date.valueOf(startDate));
        normalized.setEnd_date(java.sql.Date.valueOf(endDate));
        BigDecimal amount = normalizeMoney(spending.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("投放金额必须大于0");
        }
        normalized.setAmount(amount);
        normalized.setTarget_project(trimToNull(spending.getTarget_project()));
        normalized.setTarget_audience(trimToNull(spending.getTarget_audience()));
        normalized.setRemark(trimToNull(spending.getRemark()));
        if (creating) {
            normalized.setCreated_by(normalizePositiveId(spending.getCreated_by()));
            normalized.setCreated_by_name(trimToNull(spending.getCreated_by_name()));
        } else {
            normalized.setCreated_by(normalizePositiveId(spending.getCreated_by()) == null && existing != null ? existing.getCreated_by() : normalizePositiveId(spending.getCreated_by()));
            normalized.setCreated_by_name(StringUtils.hasText(spending.getCreated_by_name()) ? spending.getCreated_by_name().trim() : (existing == null ? null : existing.getCreated_by_name()));
        }
        return normalized;
    }

    private void syncFinance(AdvertisingSpending spending) {
        if (spending == null || spending.getId() == null || spending.getId() <= 0) {
            return;
        }
        String financeRemark = buildFinanceRemark(spending);
        if (spending.getFinance_record_id() != null && spending.getFinance_record_id() > 0) {
            List<Finance> finances = financeService.getFinanceByid(spending.getFinance_record_id());
            if (finances != null && !finances.isEmpty() && finances.get(0) != null) {
                Finance finance = finances.get(0);
                finance.setName("广告投放");
                finance.setAmount(round2(spending.getAmount().doubleValue()));
                finance.setDate(toLocalDate(spending.getStart_date()).toString());
                finance.setType("支出");
                finance.setBiz_type("advertising_spending");
                finance.setRemark(financeRemark);
                financeService.editFinance(finance);
                return;
            }
        }
        Long financeId = financeService.recordExpense(
                null,
                null,
                normalizeMoney(spending.getAmount()),
                "广告投放",
                financeRemark,
                "advertising_spending",
                String.valueOf(spending.getId())
        );
        spending.setFinance_record_id(financeId);
        List<Finance> finances = financeService.getFinanceByid(financeId);
        if (finances != null && !finances.isEmpty() && finances.get(0) != null) {
            Finance finance = finances.get(0);
            finance.setDate(toLocalDate(spending.getStart_date()).toString());
            financeService.editFinance(finance);
        }
    }

    private String buildFinanceRemark(AdvertisingSpending spending) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(spending.getPlatform())) {
            parts.add("平台：" + spending.getPlatform().trim());
        }
        if (StringUtils.hasText(spending.getCampaign_name())) {
            parts.add("活动：" + spending.getCampaign_name().trim());
        }
        if (StringUtils.hasText(spending.getRemark())) {
            parts.add("备注：" + spending.getRemark().trim());
        }
        return String.join("；", parts);
    }

    private boolean containsKeyword(AdvertisingSpending item, String keyword) {
        return trim(item.getCampaign_name()).toLowerCase(Locale.ROOT).contains(keyword)
                || trim(item.getPlatform()).toLowerCase(Locale.ROOT).contains(keyword)
                || trim(item.getTarget_project()).toLowerCase(Locale.ROOT).contains(keyword)
                || trim(item.getTarget_audience()).toLowerCase(Locale.ROOT).contains(keyword)
                || trim(item.getRemark()).toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean overlapsRange(java.util.Date start, java.util.Date end, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate itemStart = toLocalDate(start);
        LocalDate itemEnd = toLocalDate(end);
        if (itemStart == null || itemEnd == null) {
            return rangeStart == null && rangeEnd == null;
        }
        if (rangeStart != null && itemEnd.isBefore(rangeStart)) {
            return false;
        }
        return rangeEnd == null || !itemStart.isAfter(rangeEnd);
    }

    private List<ConsultationRecord> fetchConsultationPool(Map<String, LocalDate> platformStart,
                                                           Map<String, LocalDate> platformEnd) {
        if (platformStart.isEmpty()) {
            return List.of();
        }
        LocalDate minStart = platformStart.values().stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        LocalDate maxEnd = platformEnd.values().stream().filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        if (minStart == null || maxEnd == null) {
            return List.of();
        }
        ConsultationQuery query = new ConsultationQuery();
        query.setStartTime(DATE_TIME_FORMATTER.format(minStart.atStartOfDay()));
        query.setEndTime(DATE_TIME_FORMATTER.format(maxEnd.atTime(LocalTime.MAX.withNano(0))));
        return consultationRecordService.search(query);
    }

    private String validateOption(String value, List<String> options, String errorMessage) {
        if (!StringUtils.hasText(value) || options == null || !options.contains(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultValue) {
        String normalized = trim(value);
        if (normalized.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (Exception exception) {
            throw new IllegalArgumentException("日期格式不合法");
        }
    }

    private LocalDate toLocalDate(java.util.Date value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return value.toInstant().atZone(ZONE_ID).toLocalDate();
    }

    private LocalDateTime toLocalDateTime(java.util.Date value) {
        if (value == null) {
            return null;
        }
        return value.toInstant().atZone(ZONE_ID).toLocalDateTime();
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String normalized = trim(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private double roundMoney(BigDecimal value) {
        return value == null ? 0D : value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
