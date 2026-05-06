package com.example.springboot.service;

import com.example.springboot.entity.ProjectOperationRelation;
import com.example.springboot.entity.TreatmentProject;
import com.example.springboot.mapper.TreatmentProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TreatmentProjectService {

    public static final String STATUS_ENABLED = "在用";
    public static final String STATUS_DISABLED = "停用";

    private final TreatmentProjectMapper projectMapper;
    private final TreatmentProjectCategoryService categoryService;
    private final ProjectOperationRelationService relationService;

    @Autowired
    public TreatmentProjectService(TreatmentProjectMapper projectMapper,
                                   TreatmentProjectCategoryService categoryService,
                                   ProjectOperationRelationService relationService) {
        this.projectMapper = projectMapper;
        this.categoryService = categoryService;
        this.relationService = relationService;
    }

    public List<TreatmentProject> search(String keyword, Long categoryId, String status) {
        String normalizedKeyword = normalizeText(keyword).toLowerCase(Locale.ROOT);
        String normalizedStatus = normalizeStatus(status, false);
        List<Long> scopeCategoryIds = categoryService.resolveCategoryScopeIds(categoryId);
        return projectMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.getStatus()))
                .filter(item -> scopeCategoryIds.isEmpty() || scopeCategoryIds.contains(item.getCategory_id()))
                .filter(item -> normalizedKeyword.isEmpty() || matchesKeyword(item, normalizedKeyword))
                .sorted(Comparator.comparing(TreatmentProject::getSort_order, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TreatmentProject::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(item -> enrichProject(item, false))
                .toList();
    }

    public List<TreatmentProject> selectEnabled() {
        return projectMapper.selectEnabled().stream()
                .filter(Objects::nonNull)
                .map(item -> enrichProject(item, false))
                .toList();
    }

    public TreatmentProject selectById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return enrichProject(projectMapper.selectById(id), true);
    }

    @Transactional
    public TreatmentProject add(TreatmentProject item) {
        normalizeAndValidate(item, true);
        projectMapper.insert(item);
        relationService.saveByProjectId(item.getId(), item.getOperation_relations());
        return selectById(item.getId());
    }

    @Transactional
    public TreatmentProject edit(TreatmentProject item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (projectMapper.selectById(item.getId()) == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        normalizeAndValidate(item, false);
        projectMapper.update(item);
        if (item.getOperation_relations() != null) {
            relationService.saveByProjectId(item.getId(), item.getOperation_relations());
        }
        return selectById(item.getId());
    }

    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        relationService.deleteByProjectId(id);
        projectMapper.delete(id);
    }

    @Transactional
    public int importBatch(List<TreatmentProject> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("导入数据不能为空");
        }
        int successCount = 0;
        for (TreatmentProject item : items) {
            if (item == null) {
                continue;
            }
            normalizeAndValidate(item, true);
            projectMapper.insert(item);
            relationService.saveByProjectId(item.getId(), item.getOperation_relations());
            successCount += 1;
        }
        return successCount;
    }

    private TreatmentProject enrichProject(TreatmentProject item, boolean withRelations) {
        if (item == null) {
            return null;
        }
        item.setCategory_name(resolveCategoryName(item.getCategory_path()));
        if (withRelations) {
            List<ProjectOperationRelation> relations = relationService.selectByProjectId(item.getId());
            item.setOperation_relations(relations);
        }
        return item;
    }

    private void normalizeAndValidate(TreatmentProject item, boolean create) {
        if (item == null) {
            throw new IllegalArgumentException("项目数据不能为空");
        }
        if (!StringUtils.hasText(item.getProject_code())) {
            throw new IllegalArgumentException("项目编码不能为空");
        }
        if (!StringUtils.hasText(item.getProject_name())) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        item.setProject_code(item.getProject_code().trim());
        item.setProject_name(item.getProject_name().trim());
        item.setStatus(normalizeStatus(item.getStatus(), true));
        item.setSort_order(item.getSort_order() == null ? 0 : item.getSort_order());
        item.setEstimated_visit_count(item.getEstimated_visit_count() == null || item.getEstimated_visit_count() <= 0 ? 1 : item.getEstimated_visit_count());
        item.setEstimated_cycle_days(item.getEstimated_cycle_days() == null || item.getEstimated_cycle_days() < 0 ? 0 : item.getEstimated_cycle_days());
        item.setDefault_price(normalizeMoney(item.getDefault_price()));
        item.setRemark(trimToNull(item.getRemark()));
        item.setCategory_id(item.getCategory_id() != null && item.getCategory_id() > 0 ? item.getCategory_id() : null);
        item.setCategory_path(item.getCategory_id() == null ? "" : categoryService.resolveCategoryPath(item.getCategory_id()));
        if (create) {
            item.setLegacy_treatment_catalog_id(null);
        }
    }

    private boolean matchesKeyword(TreatmentProject item, String keyword) {
        String code = normalizeText(item.getProject_code()).toLowerCase(Locale.ROOT);
        String name = normalizeText(item.getProject_name()).toLowerCase(Locale.ROOT);
        String path = normalizeText(item.getCategory_path()).toLowerCase(Locale.ROOT);
        return code.contains(keyword) || name.contains(keyword) || path.contains(keyword);
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
            throw new IllegalArgumentException("项目状态不合法");
        }
        return normalized;
    }

    private String resolveCategoryName(String categoryPath) {
        String normalized = normalizeText(categoryPath);
        if (normalized.isEmpty()) {
            return "";
        }
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
