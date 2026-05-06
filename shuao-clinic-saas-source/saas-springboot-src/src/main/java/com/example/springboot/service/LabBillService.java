package com.example.springboot.service;

import com.example.springboot.entity.LabBill;
import com.example.springboot.entity.LabBillConfirmRequest;
import com.example.springboot.entity.LabBillImportItemPayload;
import com.example.springboot.entity.LabBillItem;
import com.example.springboot.entity.LabBillResolutionRequest;
import com.example.springboot.entity.LabBillTemplate;
import com.example.springboot.entity.LabBillUnmatchedOrder;
import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.LabOrder;
import com.example.springboot.mapper.LabBillItemMapper;
import com.example.springboot.mapper.LabBillMapper;
import com.example.springboot.mapper.LabBillTemplateMapper;
import com.example.springboot.mapper.LabBillUnmatchedOrderMapper;
import com.example.springboot.mapper.LabFactoryMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class LabBillService {

    public static final String BILL_STATUS_PENDING = "待对账";
    public static final String BILL_STATUS_RECONCILING = "对账中";
    public static final String BILL_STATUS_COMPLETED = "已完成对账";

    public static final String MATCH_STATUS_FULL = "完全匹配";
    public static final String MATCH_STATUS_QUANTITY = "数量不符";
    public static final String MATCH_STATUS_AMOUNT = "金额不符";
    public static final String MATCH_STATUS_ONLY_IN_BILL = "仅账单有";

    public static final String RESOLUTION_PENDING = "待处理";
    public static final String RESOLUTION_DONE = "已处理";
    public static final String RESOLUTION_IGNORED = "已忽略";
    public static final String RESOLUTION_NOT_REQUIRED = "无需处理";

    private static final String BILL_UPLOAD_DIR = System.getProperty("user.home") + "/.local/uploads/lab-bills/";

    private final LabBillMapper labBillMapper;
    private final LabBillItemMapper labBillItemMapper;
    private final LabBillUnmatchedOrderMapper labBillUnmatchedOrderMapper;
    private final LabBillTemplateMapper labBillTemplateMapper;
    private final LabFactoryMapper labFactoryMapper;
    private final LabOrderService labOrderService;
    private final FinanceService financeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LabBillService(LabBillMapper labBillMapper,
                          LabBillItemMapper labBillItemMapper,
                          LabBillUnmatchedOrderMapper labBillUnmatchedOrderMapper,
                          LabBillTemplateMapper labBillTemplateMapper,
                          LabFactoryMapper labFactoryMapper,
                          LabOrderService labOrderService,
                          FinanceService financeService) {
        this.labBillMapper = labBillMapper;
        this.labBillItemMapper = labBillItemMapper;
        this.labBillUnmatchedOrderMapper = labBillUnmatchedOrderMapper;
        this.labBillTemplateMapper = labBillTemplateMapper;
        this.labFactoryMapper = labFactoryMapper;
        this.labOrderService = labOrderService;
        this.financeService = financeService;
    }

    public List<LabBill> searchBills(Long factoryId, String status, String billMonth) {
        String normalizedStatus = normalizeBillStatus(status, false);
        String normalizedMonth = normalizeBillMonthOrEmpty(billMonth);
        return labBillMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> factoryId == null || factoryId <= 0 || factoryId.equals(item.getFactory_id()))
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.getStatus()))
                .filter(item -> normalizedMonth.isEmpty() || normalizedMonth.equals(item.getBill_month()))
                .sorted(Comparator.comparing(LabBill::getImported_at, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(LabBill::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public LabBill selectById(Long id) {
        return id == null ? null : labBillMapper.selectById(id);
    }

    public Map<String, Object> getBillDetail(Long billId) {
        LabBill bill = requireExistingBill(billId);
        List<LabBillItem> items = labBillItemMapper.selectByBillId(billId);
        List<LabBillUnmatchedOrder> unmatchedOrders = labBillUnmatchedOrderMapper.selectByBillId(billId);
        Map<Long, LabOrder> orderMap = buildOrderMap(unmatchedOrders, items);

        List<LabBillItem> matchedItems = items.stream().filter(item -> MATCH_STATUS_FULL.equals(item.getMatch_status())).toList();
        List<LabBillItem> mismatchItems = items.stream()
                .filter(item -> MATCH_STATUS_QUANTITY.equals(item.getMatch_status()) || MATCH_STATUS_AMOUNT.equals(item.getMatch_status()))
                .toList();
        List<LabBillItem> onlyInBillItems = items.stream().filter(item -> MATCH_STATUS_ONLY_IN_BILL.equals(item.getMatch_status())).toList();

        List<Map<String, Object>> onlyInSystemOrders = unmatchedOrders.stream()
                .map(item -> buildUnmatchedOrderView(item, orderMap.get(item.getLab_order_id())))
                .toList();

        long pendingItemCount = items.stream()
                .filter(item -> !MATCH_STATUS_FULL.equals(item.getMatch_status()))
                .filter(item -> RESOLUTION_PENDING.equals(normalizeResolutionStatus(item.getResolution_status(), false, false)))
                .count();
        long pendingSystemCount = unmatchedOrders.stream()
                .filter(item -> RESOLUTION_PENDING.equals(normalizeResolutionStatus(item.getResolution_status(), false, false)))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bill", bill);
        result.put("matchedItems", matchedItems);
        result.put("mismatchItems", mismatchItems);
        result.put("onlyInBillItems", onlyInBillItems);
        result.put("onlyInSystemOrders", onlyInSystemOrders);
        result.put("allItems", items);
        result.put("allResolved", pendingItemCount + pendingSystemCount == 0);
        result.put("pendingIssueCount", pendingItemCount + pendingSystemCount);
        return result;
    }

    @Transactional
    public LabBill importBill(MultipartFile file,
                              Long factoryId,
                              String billMonth,
                              Long templateId,
                              Long importedBy,
                              String importedByName,
                              String parsedItemsJson) {
        LabFactory factory = requireExistingFactory(factoryId);
        String normalizedMonth = normalizeBillMonth(billMonth);
        LabBill existingBill = labBillMapper.selectByFactoryIdAndMonth(factoryId, normalizedMonth);
        if (existingBill != null) {
            throw new IllegalArgumentException("该加工厂该月份账单已存在");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传账单文件");
        }
        LabBillTemplate template = resolveTemplate(factoryId, templateId);
        List<LabBillImportItemPayload> parsedItems = parseImportItems(parsedItemsJson);
        if (parsedItems.isEmpty()) {
            throw new IllegalArgumentException("账单中没有可导入的数据");
        }

        String storedPath = storeBillFile(file, normalizedMonth, factory.getName());
        BigDecimal totalAmount = parsedItems.stream()
                .map(item -> normalizeMoney(item.getTotal_amount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        LabBill bill = new LabBill();
        bill.setFactory_id(factoryId);
        bill.setFactory_name(factory.getName());
        bill.setTemplate_id(template == null ? null : template.getId());
        bill.setBill_month(normalizedMonth);
        bill.setTotal_amount(totalAmount);
        bill.setBill_file_url(storedPath);
        bill.setStatus(BILL_STATUS_PENDING);
        bill.setMatched_count(0);
        bill.setMismatched_count(0);
        bill.setOnly_in_system_count(0);
        bill.setOnly_in_bill_count(0);
        bill.setImported_by(importedBy);
        bill.setImported_by_name(trimToNull(importedByName));
        bill.setImported_at(new Date());
        labBillMapper.insert(bill);

        List<LabBillItem> billItems = new ArrayList<>();
        for (LabBillImportItemPayload payload : parsedItems) {
            LabBillItem item = new LabBillItem();
            item.setBill_id(bill.getId());
            item.setRaw_row_number(payload.getRaw_row_number());
            item.setProduct_name(trimToNull(payload.getProduct_name()));
            item.setProduct_spec(trimToNull(payload.getProduct_spec()));
            item.setQuantity(normalizeQuantity(payload.getQuantity()));
            item.setUnit_price(normalizeMoney(payload.getUnit_price()));
            item.setTotal_amount(normalizeMoney(payload.getTotal_amount()));
            item.setDelivery_date(parseSqlDate(payload.getDelivery_date()));
            item.setPatient_name(trimToNull(payload.getPatient_name()));
            item.setMatch_status(MATCH_STATUS_ONLY_IN_BILL);
            item.setResolution_status(RESOLUTION_PENDING);
            item.setResolution_remark(null);
            item.setResolved_by(null);
            item.setResolved_by_name(null);
            item.setResolved_at(null);
            labBillItemMapper.insert(item);
            billItems.add(item);
        }

        autoMatchBill(bill, billItems);
        return labBillMapper.selectById(bill.getId());
    }

    @Transactional
    public LabBillItem updateBillItemResolution(Long itemId, LabBillResolutionRequest request) {
        LabBillItem item = requireExistingBillItem(itemId);
        String nextStatus = normalizeResolutionStatus(request == null ? null : request.getResolution_status(), true, false);
        item.setResolution_status(nextStatus);
        item.setResolution_remark(trimToNull(request == null ? null : request.getResolution_remark()));
        item.setResolved_by(request == null ? null : request.getResolved_by());
        item.setResolved_by_name(trimToNull(request == null ? null : request.getResolved_by_name()));
        item.setResolved_at(RESOLUTION_PENDING.equals(nextStatus) ? null : new Date());
        labBillItemMapper.update(item);
        return labBillItemMapper.selectById(itemId);
    }

    @Transactional
    public LabBillUnmatchedOrder updateUnmatchedOrderResolution(Long itemId, LabBillResolutionRequest request) {
        LabBillUnmatchedOrder item = requireExistingUnmatchedOrder(itemId);
        String nextStatus = normalizeResolutionStatus(request == null ? null : request.getResolution_status(), true, false);
        item.setResolution_status(nextStatus);
        item.setResolution_remark(trimToNull(request == null ? null : request.getResolution_remark()));
        item.setResolved_by(request == null ? null : request.getResolved_by());
        item.setResolved_by_name(trimToNull(request == null ? null : request.getResolved_by_name()));
        item.setResolved_at(RESOLUTION_PENDING.equals(nextStatus) ? null : new Date());
        labBillUnmatchedOrderMapper.update(item);
        return labBillUnmatchedOrderMapper.selectById(itemId);
    }

    @Transactional
    public LabBill confirmBill(Long billId, LabBillConfirmRequest request) {
        LabBill bill = requireExistingBill(billId);
        if (BILL_STATUS_COMPLETED.equals(bill.getStatus())) {
            throw new IllegalArgumentException("该账单已完成对账");
        }

        List<LabBillItem> items = labBillItemMapper.selectByBillId(billId);
        List<LabBillUnmatchedOrder> unmatchedOrders = labBillUnmatchedOrderMapper.selectByBillId(billId);

        boolean hasPendingItems = items.stream()
                .filter(item -> !MATCH_STATUS_FULL.equals(item.getMatch_status()))
                .anyMatch(item -> RESOLUTION_PENDING.equals(normalizeResolutionStatus(item.getResolution_status(), false, false)));
        boolean hasPendingUnmatched = unmatchedOrders.stream()
                .anyMatch(item -> RESOLUTION_PENDING.equals(normalizeResolutionStatus(item.getResolution_status(), false, false)));
        if (hasPendingItems || hasPendingUnmatched) {
            throw new IllegalArgumentException("请先处理完所有异常项目后再完成对账");
        }

        List<Long> matchedOrderIds = items.stream()
                .map(LabBillItem::getMatched_lab_order_id)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        labOrderService.markOrdersReconciled(matchedOrderIds);

        String billRemark = bill.getFactory_name() + " " + bill.getBill_month() + "月度账单";
        financeService.recordExpense(null, null, bill.getTotal_amount(), "义齿加工", billRemark, "lab_bill", String.valueOf(bill.getId()));

        bill.setStatus(BILL_STATUS_COMPLETED);
        bill.setConfirmed_by(request == null ? null : request.getConfirmed_by());
        bill.setConfirmed_by_name(trimToNull(request == null ? null : request.getConfirmed_by_name()));
        bill.setConfirmed_at(new Date());
        labBillMapper.update(bill);
        return labBillMapper.selectById(billId);
    }

    public ResponseEntity<Resource> getBillFile(Long billId) {
        LabBill bill = requireExistingBill(billId);
        if (!StringUtils.hasText(bill.getBill_file_url())) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(bill.getBill_file_url());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    private void autoMatchBill(LabBill bill, List<LabBillItem> items) {
        List<LabOrder> candidates = labOrderService.findReconciliationCandidates(bill.getFactory_id(), bill.getBill_month());
        Map<Long, LabOrder> candidateMap = new HashMap<>();
        for (LabOrder candidate : candidates) {
            candidateMap.put(candidate.getId(), candidate);
        }
        Set<Long> usedOrderIds = new LinkedHashSet<>();

        for (LabBillItem item : items) {
            List<LabOrder> matchedCandidates = candidates.stream()
                    .filter(order -> !usedOrderIds.contains(order.getId()))
                    .filter(order -> matchesBillItem(item, order))
                    .toList();

            item.setMatched_lab_order_id(null);
            item.setResolution_remark(null);
            item.setResolved_at(null);
            item.setResolved_by(null);
            item.setResolved_by_name(null);

            if (matchedCandidates.size() == 1) {
                LabOrder matchedOrder = matchedCandidates.get(0);
                item.setMatched_lab_order_id(matchedOrder.getId());
                usedOrderIds.add(matchedOrder.getId());
                if (normalizeQuantity(item.getQuantity()) != normalizeQuantity(matchedOrder.getQuantity())) {
                    item.setMatch_status(MATCH_STATUS_QUANTITY);
                    item.setResolution_status(RESOLUTION_PENDING);
                } else if (normalizeMoney(item.getTotal_amount()).compareTo(normalizeMoney(matchedOrder.getTotal_amount())) != 0) {
                    item.setMatch_status(MATCH_STATUS_AMOUNT);
                    item.setResolution_status(RESOLUTION_PENDING);
                } else {
                    item.setMatch_status(MATCH_STATUS_FULL);
                    item.setResolution_status(RESOLUTION_NOT_REQUIRED);
                }
            } else {
                item.setMatch_status(MATCH_STATUS_ONLY_IN_BILL);
                item.setResolution_status(RESOLUTION_PENDING);
                if (matchedCandidates.size() > 1) {
                    item.setResolution_remark("匹配到多条系统订单，请人工核查");
                }
            }
            labBillItemMapper.update(item);
        }

        labBillUnmatchedOrderMapper.deleteByBillId(bill.getId());
        for (LabOrder candidate : candidates) {
            if (candidate == null || usedOrderIds.contains(candidate.getId())) {
                continue;
            }
            LabBillUnmatchedOrder unmatchedOrder = new LabBillUnmatchedOrder();
            unmatchedOrder.setBill_id(bill.getId());
            unmatchedOrder.setLab_order_id(candidate.getId());
            unmatchedOrder.setResolution_status(RESOLUTION_PENDING);
            unmatchedOrder.setResolution_remark(null);
            unmatchedOrder.setResolved_by(null);
            unmatchedOrder.setResolved_by_name(null);
            unmatchedOrder.setResolved_at(null);
            labBillUnmatchedOrderMapper.insert(unmatchedOrder);
        }

        List<LabBillItem> updatedItems = labBillItemMapper.selectByBillId(bill.getId());
        List<LabBillUnmatchedOrder> unmatchedOrders = labBillUnmatchedOrderMapper.selectByBillId(bill.getId());
        bill.setStatus(BILL_STATUS_RECONCILING);
        bill.setMatched_count((int) updatedItems.stream().filter(row -> MATCH_STATUS_FULL.equals(row.getMatch_status())).count());
        bill.setMismatched_count((int) updatedItems.stream()
                .filter(row -> MATCH_STATUS_QUANTITY.equals(row.getMatch_status()) || MATCH_STATUS_AMOUNT.equals(row.getMatch_status()))
                .count());
        bill.setOnly_in_bill_count((int) updatedItems.stream().filter(row -> MATCH_STATUS_ONLY_IN_BILL.equals(row.getMatch_status())).count());
        bill.setOnly_in_system_count(unmatchedOrders.size());
        labBillMapper.update(bill);
    }

    private boolean matchesBillItem(LabBillItem item, LabOrder order) {
        if (item == null || order == null) {
            return false;
        }
        if (!normalizeText(item.getProduct_name()).equalsIgnoreCase(normalizeText(order.getProduct_name()))) {
            return false;
        }
        String itemPatientName = normalizeText(item.getPatient_name());
        return itemPatientName.isEmpty() || itemPatientName.equalsIgnoreCase(normalizeText(order.getPatient_name()));
    }

    private Map<Long, LabOrder> buildOrderMap(List<LabBillUnmatchedOrder> unmatchedOrders, List<LabBillItem> items) {
        Set<Long> orderIds = new LinkedHashSet<>();
        for (LabBillUnmatchedOrder item : unmatchedOrders) {
            if (item != null && item.getLab_order_id() != null && item.getLab_order_id() > 0) {
                orderIds.add(item.getLab_order_id());
            }
        }
        for (LabBillItem item : items) {
            if (item != null && item.getMatched_lab_order_id() != null && item.getMatched_lab_order_id() > 0) {
                orderIds.add(item.getMatched_lab_order_id());
            }
        }
        Map<Long, LabOrder> result = new HashMap<>();
        for (Long orderId : orderIds) {
            LabOrder order = labOrderService.selectById(orderId);
            if (order != null) {
                result.put(orderId, order);
            }
        }
        return result;
    }

    private Map<String, Object> buildUnmatchedOrderView(LabBillUnmatchedOrder item, LabOrder order) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", item.getId());
        view.put("bill_id", item.getBill_id());
        view.put("lab_order_id", item.getLab_order_id());
        view.put("resolution_status", item.getResolution_status());
        view.put("resolution_remark", item.getResolution_remark());
        view.put("resolved_by", item.getResolved_by());
        view.put("resolved_by_name", item.getResolved_by_name());
        view.put("resolved_at", item.getResolved_at());
        if (order != null) {
            view.put("order", order);
        }
        return view;
    }

    private LabFactory requireExistingFactory(Long factoryId) {
        if (factoryId == null || factoryId <= 0) {
            throw new IllegalArgumentException("加工厂不能为空");
        }
        LabFactory factory = labFactoryMapper.selectById(factoryId);
        if (factory == null) {
            throw new IllegalArgumentException("加工厂不存在");
        }
        return factory;
    }

    private LabBill requireExistingBill(Long billId) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("账单ID不能为空");
        }
        LabBill bill = labBillMapper.selectById(billId);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        return bill;
    }

    private LabBillItem requireExistingBillItem(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("账单条目ID不能为空");
        }
        LabBillItem item = labBillItemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("账单条目不存在");
        }
        return item;
    }

    private LabBillUnmatchedOrder requireExistingUnmatchedOrder(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("仅系统有记录ID不能为空");
        }
        LabBillUnmatchedOrder item = labBillUnmatchedOrderMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("仅系统有记录不存在");
        }
        return item;
    }

    private LabBillTemplate resolveTemplate(Long factoryId, Long templateId) {
        if (templateId == null || templateId <= 0) {
            return null;
        }
        LabBillTemplate template = labBillTemplateMapper.selectById(templateId);
        if (template == null || !factoryId.equals(template.getFactory_id())) {
            throw new IllegalArgumentException("账单模板不存在");
        }
        return template;
    }

    private List<LabBillImportItemPayload> parseImportItems(String parsedItemsJson) {
        if (!StringUtils.hasText(parsedItemsJson)) {
            throw new IllegalArgumentException("缺少解析后的账单数据");
        }
        try {
            List<LabBillImportItemPayload> rows = objectMapper.readValue(parsedItemsJson, new TypeReference<List<LabBillImportItemPayload>>() {});
            return rows == null ? List.of() : rows.stream().filter(Objects::nonNull).toList();
        } catch (Exception error) {
            throw new IllegalArgumentException("账单数据解析失败");
        }
    }

    private String storeBillFile(MultipartFile file, String billMonth, String factoryName) {
        try {
            Path baseDir = Paths.get(BILL_UPLOAD_DIR, billMonth);
            Files.createDirectories(baseDir);
            String originalName = normalizeText(file.getOriginalFilename());
            String suffix = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                suffix = originalName.substring(dotIndex);
            }
            String safeFactoryName = normalizeText(factoryName).replaceAll("[^0-9A-Za-z\\u4e00-\\u9fa5_-]", "_");
            String filename = safeFactoryName + "-" + UUID.randomUUID().toString().replace("-", "") + suffix;
            Path target = baseDir.resolve(filename).normalize();
            file.transferTo(target);
            return target.toString();
        } catch (Exception error) {
            throw new IllegalArgumentException("保存账单文件失败: " + error.getMessage());
        }
    }

    private String normalizeBillStatus(String status, boolean applyDefault) {
        String normalized = normalizeText(status);
        if (normalized.isEmpty() && applyDefault) {
            return BILL_STATUS_PENDING;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        if (!BILL_STATUS_PENDING.equals(normalized)
                && !BILL_STATUS_RECONCILING.equals(normalized)
                && !BILL_STATUS_COMPLETED.equals(normalized)) {
            throw new IllegalArgumentException("账单状态不合法");
        }
        return normalized;
    }

    private String normalizeBillMonth(String billMonth) {
        String normalized = normalizeText(billMonth);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("账单月份不能为空");
        }
        try {
            return YearMonth.parse(normalized).toString();
        } catch (Exception error) {
            throw new IllegalArgumentException("账单月份格式应为yyyy-MM");
        }
    }

    private String normalizeBillMonthOrEmpty(String billMonth) {
        String normalized = normalizeText(billMonth);
        return normalized.isEmpty() ? "" : normalizeBillMonth(normalized);
    }

    private String normalizeResolutionStatus(String status, boolean applyDefault, boolean allowNotRequired) {
        String normalized = normalizeText(status);
        if (normalized.isEmpty() && applyDefault) {
            return RESOLUTION_PENDING;
        }
        if (normalized.isEmpty()) {
            return RESOLUTION_PENDING;
        }
        boolean valid = RESOLUTION_PENDING.equals(normalized)
                || RESOLUTION_DONE.equals(normalized)
                || RESOLUTION_IGNORED.equals(normalized)
                || (allowNotRequired && RESOLUTION_NOT_REQUIRED.equals(normalized));
        if (!valid) {
            throw new IllegalArgumentException("处理状态不合法");
        }
        return normalized;
    }

    private java.sql.Date parseSqlDate(String value) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (Exception error) {
            throw new IllegalArgumentException("账单送货日期格式应为yyyy-MM-dd");
        }
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private int normalizeQuantity(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
