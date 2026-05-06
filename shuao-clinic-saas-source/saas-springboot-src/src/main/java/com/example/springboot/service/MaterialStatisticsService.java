package com.example.springboot.service;

import com.example.springboot.entity.Material;
import com.example.springboot.entity.MaterialCategory;
import com.example.springboot.entity.MaterialPurchase;
import com.example.springboot.entity.MaterialPurchaseItem;
import com.example.springboot.mapper.MaterialCategoryMapper;
import com.example.springboot.mapper.MaterialMapper;
import com.example.springboot.mapper.MaterialPurchaseItemMapper;
import com.example.springboot.mapper.MaterialPurchaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MaterialStatisticsService {

    private final MaterialPurchaseMapper materialPurchaseMapper;
    private final MaterialPurchaseItemMapper materialPurchaseItemMapper;
    private final MaterialMapper materialMapper;
    private final MaterialCategoryMapper materialCategoryMapper;

    @Autowired
    public MaterialStatisticsService(MaterialPurchaseMapper materialPurchaseMapper,
                                     MaterialPurchaseItemMapper materialPurchaseItemMapper,
                                     MaterialMapper materialMapper,
                                     MaterialCategoryMapper materialCategoryMapper) {
        this.materialPurchaseMapper = materialPurchaseMapper;
        this.materialPurchaseItemMapper = materialPurchaseItemMapper;
        this.materialMapper = materialMapper;
        this.materialCategoryMapper = materialCategoryMapper;
    }

    public Map<String, Object> overview(String rangePreset, String startDate, String endDate) {
        LocalDate[] range = resolveRange(rangePreset, startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];

        List<MaterialPurchase> activePurchases = materialPurchaseMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> MaterialPurchaseService.STATUS_ACTIVE.equals(item.getStatus()))
                .filter(item -> inRange(item.getPurchase_date(), start, end))
                .toList();

        Map<Long, List<MaterialPurchaseItem>> itemsByPurchaseId = new LinkedHashMap<>();
        for (MaterialPurchase purchase : activePurchases) {
            itemsByPurchaseId.put(purchase.getId(), materialPurchaseItemMapper.selectByPurchaseId(purchase.getId()));
        }

        List<Map<String, Object>> monthlyTotals = buildMonthlyTotals();
        List<Map<String, Object>> categoryDistribution = buildCategoryDistribution(itemsByPurchaseId);
        List<Map<String, Object>> supplierDistribution = buildSupplierDistribution(activePurchases);
        List<Material> lowStockList = buildLowStockList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rangePreset", normalizePreset(rangePreset));
        result.put("startDate", start == null ? "" : start.toString());
        result.put("endDate", end == null ? "" : end.toString());
        result.put("monthlyTotals", monthlyTotals);
        result.put("categoryDistribution", categoryDistribution);
        result.put("supplierDistribution", supplierDistribution);
        result.put("lowStockList", lowStockList);
        return result;
    }

    private List<Map<String, Object>> buildMonthlyTotals() {
        List<MaterialPurchase> purchases = materialPurchaseMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> MaterialPurchaseService.STATUS_ACTIVE.equals(item.getStatus()))
                .toList();
        YearMonth now = YearMonth.now();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = now.minusMonths(i);
            BigDecimal amount = purchases.stream()
                    .filter(item -> toYearMonth(item.getPurchase_date()).equals(month))
                    .map(MaterialPurchase::getTotal_amount)
                    .map(this::normalizeMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(chartRow("month", month.toString(), amount));
        }
        return rows;
    }

    private List<Map<String, Object>> buildCategoryDistribution(Map<Long, List<MaterialPurchaseItem>> itemsByPurchaseId) {
        Map<Long, Material> materialMap = materialMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(Material::getId, item -> item, (left, right) -> left));
        Map<Long, MaterialCategory> categoryMap = materialCategoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(MaterialCategory::getId, item -> item, (left, right) -> left));
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (List<MaterialPurchaseItem> items : itemsByPurchaseId.values()) {
            for (MaterialPurchaseItem item : items) {
                if (item == null || item.getMaterial_id() == null) {
                    continue;
                }
                Material material = materialMap.get(item.getMaterial_id());
                String rootCategoryName = resolveRootCategoryName(material == null ? null : material.getCategory_id(), categoryMap);
                totals.merge(rootCategoryName, normalizeMoney(item.getSubtotal()), BigDecimal::add);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> chartRow("name", entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("value")).reversed())
                .toList();
    }

    private List<Map<String, Object>> buildSupplierDistribution(List<MaterialPurchase> purchases) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (MaterialPurchase purchase : purchases) {
            String supplierName = purchase.getSupplier_name() == null || purchase.getSupplier_name().trim().isEmpty()
                    ? "未填写供应商"
                    : purchase.getSupplier_name().trim();
            totals.merge(supplierName, normalizeMoney(purchase.getTotal_amount()), BigDecimal::add);
        }
        return totals.entrySet().stream()
                .map(entry -> chartRow("name", entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("value")).reversed())
                .limit(10)
                .toList();
    }

    private List<Material> buildLowStockList() {
        return materialMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .map(this::withAlertGap)
                .filter(item -> item.getAlert_gap() != null && item.getAlert_gap() > 0)
                .sorted(Comparator.comparing(Material::getAlert_gap, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Material::getUpdated_at, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private LocalDate[] resolveRange(String preset, String startDate, String endDate) {
        String normalized = normalizePreset(preset);
        LocalDate today = LocalDate.now();
        return switch (normalized) {
            case "lastMonth" -> {
                YearMonth lastMonth = YearMonth.from(today).minusMonths(1);
                yield new LocalDate[]{lastMonth.atDay(1), lastMonth.atEndOfMonth()};
            }
            case "quarter" -> {
                int quarterStartMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                yield new LocalDate[]{LocalDate.of(today.getYear(), quarterStartMonth, 1), today};
            }
            case "year" -> new LocalDate[]{LocalDate.of(today.getYear(), 1, 1), today};
            case "custom" -> new LocalDate[]{parseDate(startDate), parseDate(endDate)};
            default -> new LocalDate[]{today.withDayOfMonth(1), today};
        };
    }

    private String normalizePreset(String preset) {
        String normalized = preset == null ? "" : preset.trim();
        return switch (normalized) {
            case "lastMonth", "quarter", "year", "custom" -> normalized;
            default -> "month";
        };
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException("日期格式应为yyyy-MM-dd");
        }
    }

    private boolean inRange(java.util.Date date, LocalDate start, LocalDate end) {
        if (date == null) {
            return start == null && end == null;
        }
        LocalDate current = toLocalDate(date);
        if (start != null && current.isBefore(start)) {
            return false;
        }
        return end == null || !current.isAfter(end);
    }

    private YearMonth toYearMonth(java.util.Date date) {
        if (date == null) {
            return YearMonth.now();
        }
        return YearMonth.from(toLocalDate(date));
    }

    private String resolveRootCategoryName(Long categoryId, Map<Long, MaterialCategory> categoryMap) {
        if (categoryId == null || categoryId <= 0) {
            return "未分类";
        }
        MaterialCategory category = categoryMap.get(categoryId);
        if (category == null) {
            return "未分类";
        }
        if (category.getParent_id() == null || category.getParent_id() <= 0) {
            return category.getName();
        }
        MaterialCategory parent = categoryMap.get(category.getParent_id());
        return parent == null ? category.getName() : parent.getName();
    }

    private Material withAlertGap(Material material) {
        int alert = material.getMin_stock_alert() == null ? 0 : material.getMin_stock_alert();
        int current = material.getCurrent_stock() == null ? 0 : material.getCurrent_stock();
        material.setAlert_gap(alert > 0 && current <= alert ? alert - current : 0);
        return material;
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

    private LocalDate toLocalDate(java.util.Date date) {
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
