package com.example.springboot.service;

import com.example.springboot.entity.Material;
import com.example.springboot.entity.MaterialCategory;
import com.example.springboot.mapper.MaterialCategoryMapper;
import com.example.springboot.mapper.MaterialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MaterialService {

    public static final String STATUS_ACTIVE = "在用";
    public static final String STATUS_DISABLED = "停用";

    private final MaterialMapper materialMapper;
    private final MaterialCategoryService materialCategoryService;
    private final MaterialCategoryMapper materialCategoryMapper;

    @Autowired
    public MaterialService(MaterialMapper materialMapper,
                           MaterialCategoryService materialCategoryService,
                           MaterialCategoryMapper materialCategoryMapper) {
        this.materialMapper = materialMapper;
        this.materialCategoryService = materialCategoryService;
        this.materialCategoryMapper = materialCategoryMapper;
    }

    public List<Material> search(String keyword,
                                 Long categoryId,
                                 Boolean lowStockOnly,
                                 String status) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedStatus = normalizeStatus(status, false);
        Set<Long> categoryScope = Set.copyOf(materialCategoryService.resolveCategoryScopeIds(categoryId));
        boolean filterLowStock = Boolean.TRUE.equals(lowStockOnly);

        // 将过滤和排序下沉到数据库层，避免全量加载到内存
        return materialMapper.search(
                normalizedKeyword.isEmpty() ? null : normalizedKeyword,
                categoryScope.isEmpty() ? null : categoryScope,
                normalizedStatus.isEmpty() ? null : normalizedStatus,
                filterLowStock ? true : null
        );
    }

    public List<Material> selectEnabled(String keyword, Long categoryId, Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 30 : Math.min(limit, 100);
        return search(keyword, categoryId, false, STATUS_ACTIVE).stream().limit(safeLimit).toList();
    }

    public Material selectById(Long id) {
        return id == null ? null : materialMapper.selectById(id);
    }

    @Transactional
    public Material add(Material item) {
        validate(item);
        item.setCategory_name(resolveCategoryName(item.getCategory_id()));
        item.setName(item.getName().trim());
        item.setSpec(trimToNull(item.getSpec()));
        item.setBrand(trimToNull(item.getBrand()));
        item.setUnit(trimToNull(item.getUnit()));
        item.setRemark(trimToNull(item.getRemark()));
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setMin_stock_alert(normalizeNonNegative(item.getMin_stock_alert()));
        item.setCurrent_stock(normalizeNonNegative(item.getCurrent_stock()));
        materialMapper.insert(item);
        return item;
    }

    @Transactional
    public Material edit(Material item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("耗材ID不能为空");
        }
        requireExisting(item.getId());
        validate(item);
        item.setCategory_name(resolveCategoryName(item.getCategory_id()));
        item.setName(item.getName().trim());
        item.setSpec(trimToNull(item.getSpec()));
        item.setBrand(trimToNull(item.getBrand()));
        item.setUnit(trimToNull(item.getUnit()));
        item.setRemark(trimToNull(item.getRemark()));
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setMin_stock_alert(normalizeNonNegative(item.getMin_stock_alert()));
        item.setCurrent_stock(normalizeNonNegative(item.getCurrent_stock()));
        materialMapper.update(item);
        return materialMapper.selectById(item.getId());
    }

    @Transactional
    public void adjustStock(Long materialId, int delta) {
        Material material = requireExisting(materialId);
        int nextStock = normalizeNonNegative(material.getCurrent_stock()) + delta;
        if (nextStock < 0) {
            throw new IllegalArgumentException("耗材库存不足，无法扣减");
        }
        material.setCurrent_stock(nextStock);
        materialMapper.update(material);
    }

    private void validate(Material item) {
        if (item == null) {
            throw new IllegalArgumentException("耗材信息不能为空");
        }
        if (!StringUtils.hasText(item.getName())) {
            throw new IllegalArgumentException("耗材名称不能为空");
        }
        if (item.getCategory_id() == null || item.getCategory_id() <= 0) {
            throw new IllegalArgumentException("请选择耗材分类");
        }
        MaterialCategory category = materialCategoryMapper.selectById(item.getCategory_id());
        if (category == null || MaterialCategoryService.STATUS_DELETED.equals(category.getStatus())) {
            throw new IllegalArgumentException("耗材分类不存在");
        }
        if (!STATUS_ACTIVE.equals(normalizeStatus(item.getStatus(), true)) && !STATUS_DISABLED.equals(normalizeStatus(item.getStatus(), true))) {
            throw new IllegalArgumentException("耗材状态不合法");
        }
        if (normalizeNonNegative(item.getMin_stock_alert()) < 0 || normalizeNonNegative(item.getCurrent_stock()) < 0) {
            throw new IllegalArgumentException("库存数值不能小于0");
        }
    }

    private Material requireExisting(Long materialId) {
        if (materialId == null || materialId <= 0) {
            throw new IllegalArgumentException("耗材ID不能为空");
        }
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("耗材不存在");
        }
        return material;
    }

    private String resolveCategoryName(Long categoryId) {
        return materialCategoryService.resolveCategoryName(categoryId);
    }

    private String normalizeStatus(String status, boolean applyDefault) {
        String normalized = status == null ? "" : status.trim();
        if (normalized.isEmpty() && applyDefault) {
            return STATUS_ACTIVE;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        if (!STATUS_ACTIVE.equals(normalized) && !STATUS_DISABLED.equals(normalized)) {
            throw new IllegalArgumentException("耗材状态不合法");
        }
        return normalized;
    }

    private Integer normalizeNonNegative(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
