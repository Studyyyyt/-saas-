package com.example.springboot.service;

import com.example.springboot.entity.LabBill;
import com.example.springboot.entity.LabBillItem;
import com.example.springboot.mapper.LabBillItemMapper;
import com.example.springboot.mapper.LabBillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class LabStatisticsService {

    private final LabBillMapper labBillMapper;
    private final LabBillItemMapper labBillItemMapper;

    @Autowired
    public LabStatisticsService(LabBillMapper labBillMapper, LabBillItemMapper labBillItemMapper) {
        this.labBillMapper = labBillMapper;
        this.labBillItemMapper = labBillItemMapper;
    }

    public Map<String, Object> buildOverview(String rangePreset, String startDate, String endDate) {
        YearMonth[] window = resolveRange(rangePreset, startDate, endDate);
        YearMonth from = window[0];
        YearMonth to = window[1];

        List<LabBill> completedBills = labBillMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> LabBillService.BILL_STATUS_COMPLETED.equals(item.getStatus()))
                .filter(item -> inMonthRange(item.getBill_month(), from, to))
                .toList();

        Map<Long, List<LabBillItem>> itemsByBillId = new LinkedHashMap<>();
        for (LabBill bill : completedBills) {
            itemsByBillId.put(bill.getId(), labBillItemMapper.selectByBillId(bill.getId()));
        }

        List<Map<String, Object>> monthlyTotals = buildMonthlyTotals(completedBills);
        List<Map<String, Object>> factoryDistribution = buildFactoryDistribution(completedBills);
        List<Map<String, Object>> productDistribution = buildProductDistribution(itemsByBillId);
        List<Map<String, Object>> factoryComparison = buildFactoryComparison(completedBills, itemsByBillId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rangePreset", normalizePreset(rangePreset));
        result.put("startMonth", from.toString());
        result.put("endMonth", to.toString());
        result.put("monthlyTotals", monthlyTotals);
        result.put("factoryDistribution", factoryDistribution);
        result.put("productDistribution", productDistribution);
        result.put("factoryComparison", factoryComparison);
        return result;
    }

    private List<Map<String, Object>> buildMonthlyTotals(List<LabBill> completedBills) {
        YearMonth currentMonth = YearMonth.now();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            BigDecimal amount = completedBills.stream()
                    .filter(item -> month.toString().equals(item.getBill_month()))
                    .map(item -> normalizeMoney(item.getTotal_amount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(chartRow("month", month.toString(), amount));
        }
        return rows;
    }

    private List<Map<String, Object>> buildFactoryDistribution(List<LabBill> completedBills) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (LabBill bill : completedBills) {
            totals.merge(normalizeText(bill.getFactory_name()), normalizeMoney(bill.getTotal_amount()), BigDecimal::add);
        }
        return totals.entrySet().stream()
                .map(entry -> chartRow("name", entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing((Map<String, Object> item) -> (BigDecimal) item.get("value")).reversed())
                .toList();
    }

    private List<Map<String, Object>> buildProductDistribution(Map<Long, List<LabBillItem>> itemsByBillId) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (List<LabBillItem> items : itemsByBillId.values()) {
            for (LabBillItem item : items) {
                if (item == null) {
                    continue;
                }
                String key = normalizeText(item.getProduct_name());
                if (key.isEmpty()) {
                    key = "未命名产品";
                }
                totals.merge(key, normalizeMoney(item.getTotal_amount()), BigDecimal::add);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> chartRow("name", entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing((Map<String, Object> item) -> (BigDecimal) item.get("value")).reversed())
                .toList();
    }

    private List<Map<String, Object>> buildFactoryComparison(List<LabBill> completedBills,
                                                             Map<Long, List<LabBillItem>> itemsByBillId) {
        Map<String, Map<String, Object>> rows = new LinkedHashMap<>();
        for (LabBill bill : completedBills) {
            String key = normalizeText(bill.getFactory_name());
            Map<String, Object> row = rows.computeIfAbsent(key, ignored -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("factory_name", key);
                created.put("order_count", 0);
                created.put("total_amount", BigDecimal.ZERO);
                created.put("unit_price_total", BigDecimal.ZERO);
                created.put("unit_price_count", 0);
                created.put("repair_rate", null);
                return created;
            });

            List<LabBillItem> items = itemsByBillId.getOrDefault(bill.getId(), List.of());
            int orderCount = (int) items.stream().filter(Objects::nonNull).count();
            row.put("order_count", (Integer) row.get("order_count") + orderCount);
            row.put("total_amount", ((BigDecimal) row.get("total_amount")).add(normalizeMoney(bill.getTotal_amount())));

            BigDecimal unitPriceTotal = (BigDecimal) row.get("unit_price_total");
            int unitPriceCount = (Integer) row.get("unit_price_count");
            for (LabBillItem item : items) {
                if (item == null) {
                    continue;
                }
                unitPriceTotal = unitPriceTotal.add(normalizeMoney(item.getUnit_price()));
                unitPriceCount++;
            }
            row.put("unit_price_total", unitPriceTotal);
            row.put("unit_price_count", unitPriceCount);
        }

        return rows.values().stream()
                .map(row -> {
                    BigDecimal totalAmount = normalizeMoney((BigDecimal) row.get("total_amount"));
                    int orderCount = (Integer) row.get("order_count");
                    BigDecimal unitPriceTotal = normalizeMoney((BigDecimal) row.get("unit_price_total"));
                    int unitPriceCount = (Integer) row.get("unit_price_count");
                    Map<String, Object> output = new LinkedHashMap<>();
                    output.put("factory_name", row.get("factory_name"));
                    output.put("order_count", orderCount);
                    output.put("total_amount", totalAmount);
                    output.put("average_unit_price", unitPriceCount <= 0 ? BigDecimal.ZERO : unitPriceTotal.divide(BigDecimal.valueOf(unitPriceCount), 2, RoundingMode.HALF_UP));
                    output.put("repair_rate", null);
                    return output;
                })
                .sorted(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("total_amount")).reversed())
                .toList();
    }

    private YearMonth[] resolveRange(String rangePreset, String startDate, String endDate) {
        YearMonth currentMonth = YearMonth.now();
        String preset = normalizePreset(rangePreset);
        return switch (preset) {
            case "lastMonth" -> new YearMonth[]{currentMonth.minusMonths(1), currentMonth.minusMonths(1)};
            case "quarter" -> {
                int quarterStartMonth = ((currentMonth.getMonthValue() - 1) / 3) * 3 + 1;
                YearMonth start = YearMonth.of(currentMonth.getYear(), quarterStartMonth);
                yield new YearMonth[]{start, currentMonth};
            }
            case "year" -> new YearMonth[]{YearMonth.of(currentMonth.getYear(), 1), currentMonth};
            case "custom" -> new YearMonth[]{parseCustomMonth(startDate, currentMonth), parseCustomMonth(endDate, currentMonth)};
            default -> new YearMonth[]{currentMonth, currentMonth};
        };
    }

    private String normalizePreset(String rangePreset) {
        String normalized = normalizeText(rangePreset);
        return switch (normalized) {
            case "lastMonth", "quarter", "year", "custom" -> normalized;
            default -> "month";
        };
    }

    private YearMonth parseCustomMonth(String value, YearMonth fallback) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            return fallback;
        }
        try {
            return YearMonth.from(LocalDate.parse(normalized));
        } catch (Exception ignored) {
            try {
                return YearMonth.parse(normalized);
            } catch (Exception ignoredAgain) {
                return fallback;
            }
        }
    }

    private boolean inMonthRange(String billMonth, YearMonth from, YearMonth to) {
        if (billMonth == null || billMonth.trim().isEmpty()) {
            return false;
        }
        try {
            YearMonth current = YearMonth.parse(billMonth.trim());
            return !current.isBefore(from) && !current.isAfter(to);
        } catch (Exception error) {
            return false;
        }
    }

    private Map<String, Object> chartRow(String keyName, String label, BigDecimal value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(keyName, label);
        row.put("name", label);
        row.put("value", normalizeMoney(value));
        return row;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
