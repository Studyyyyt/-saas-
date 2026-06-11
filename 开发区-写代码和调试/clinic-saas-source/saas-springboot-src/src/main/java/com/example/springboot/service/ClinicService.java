package com.example.springboot.service;

import com.example.springboot.entity.Clinic;
import com.example.springboot.entity.UserClinic;
import com.example.springboot.mapper.ClinicMapper;
import com.example.springboot.mapper.UserClinicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ClinicService {

    @Autowired
    private ClinicMapper clinicMapper;

    @Autowired
    private UserClinicMapper userClinicMapper;

    @Autowired
    private LicenseVerificationService licenseVerificationService;

    public List<Clinic> listClinics(String name) {
        return clinicMapper.selectList(name);
    }

    public Clinic getById(String id) {
        return clinicMapper.selectById(id);
    }

    @Transactional
    public String createClinic(Clinic clinic, Integer creatorUserId) {
        if (clinic == null || !StringUtils.hasText(clinic.getName())) {
            throw new IllegalArgumentException("诊所名称必填");
        }
        // 自动生成 ID（如果未指定）
        if (!StringUtils.hasText(clinic.getId())) {
            clinic.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        // 校验 ID 唯一性
        if (clinicMapper.countById(clinic.getId()) > 0) {
            throw new IllegalArgumentException("诊所ID已存在");
        }
        // 校验名称唯一性
        if (clinicMapper.countByName(clinic.getName()) > 0) {
            throw new IllegalArgumentException("诊所名称已存在");
        }
        clinic.setStatus(1);
        clinicMapper.insert(clinic);

        // 创建者自动绑定为新诊所的 admin
        if (creatorUserId != null) {
            assignClinicToUser(creatorUserId, clinic.getId(), "admin", 1);
        }

        return clinic.getId();
    }

    @Transactional
    public void updateClinic(Clinic clinic) {
        if (clinic == null || !StringUtils.hasText(clinic.getId())) {
            throw new IllegalArgumentException("诊所ID必填");
        }
        Clinic existing = clinicMapper.selectById(clinic.getId());
        if (existing == null) {
            throw new IllegalArgumentException("诊所不存在");
        }
        // 名称修改时校验唯一性
        if (StringUtils.hasText(clinic.getName()) && !clinic.getName().equals(existing.getName())) {
            if (clinicMapper.countByName(clinic.getName()) > 0) {
                throw new IllegalArgumentException("诊所名称已存在");
            }
        }
        clinicMapper.update(clinic);
    }

    @Transactional
    public void deleteClinic(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("诊所ID必填");
        }
        // 检查是否有关联用户
        int userCount = userClinicMapper.countByClinicId(id);
        if (userCount > 0) {
            throw new IllegalArgumentException("该诊所下还有 " + userCount + " 位关联用户，无法删除");
        }
        clinicMapper.deleteById(id);
    }

    @Transactional
    public void toggleStatus(String id, Integer status) {
        if (!StringUtils.hasText(id) || status == null) {
            throw new IllegalArgumentException("参数非法");
        }
        Clinic clinic = new Clinic();
        clinic.setId(id);
        clinic.setStatus(status);
        clinicMapper.update(clinic);
    }

    /**
     * 获取用户关联的诊所列表（含角色信息）
     */
    public List<Map<String, Object>> getUserClinics(Integer userId) {
        List<UserClinic> userClinics = userClinicMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserClinic uc : userClinics) {
            Clinic clinic = clinicMapper.selectById(uc.getClinicId());
            if (clinic == null || clinic.getStatus() == null || clinic.getStatus() != 1) {
                continue; // 跳过已禁用的诊所
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("clinicId", uc.getClinicId());
            item.put("clinicName", clinic.getName());
            item.put("role", uc.getRole());
            item.put("isDefault", uc.getIsDefault());
            result.add(item);
        }
        return result;
    }

    /**
     * 设置用户的默认诊所
     */
    @Transactional
    public void setDefaultClinic(Integer userId, String clinicId) {
        userClinicMapper.clearDefaultByUserId(userId);
        userClinicMapper.setDefaultClinic(userId, clinicId);
    }

    /**
     * 分配用户到诊所
     */
    @Transactional
    public void assignClinicToUser(Integer userId, String clinicId, String role, Integer isDefault) {
        UserClinic existing = userClinicMapper.selectByUserAndClinic(userId, clinicId);
        if (existing != null) {
            // 已存在，更新角色
            existing.setRole(role);
            if (isDefault != null && isDefault == 1) {
                userClinicMapper.clearDefaultByUserId(userId);
                existing.setIsDefault(1);
            }
            userClinicMapper.update(existing);
        } else {
            if (isDefault != null && isDefault == 1) {
                userClinicMapper.clearDefaultByUserId(userId);
            }
            UserClinic uc = new UserClinic();
            uc.setUserId(userId);
            uc.setClinicId(clinicId);
            uc.setRole(role);
            uc.setIsDefault(isDefault != null ? isDefault : 0);
            userClinicMapper.insert(uc);
        }
    }

    /**
     * 移除用户的诊所分配
     */
    @Transactional
    public void removeClinicFromUser(Integer userId, String clinicId) {
        userClinicMapper.deleteByUserAndClinic(userId, clinicId);
    }

    /**
     * 移除用户的所有诊所分配
     */
    @Transactional
    public void removeAllClinicsFromUser(Integer userId) {
        userClinicMapper.deleteByUserId(userId);
    }

    /**
     * 获取诊所的统计信息
     */
    public Map<String, Object> getClinicStats(String clinicId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("userCount", userClinicMapper.countByClinicId(clinicId));
        // 更多统计可以通过各 Mapper 的 COUNT 查询实现
        return stats;
    }
}
