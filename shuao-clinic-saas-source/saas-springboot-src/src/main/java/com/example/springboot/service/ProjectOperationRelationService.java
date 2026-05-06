package com.example.springboot.service;

import com.example.springboot.entity.ProjectOperationRelation;
import com.example.springboot.mapper.ProjectOperationRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectOperationRelationService {

    private final ProjectOperationRelationMapper relationMapper;

    @Autowired
    public ProjectOperationRelationService(ProjectOperationRelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    public List<ProjectOperationRelation> selectByProjectId(Long projectId) {
        if (projectId == null || projectId <= 0) {
            return List.of();
        }
        return relationMapper.selectByProjectId(projectId);
    }

    @Transactional
    public void saveByProjectId(Long projectId, List<ProjectOperationRelation> items) {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        relationMapper.deleteByProjectId(projectId);
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> operationIds = new LinkedHashSet<>();
        int order = 1;
        for (ProjectOperationRelation item : items) {
            if (item == null || item.getOperation_id() == null || item.getOperation_id() <= 0) {
                continue;
            }
            if (!operationIds.add(item.getOperation_id())) {
                continue;
            }
            ProjectOperationRelation normalized = new ProjectOperationRelation();
            normalized.setProject_id(projectId);
            normalized.setOperation_id(item.getOperation_id());
            normalized.setOperation_order(item.getOperation_order() != null && item.getOperation_order() > 0 ? item.getOperation_order() : order);
            normalized.setIs_required(item.getIs_required() != null && item.getIs_required() == 0 ? 0 : 1);
            normalized.setPerformance_weight(normalizePerformanceWeight(item.getPerformance_weight()));
            relationMapper.insert(normalized);
            order += 1;
        }
    }

    @Transactional
    public void deleteByProjectId(Long projectId) {
        if (projectId == null || projectId <= 0) {
            return;
        }
        relationMapper.deleteByProjectId(projectId);
    }

    @Transactional
    public void deleteByOperationId(Long operationId) {
        if (operationId == null || operationId <= 0) {
            return;
        }
        relationMapper.deleteByOperationId(operationId);
    }

    private Double normalizePerformanceWeight(Double value) {
        if (value == null) {
            return 1D;
        }
        return value < 0 ? 0D : value;
    }
}
