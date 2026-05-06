package com.example.springboot.service;

import com.example.springboot.entity.MaterialCategory;
import com.example.springboot.mapper.MaterialCategoryMapper;
import com.example.springboot.mapper.MaterialMapper;
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
public class MaterialCategoryService {

    public static final String STATUS_ENABLED = "启用";
    public static final String STATUS_DISABLED = "停用";
    public static final String STATUS_DELETED = "已删除";

    private final MaterialCategoryMapper materialCategoryMapper;
    private final MaterialMapper materialMapper;

    @Autowired
    public MaterialCategoryService(MaterialCategoryMapper materialCategoryMapper,
                                   MaterialMapper materialMapper) {
        this.materialCategoryMapper = materialCategoryMapper;
        this.materialMapper = materialMapper;
    }

    public List<MaterialCategory> selectTree(boolean includeDisabled) {
        List<MaterialCategory> all = materialCategoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> !STATUS_DELETED.equals(item.getStatus()))
                .filter(item -> includeDisabled || STATUS_ENABLED.equals(item.getStatus()))
                .sorted(Comparator.comparing(MaterialCategory::getSort_order, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MaterialCategory::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::copyWithoutChildren)
                .toList();

        Map<Long, MaterialCategory> map = new LinkedHashMap<>();
        for (MaterialCategory item : all) {
            map.put(item.getId(), item);
        }

        List<MaterialCategory> roots = new ArrayList<>();
        for (MaterialCategory item : all) {
            Long parentId = item.getParent_id() == null ? 0L : item.getParent_id();
            if (parentId <= 0) {
                roots.add(item);
                continue;
            }
            MaterialCategory parent = map.get(parentId);
            if (parent != null) {
                parent.getChildren().add(item);
            }
        }
        return roots;
    }

    public List<MaterialCategory> selectAllActiveFlat() {
        return materialCategoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> STATUS_ENABLED.equals(item.getStatus()))
                .filter(item -> !STATUS_DELETED.equals(item.getStatus()))
                .sorted(Comparator.comparing(MaterialCategory::getSort_order, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MaterialCategory::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public MaterialCategory selectById(Long id) {
        return id == null ? null : materialCategoryMapper.selectById(id);
    }

    @Transactional
    public MaterialCategory add(MaterialCategory item) {
        validate(item, true);
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setName(item.getName().trim());
        item.setParent_id(item.getParent_id() == null ? 0L : item.getParent_id());
        item.setSort_order(normalizeSort(item.getSort_order()));
        materialCategoryMapper.insert(item);
        return item;
    }

    @Transactional
    public MaterialCategory edit(MaterialCategory item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        MaterialCategory existing = requireExisting(item.getId());
        validate(item, false);
        if (existing.getParent_id() != null && existing.getParent_id() > 0 && (item.getParent_id() == null || item.getParent_id() <= 0)) {
            throw new IllegalArgumentException("二级分类不能修改为一级分类");
        }
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setName(item.getName().trim());
        item.setParent_id(item.getParent_id() == null ? 0L : item.getParent_id());
        item.setSort_order(normalizeSort(item.getSort_order()));
        materialCategoryMapper.update(item);
        return materialCategoryMapper.selectById(item.getId());
    }

    @Transactional
    public void delete(Long id) {
        MaterialCategory existing = requireExisting(id);
        boolean rootCategory = existing.getParent_id() == null || existing.getParent_id() <= 0;
        if (rootCategory) {
            existing.setStatus(STATUS_DELETED);
            materialCategoryMapper.update(existing);
            return;
        }
        boolean usedByMaterial = materialMapper.selectAll().stream()
                .anyMatch(item -> item != null && id.equals(item.getCategory_id()));
        if (usedByMaterial) {
            throw new IllegalArgumentException("该子分类已被耗材使用，不能删除");
        }
        materialCategoryMapper.delete(id);
    }

    public String resolveCategoryName(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return "";
        }
        MaterialCategory category = materialCategoryMapper.selectById(categoryId);
        if (category == null || STATUS_DELETED.equals(category.getStatus())) {
            throw new IllegalArgumentException("耗材分类不存在");
        }
        return category.getName();
    }

    public List<Long> resolveCategoryScopeIds(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return List.of();
        }
        MaterialCategory category = requireExisting(categoryId);
        if (category.getParent_id() != null && category.getParent_id() > 0) {
            return List.of(categoryId);
        }
        return materialCategoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> !STATUS_DELETED.equals(item.getStatus()))
                .filter(item -> categoryId.equals(item.getId()) || categoryId.equals(item.getParent_id()))
                .map(MaterialCategory::getId)
                .distinct()
                .toList();
    }

    private void validate(MaterialCategory item, boolean create) {
        if (item == null) {
            throw new IllegalArgumentException("分类信息不能为空");
        }
        if (!StringUtils.hasText(item.getName())) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        Long parentId = item.getParent_id() == null ? 0L : item.getParent_id();
        if (parentId > 0) {
            MaterialCategory parent = requireExisting(parentId);
            if (parent.getParent_id() != null && parent.getParent_id() > 0) {
                throw new IllegalArgumentException("分类最多支持两级");
            }
        }
        if (!create && normalizeStatus(item.getStatus(), false).isEmpty()) {
            throw new IllegalArgumentException("分类状态不能为空");
        }
    }

    private MaterialCategory requireExisting(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        MaterialCategory category = materialCategoryMapper.selectById(id);
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
        if (normalized.isEmpty()) {
            return "";
        }
        if (!STATUS_ENABLED.equals(normalized) && !STATUS_DISABLED.equals(normalized) && !STATUS_DELETED.equals(normalized)) {
            throw new IllegalArgumentException("分类状态不合法");
        }
        return normalized;
    }

    private Integer normalizeSort(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private MaterialCategory copyWithoutChildren(MaterialCategory source) {
        MaterialCategory copy = new MaterialCategory();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setParent_id(source.getParent_id());
        copy.setSort_order(source.getSort_order());
        copy.setStatus(source.getStatus());
        copy.setCreated_at(source.getCreated_at());
        copy.setUpdated_at(source.getUpdated_at());
        copy.setChildren(new ArrayList<>());
        return copy;
    }
}
