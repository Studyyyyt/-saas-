package com.example.springboot.service;

import com.example.springboot.entity.TreatmentOperation;
import com.example.springboot.mapper.TreatmentOperationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TreatmentOperationService {

    public static final String STATUS_ENABLED = "在用";
    public static final String STATUS_DISABLED = "停用";

    private final TreatmentOperationMapper operationMapper;
    private final ProjectOperationRelationService relationService;

    @Autowired
    public TreatmentOperationService(TreatmentOperationMapper operationMapper,
                                     ProjectOperationRelationService relationService) {
        this.operationMapper = operationMapper;
        this.relationService = relationService;
    }

    public List<TreatmentOperation> search(String keyword, String category, Integer needLabProcessing, String status) {
        String normalizedKeyword = normalizeText(keyword).toLowerCase(Locale.ROOT);
        String normalizedCategory = normalizeText(category).toLowerCase(Locale.ROOT);
        String normalizedStatus = normalizeStatus(status, false);
        return operationMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.getStatus()))
                .filter(item -> needLabProcessing == null || needLabProcessing < 0 || needLabProcessing.equals(item.getNeed_lab_processing()))
                .filter(item -> normalizedCategory.isEmpty() || normalizeText(item.getOperation_category()).toLowerCase(Locale.ROOT).contains(normalizedCategory))
                .filter(item -> normalizedKeyword.isEmpty() || matchesKeyword(item, normalizedKeyword))
                .sorted(Comparator.comparing(TreatmentOperation::getSort_order, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TreatmentOperation::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<TreatmentOperation> selectEnabled() {
        return operationMapper.selectEnabled();
    }

    public TreatmentOperation selectById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return operationMapper.selectById(id);
    }

    @Transactional
    public TreatmentOperation add(TreatmentOperation item) {
        normalizeAndValidate(item);
        operationMapper.insert(item);
        return item;
    }

    @Transactional
    public TreatmentOperation edit(TreatmentOperation item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("操作ID不能为空");
        }
        if (operationMapper.selectById(item.getId()) == null) {
            throw new IllegalArgumentException("操作不存在");
        }
        normalizeAndValidate(item);
        operationMapper.update(item);
        return operationMapper.selectById(item.getId());
    }

    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("操作ID不能为空");
        }
        relationService.deleteByOperationId(id);
        operationMapper.delete(id);
    }

    @Transactional
    public int importBatch(List<TreatmentOperation> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("导入数据不能为空");
        }
        int successCount = 0;
        for (TreatmentOperation item : items) {
            if (item == null) {
                continue;
            }
            normalizeAndValidate(item);
            operationMapper.insert(item);
            successCount += 1;
        }
        return successCount;
    }

    private void normalizeAndValidate(TreatmentOperation item) {
        if (item == null) {
            throw new IllegalArgumentException("操作数据不能为空");
        }
        if (!StringUtils.hasText(item.getOperation_code())) {
            throw new IllegalArgumentException("操作编码不能为空");
        }
        if (!StringUtils.hasText(item.getOperation_name())) {
            throw new IllegalArgumentException("操作名称不能为空");
        }
        item.setOperation_code(item.getOperation_code().trim());
        item.setOperation_name(item.getOperation_name().trim());
        item.setOperation_category(normalizeText(item.getOperation_category()));
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setNeed_lab_processing(item.getNeed_lab_processing() != null && item.getNeed_lab_processing() == 1 ? 1 : 0);
        item.setDefault_processing_days(item.getDefault_processing_days() == null || item.getDefault_processing_days() < 0 ? 0 : item.getDefault_processing_days());
        item.setSort_order(item.getSort_order() == null ? 0 : item.getSort_order());
        item.setRemark(StringUtils.hasText(item.getRemark()) ? item.getRemark().trim() : null);
    }

    private boolean matchesKeyword(TreatmentOperation item, String keyword) {
        String code = normalizeText(item.getOperation_code()).toLowerCase(Locale.ROOT);
        String name = normalizeText(item.getOperation_name()).toLowerCase(Locale.ROOT);
        String category = normalizeText(item.getOperation_category()).toLowerCase(Locale.ROOT);
        return code.contains(keyword) || name.contains(keyword) || category.contains(keyword);
    }

    private String normalizeStatus(String status, boolean applyDefault) {
        String normalized = normalizeText(status);
        if (normalized.isEmpty() && applyDefault) {
            return STATUS_ENABLED;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        if (!STATUS_ENABLED.equals(normalized) && !STATUS_DISABLED.equals(normalized)) {
            throw new IllegalArgumentException("操作状态不合法");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
