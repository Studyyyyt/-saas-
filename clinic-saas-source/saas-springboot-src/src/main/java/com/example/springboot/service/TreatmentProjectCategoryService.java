package com.example.springboot.service;

import com.example.springboot.entity.TreatmentProjectCategory;
import com.example.springboot.mapper.TreatmentProjectCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TreatmentProjectCategoryService {

    public static final String STATUS_ENABLED = "启用";
    public static final String STATUS_DISABLED = "停用";
    public static final String STATUS_DELETED = "已删除";

    private final TreatmentProjectCategoryMapper categoryMapper;

    @Autowired
    public TreatmentProjectCategoryService(TreatmentProjectCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<TreatmentProjectCategory> selectTree(boolean includeDisabled) {
        List<TreatmentProjectCategory> all = categoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> !STATUS_DELETED.equals(item.getStatus()))
                .filter(item -> includeDisabled || STATUS_ENABLED.equals(item.getStatus()))
                .sorted(Comparator.comparing(TreatmentProjectCategory::getSort_order, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TreatmentProjectCategory::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::copyWithoutChildren)
                .toList();

        Map<Long, TreatmentProjectCategory> map = new LinkedHashMap<>();
        for (TreatmentProjectCategory item : all) {
            map.put(item.getId(), item);
        }

        List<TreatmentProjectCategory> roots = new ArrayList<>();
        for (TreatmentProjectCategory item : all) {
            Long parentId = item.getParent_id() == null ? 0L : item.getParent_id();
            if (parentId <= 0) {
                roots.add(item);
                continue;
            }
            TreatmentProjectCategory parent = map.get(parentId);
            if (parent != null) {
                parent.getChildren().add(item);
            }
        }
        return roots;
    }

    public List<TreatmentProjectCategory> selectAllActiveFlat() {
        return categoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> STATUS_ENABLED.equals(item.getStatus()))
                .filter(item -> !STATUS_DELETED.equals(item.getStatus()))
                .sorted(Comparator.comparing(TreatmentProjectCategory::getSort_order, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TreatmentProjectCategory::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public TreatmentProjectCategory selectById(Long id) {
        return id == null ? null : categoryMapper.selectById(id);
    }

    @Transactional
    public TreatmentProjectCategory add(TreatmentProjectCategory item) {
        validate(item, true);
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setName(item.getName().trim());
        item.setParent_id(item.getParent_id() == null ? 0L : item.getParent_id());
        item.setSort_order(normalizeSort(item.getSort_order()));
        item.setUpdated_by(item.getUpdated_by() != null ? item.getUpdated_by() : item.getCreated_by());
        item.setUpdated_by_name(StringUtils.hasText(item.getUpdated_by_name()) ? item.getUpdated_by_name() : item.getCreated_by_name());
        categoryMapper.insert(item);
        return item;
    }

    @Transactional
    public TreatmentProjectCategory edit(TreatmentProjectCategory item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        TreatmentProjectCategory existing = requireExisting(item.getId());
        validate(item, false);
        if (existing.getParent_id() != null && existing.getParent_id() > 0 && (item.getParent_id() == null || item.getParent_id() <= 0)) {
            throw new IllegalArgumentException("二级分类不能修改为一级分类");
        }
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setName(item.getName().trim());
        item.setParent_id(item.getParent_id() == null ? 0L : item.getParent_id());
        item.setSort_order(normalizeSort(item.getSort_order()));
        categoryMapper.update(item);
        return categoryMapper.selectById(item.getId());
    }

    @Transactional
    public void delete(Long id) {
        TreatmentProjectCategory existing = requireExisting(id);
        existing.setStatus(STATUS_DELETED);
        categoryMapper.update(existing);
    }

    public String resolveCategoryPath(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return "";
        }
        TreatmentProjectCategory current = requireExisting(categoryId);
        if (current.getParent_id() == null || current.getParent_id() <= 0) {
            return current.getName();
        }
        TreatmentProjectCategory parent = requireExisting(current.getParent_id());
        return parent.getName() + "/" + current.getName();
    }

    public List<Long> resolveCategoryScopeIds(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return List.of();
        }
        TreatmentProjectCategory category = requireExisting(categoryId);
        if (category.getParent_id() != null && category.getParent_id() > 0) {
            return List.of(categoryId);
        }
        return categoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> !STATUS_DELETED.equals(item.getStatus()))
                .filter(item -> categoryId.equals(item.getId()) || categoryId.equals(item.getParent_id()))
                .map(TreatmentProjectCategory::getId)
                .distinct()
                .toList();
    }

    private void validate(TreatmentProjectCategory item, boolean create) {
        if (item == null) {
            throw new IllegalArgumentException("分类信息不能为空");
        }
        if (!StringUtils.hasText(item.getName())) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        Long parentId = item.getParent_id() == null ? 0L : item.getParent_id();
        if (parentId > 0) {
            TreatmentProjectCategory parent = requireExisting(parentId);
            if (parent.getParent_id() != null && parent.getParent_id() > 0) {
                throw new IllegalArgumentException("分类最多支持两级");
            }
        }
        if (!create && !StringUtils.hasText(item.getStatus())) {
            throw new IllegalArgumentException("分类状态不能为空");
        }
    }

    private TreatmentProjectCategory requireExisting(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        TreatmentProjectCategory category = categoryMapper.selectById(id);
        if (category == null || STATUS_DELETED.equals(category.getStatus())) {
            throw new IllegalArgumentException("分类不存在");
        }
        return category;
    }

    private String normalizeStatus(String status, boolean applyDefault) {
        String normalized = status == null ? "" : status.trim();
        if (normalized.isEmpty() && applyDefault) {
            return STATUS_ENABLED;
        }
        if (!STATUS_ENABLED.equals(normalized) && !STATUS_DISABLED.equals(normalized) && !STATUS_DELETED.equals(normalized)) {
            throw new IllegalArgumentException("分类状态不合法");
        }
        return normalized;
    }

    private Integer normalizeSort(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private TreatmentProjectCategory copyWithoutChildren(TreatmentProjectCategory source) {
        TreatmentProjectCategory copy = new TreatmentProjectCategory();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setParent_id(source.getParent_id());
        copy.setSort_order(source.getSort_order());
        copy.setStatus(source.getStatus());
        copy.setCreated_by(source.getCreated_by());
        copy.setCreated_by_name(source.getCreated_by_name());
        copy.setUpdated_by(source.getUpdated_by());
        copy.setUpdated_by_name(source.getUpdated_by_name());
        copy.setCreated_at(source.getCreated_at());
        copy.setUpdated_at(source.getUpdated_at());
        copy.setChildren(new ArrayList<>());
        return copy;
    }
}
