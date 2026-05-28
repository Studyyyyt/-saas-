package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientCustomGroup;
import com.example.springboot.entity.PatientCustomGroupMember;
import com.example.springboot.mapper.PatientCustomGroupMapper;
import com.example.springboot.mapper.PatientCustomGroupMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PatientCustomGroupService {

    private final PatientCustomGroupMapper groupMapper;
    private final PatientCustomGroupMemberMapper memberMapper;
    private final PatientService patientService;

    @Autowired
    public PatientCustomGroupService(PatientCustomGroupMapper groupMapper,
                                     PatientCustomGroupMemberMapper memberMapper,
                                     PatientService patientService) {
        this.groupMapper = groupMapper;
        this.memberMapper = memberMapper;
        this.patientService = patientService;
    }

    public List<PatientCustomGroup> selectActive() {
        return groupMapper.selectActive();
    }

    public List<PatientCustomGroupMember> selectMembersByPatientIds(List<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return List.of();
        }
        return memberMapper.selectByPatientIds(patientIds);
    }

    @Transactional
    public PatientCustomGroup create(PatientCustomGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("分组信息不能为空");
        }
        String groupName = trim(group.getGroup_name());
        if (!StringUtils.hasText(groupName)) {
            throw new IllegalArgumentException("分组名称不能为空");
        }
        if (groupName.length() > 50) {
            throw new IllegalArgumentException("分组名称不能超过50字");
        }

        Integer maxSortOrder = groupMapper.selectMaxSortOrder();
        PatientCustomGroup item = new PatientCustomGroup();
        item.setGroup_name(groupName);
        item.setRemark(trimToNull(group.getRemark()));
        item.setStatus(1);
        item.setSort_order((maxSortOrder == null ? 0 : maxSortOrder) + 10);
        item.setGroup_key(generateGroupKey(groupName));
        groupMapper.insert(item);
        return item;
    }

    @Transactional
    public void assignPatients(Long groupId, List<Long> patientIds) {
        if (groupId == null || groupId <= 0) {
            throw new IllegalArgumentException("患者分组不存在");
        }
        PatientCustomGroup group = groupMapper.selectById(groupId);
        if (group == null || !Integer.valueOf(1).equals(group.getStatus())) {
            throw new IllegalArgumentException("患者分组不存在或已停用");
        }
        Set<Long> normalizedIds = new LinkedHashSet<>();
        for (Long patientId : patientIds == null ? List.<Long>of() : patientIds) {
            if (patientId != null && patientId > 0) {
                normalizedIds.add(patientId);
            }
        }
        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("请选择患者");
        }
        for (Long patientId : normalizedIds) {
            List<Patient> patients = patientService.selectById(patientId);
            if (patients == null || patients.isEmpty()) {
                throw new IllegalArgumentException("存在无效患者ID：" + patientId);
            }
        }
        memberMapper.insertBatch(groupId, List.copyOf(normalizedIds));
    }

    private String generateGroupKey(String groupName) {
        String normalized = groupName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "");
        if (!StringUtils.hasText(normalized)) {
            normalized = "group";
        }
        String base = "custom_" + normalized;
        String candidate = base;
        int index = 1;
        while (groupMapper.selectByGroupKey(candidate) != null) {
            candidate = base + "_" + index++;
        }
        return candidate;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String normalized = trim(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
