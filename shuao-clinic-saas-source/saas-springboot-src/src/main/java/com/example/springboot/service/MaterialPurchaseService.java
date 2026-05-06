package com.example.springboot.service;

import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Material;
import com.example.springboot.entity.MaterialPurchase;
import com.example.springboot.entity.MaterialPurchaseItem;
import com.example.springboot.entity.MaterialPurchaseVoidRequest;
import com.example.springboot.mapper.MaterialMapper;
import com.example.springboot.mapper.MaterialPurchaseItemMapper;
import com.example.springboot.mapper.MaterialPurchaseMapper;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialPurchaseService {

    public static final String STATUS_ACTIVE = "有效";
    public static final String STATUS_VOIDED = "已作废";

    private static final String INVOICE_UPLOAD_DIR = System.getProperty("user.home") + "/.local/uploads/material-invoices/";

    private final MaterialPurchaseMapper materialPurchaseMapper;
    private final MaterialPurchaseItemMapper materialPurchaseItemMapper;
    private final MaterialMapper materialMapper;
    private final MaterialService materialService;
    private final FinanceService financeService;

    @Autowired
    public MaterialPurchaseService(MaterialPurchaseMapper materialPurchaseMapper,
                                   MaterialPurchaseItemMapper materialPurchaseItemMapper,
                                   MaterialMapper materialMapper,
                                   MaterialService materialService,
                                   FinanceService financeService) {
        this.materialPurchaseMapper = materialPurchaseMapper;
        this.materialPurchaseItemMapper = materialPurchaseItemMapper;
        this.materialMapper = materialMapper;
        this.materialService = materialService;
        this.financeService = financeService;
    }

    public List<MaterialPurchase> search(String supplierKeyword,
                                         String startDate,
                                         String endDate,
                                         String status) {
        String normalizedKeyword = supplierKeyword == null ? "" : supplierKeyword.trim().toLowerCase(Locale.ROOT);
        String normalizedStatus = normalizeStatus(status, false);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        return materialPurchaseMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.getStatus()))
                .filter(item -> normalizedKeyword.isEmpty() || normalizeText(item.getSupplier_name()).toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .filter(item -> inDateRange(item.getPurchase_date(), start, end))
                .sorted(Comparator.comparing(MaterialPurchase::getPurchase_date, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MaterialPurchase::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .peek(this::attachItems)
                .toList();
    }

    public MaterialPurchase selectById(Long id) {
        MaterialPurchase purchase = id == null ? null : materialPurchaseMapper.selectById(id);
        if (purchase != null) {
            attachItems(purchase);
        }
        return purchase;
    }

    @Transactional
    public MaterialPurchase add(MaterialPurchase purchase) {
        validatePurchase(purchase, true);
        List<MaterialPurchaseItem> items = normalizeItems(purchase.getItems());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("请至少添加一条采购明细");
        }

        BigDecimal totalAmount = sumItems(items);
        purchase.setSupplier_name(trimToNull(purchase.getSupplier_name()));
        purchase.setPayment_method(normalizePaymentMethod(purchase.getPayment_method()));
        purchase.setRemark(trimToNull(purchase.getRemark()));
        purchase.setStatus(STATUS_ACTIVE);
        purchase.setTotal_amount(totalAmount);
        materialPurchaseMapper.insert(purchase);

        saveItemsAndIncreaseStock(purchase.getId(), items);
        Long financeId = financeService.recordExpense(
                null,
                null,
                totalAmount,
                "耗材采购",
                "采购自 " + normalizeText(purchase.getSupplier_name()),
                "material_purchase",
                String.valueOf(purchase.getId())
        );
        purchase.setFinance_record_id(financeId);
        materialPurchaseMapper.update(purchase);
        syncFinanceDate(purchase);
        return selectById(purchase.getId());
    }

    @Transactional
    public MaterialPurchase edit(MaterialPurchase purchase) {
        if (purchase == null || purchase.getId() == null || purchase.getId() <= 0) {
            throw new IllegalArgumentException("采购单ID不能为空");
        }
        MaterialPurchase existing = requireExisting(purchase.getId());
        ensureEditable(existing);

        List<MaterialPurchaseItem> newItems = normalizeItems(purchase.getItems());
        if (newItems.isEmpty()) {
            throw new IllegalArgumentException("请至少添加一条采购明细");
        }
        Map<Long, Integer> oldQtyMap = mergeQuantityByMaterial(materialPurchaseItemMapper.selectByPurchaseId(existing.getId()));
        Map<Long, Integer> newQtyMap = mergeQuantityByMaterial(newItems);

        applyStockDelta(oldQtyMap, newQtyMap);
        materialPurchaseItemMapper.deleteByPurchaseId(existing.getId());
        persistItems(existing.getId(), newItems);

        existing.setSupplier_name(trimToNull(purchase.getSupplier_name()));
        existing.setPurchase_date(purchase.getPurchase_date());
        existing.setPayment_method(normalizePaymentMethod(purchase.getPayment_method()));
        existing.setInvoice_image_url(trimToNull(purchase.getInvoice_image_url()));
        existing.setRemark(trimToNull(purchase.getRemark()));
        existing.setTotal_amount(sumItems(newItems));
        materialPurchaseMapper.update(existing);
        syncOrRecreateFinance(existing);
        return selectById(existing.getId());
    }

    @Transactional
    public MaterialPurchase voidPurchase(Long id, MaterialPurchaseVoidRequest request) {
        MaterialPurchase existing = requireExisting(id);
        if (STATUS_VOIDED.equals(existing.getStatus())) {
            throw new IllegalArgumentException("采购单已作废");
        }

        List<MaterialPurchaseItem> items = materialPurchaseItemMapper.selectByPurchaseId(id);
        for (MaterialPurchaseItem item : items) {
            if (item == null || item.getMaterial_id() == null || item.getMaterial_id() <= 0) {
                continue;
            }
            materialService.adjustStock(item.getMaterial_id(), -normalizeQuantity(item.getQuantity()));
        }

        if (existing.getFinance_record_id() != null && existing.getFinance_record_id() > 0) {
            financeService.deleteFinance(existing.getFinance_record_id().intValue());
        }

        existing.setStatus(STATUS_VOIDED);
        existing.setVoided_by(request == null ? null : request.getVoided_by());
        existing.setVoided_by_name(trimToNull(request == null ? null : request.getVoided_by_name()));
        String voidRemark = trimToNull(request == null ? null : request.getRemark());
        if (voidRemark != null && !voidRemark.isEmpty()) {
            existing.setRemark(mergeRemark(existing.getRemark(), "作废原因：" + voidRemark));
        }
        existing.setVoided_at(new Date());
        materialPurchaseMapper.update(existing);
        return selectById(id);
    }

    public String uploadInvoice(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择发票图片");
        }
        try {
            Files.createDirectories(Paths.get(INVOICE_UPLOAD_DIR));
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path target = Paths.get(INVOICE_UPLOAD_DIR, filename);
            file.transferTo(target.toFile());
            return filename;
        } catch (Exception exception) {
            throw new IllegalArgumentException("上传发票失败：" + exception.getMessage());
        }
    }

    public ResponseEntity<Resource> getInvoiceFile(Long purchaseId) {
        MaterialPurchase purchase = requireExisting(purchaseId);
        if (!StringUtils.hasText(purchase.getInvoice_image_url())) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(INVOICE_UPLOAD_DIR + purchase.getInvoice_image_url());
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    private void validatePurchase(MaterialPurchase purchase, boolean creating) {
        if (purchase == null) {
            throw new IllegalArgumentException("采购单信息不能为空");
        }
        if (purchase.getPurchase_date() == null) {
            throw new IllegalArgumentException("采购日期不能为空");
        }
        if (!StringUtils.hasText(purchase.getPayment_method())) {
            throw new IllegalArgumentException("付款方式不能为空");
        }
        normalizePaymentMethod(purchase.getPayment_method());
        if (!creating && purchase.getStatus() != null && !purchase.getStatus().trim().isEmpty()) {
            normalizeStatus(purchase.getStatus(), false);
        }
    }

    private List<MaterialPurchaseItem> normalizeItems(List<MaterialPurchaseItem> items) {
        if (items == null) {
            return List.of();
        }
        List<MaterialPurchaseItem> result = new ArrayList<>();
        for (MaterialPurchaseItem item : items) {
            if (item == null) {
                continue;
            }
            Material material = requireMaterial(item.getMaterial_id());
            int quantity = normalizeQuantity(item.getQuantity());
            if (quantity <= 0) {
                throw new IllegalArgumentException("采购数量必须大于0");
            }
            BigDecimal unitPrice = normalizeMoney(item.getUnit_price());
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("采购单价不能小于0");
            }
            MaterialPurchaseItem normalized = new MaterialPurchaseItem();
            normalized.setId(item.getId());
            normalized.setMaterial_id(material.getId());
            normalized.setMaterial_name(material.getName());
            normalized.setMaterial_spec(material.getSpec());
            normalized.setUnit_price(unitPrice);
            normalized.setQuantity(quantity);
            normalized.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            result.add(normalized);
        }
        return result;
    }

    private void saveItemsAndIncreaseStock(Long purchaseId, List<MaterialPurchaseItem> items) {
        persistItems(purchaseId, items);
        for (MaterialPurchaseItem item : items) {
            materialService.adjustStock(item.getMaterial_id(), normalizeQuantity(item.getQuantity()));
        }
    }

    private void persistItems(Long purchaseId, List<MaterialPurchaseItem> items) {
        for (MaterialPurchaseItem item : items) {
            item.setPurchase_id(purchaseId);
            materialPurchaseItemMapper.insert(item);
        }
    }

    private BigDecimal sumItems(List<MaterialPurchaseItem> items) {
        return items.stream()
                .map(item -> normalizeMoney(item.getSubtotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void applyStockDelta(Map<Long, Integer> oldQtyMap, Map<Long, Integer> newQtyMap) {
        Set<Long> materialIds = new java.util.LinkedHashSet<>();
        materialIds.addAll(oldQtyMap.keySet());
        materialIds.addAll(newQtyMap.keySet());
        for (Long materialId : materialIds) {
            int oldQty = oldQtyMap.getOrDefault(materialId, 0);
            int newQty = newQtyMap.getOrDefault(materialId, 0);
            int delta = newQty - oldQty;
            if (delta != 0) {
                materialService.adjustStock(materialId, delta);
            }
        }
    }

    private Map<Long, Integer> mergeQuantityByMaterial(List<MaterialPurchaseItem> items) {
        Map<Long, Integer> result = new HashMap<>();
        if (items == null) {
            return result;
        }
        for (MaterialPurchaseItem item : items) {
            if (item == null || item.getMaterial_id() == null || item.getMaterial_id() <= 0) {
                continue;
            }
            result.merge(item.getMaterial_id(), normalizeQuantity(item.getQuantity()), Integer::sum);
        }
        return result;
    }

    private void syncOrRecreateFinance(MaterialPurchase purchase) {
        if (purchase == null) {
            return;
        }
        String financeRemark = "采购自 " + normalizeText(purchase.getSupplier_name());
        if (purchase.getFinance_record_id() != null && purchase.getFinance_record_id() > 0) {
            List<Finance> finances = financeService.getFinanceByid(purchase.getFinance_record_id());
            if (finances != null && !finances.isEmpty() && finances.get(0) != null) {
                Finance finance = finances.get(0);
                finance.setName("耗材采购");
                finance.setAmount(normalizeMoney(purchase.getTotal_amount()).doubleValue());
                finance.setType("支出");
                finance.setBiz_type("material_purchase");
                finance.setRemark(buildRemarkWithBizId(String.valueOf(purchase.getId()), financeRemark));
                finance.setDate(formatDate(purchase.getPurchase_date()));
                financeService.editFinance(finance);
                return;
            }
        }
        Long financeId = financeService.recordExpense(
                null,
                null,
                normalizeMoney(purchase.getTotal_amount()),
                "耗材采购",
                financeRemark,
                "material_purchase",
                String.valueOf(purchase.getId())
        );
        purchase.setFinance_record_id(financeId);
        materialPurchaseMapper.update(purchase);
        syncFinanceDate(purchase);
    }

    private void syncFinanceDate(MaterialPurchase purchase) {
        if (purchase == null || purchase.getFinance_record_id() == null || purchase.getFinance_record_id() <= 0) {
            return;
        }
        List<Finance> finances = financeService.getFinanceByid(purchase.getFinance_record_id());
        if (finances == null || finances.isEmpty() || finances.get(0) == null) {
            return;
        }
        Finance finance = finances.get(0);
        finance.setDate(formatDate(purchase.getPurchase_date()));
        financeService.editFinance(finance);
    }

    private void attachItems(MaterialPurchase purchase) {
        if (purchase == null || purchase.getId() == null || purchase.getId() <= 0) {
            return;
        }
        purchase.setItems(materialPurchaseItemMapper.selectByPurchaseId(purchase.getId()));
    }

    private void ensureEditable(MaterialPurchase purchase) {
        if (purchase == null) {
            throw new IllegalArgumentException("采购单不存在");
        }
        if (STATUS_VOIDED.equals(purchase.getStatus())) {
            throw new IllegalArgumentException("已作废采购单不能编辑");
        }
        LocalDate createdDate = purchase.getCreated_at() == null
                ? LocalDate.now()
                : purchase.getCreated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!LocalDate.now().equals(createdDate)) {
            throw new IllegalArgumentException("采购单仅限创建当天编辑");
        }
    }

    private boolean inDateRange(Date value, LocalDate start, LocalDate end) {
        if (value == null) {
            return start == null && end == null;
        }
        LocalDate current = toLocalDate(value);
        if (start != null && current.isBefore(start)) {
            return false;
        }
        return end == null || !current.isAfter(end);
    }

    private MaterialPurchase requireExisting(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("采购单ID不能为空");
        }
        MaterialPurchase purchase = materialPurchaseMapper.selectById(id);
        if (purchase == null) {
            throw new IllegalArgumentException("采购单不存在");
        }
        return purchase;
    }

    private Material requireMaterial(Long materialId) {
        if (materialId == null || materialId <= 0) {
            throw new IllegalArgumentException("请选择耗材");
        }
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("耗材不存在");
        }
        return material;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod == null ? "" : paymentMethod.trim();
        if (!List.of("现金", "转账", "微信", "支付宝", "对公", "挂账").contains(normalized)) {
            throw new IllegalArgumentException("付款方式不合法");
        }
        return normalized;
    }

    private String normalizeStatus(String status, boolean applyDefault) {
        String normalized = status == null ? "" : status.trim();
        if (normalized.isEmpty() && applyDefault) {
            return STATUS_ACTIVE;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        if (!STATUS_ACTIVE.equals(normalized) && !STATUS_VOIDED.equals(normalized)) {
            throw new IllegalArgumentException("采购单状态不合法");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private int normalizeQuantity(Integer value) {
        return value == null ? 0 : value;
    }

    private LocalDate parseDate(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (Exception exception) {
            throw new IllegalArgumentException("日期格式应为yyyy-MM-dd");
        }
    }

    private String formatDate(Date date) {
        if (date == null) {
            return LocalDate.now().toString();
        }
        return toLocalDate(date).toString();
    }

    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String buildRemarkWithBizId(String bizId, String remark) {
        String normalizedBizId = bizId == null ? "" : bizId.trim();
        String normalizedRemark = normalizeText(remark);
        if (normalizedBizId.isEmpty()) {
            return normalizedRemark;
        }
        if (normalizedRemark.isEmpty()) {
            return "bizId=" + normalizedBizId;
        }
        return "bizId=" + normalizedBizId + " | " + normalizedRemark;
    }

    private String mergeRemark(String source, String appendix) {
        String left = normalizeText(source);
        String right = normalizeText(appendix);
        if (left.isEmpty()) {
            return right;
        }
        if (right.isEmpty()) {
            return left;
        }
        return left + " | " + right;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
