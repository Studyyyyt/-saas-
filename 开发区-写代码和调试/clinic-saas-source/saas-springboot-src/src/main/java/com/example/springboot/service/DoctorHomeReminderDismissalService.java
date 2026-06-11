package com.example.springboot.service;

import com.example.springboot.entity.DoctorHomeReminderDismissal;
import com.example.springboot.mapper.DoctorHomeReminderDismissalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DoctorHomeReminderDismissalService {

    private final DoctorHomeReminderDismissalMapper mapper;

    @Autowired
    public DoctorHomeReminderDismissalService(DoctorHomeReminderDismissalMapper mapper) {
        this.mapper = mapper;
    }

    public List<String> selectReminderKeysByDoctorAccountId(Long doctorAccountId) {
        validateDoctorAccountId(doctorAccountId);
        return mapper.selectReminderKeysByDoctorAccountId(doctorAccountId);
    }

    public void dismiss(DoctorHomeReminderDismissal item) {
        if (item == null) {
            throw new IllegalArgumentException("提醒数据不能为空");
        }
        validateDoctorAccountId(item.getDoctor_account_id());
        if (!StringUtils.hasText(item.getReminder_key())) {
            throw new IllegalArgumentException("提醒标识不能为空");
        }
        item.setReminder_key(item.getReminder_key().trim());
        item.setDoctor_name(trimToNull(item.getDoctor_name()));
        item.setPatient_name(trimToNull(item.getPatient_name()));
        mapper.upsert(item);
    }

    public void clear(Long doctorAccountId, String reminderKey) {
        validateDoctorAccountId(doctorAccountId);
        if (!StringUtils.hasText(reminderKey)) {
            throw new IllegalArgumentException("提醒标识不能为空");
        }
        mapper.delete(doctorAccountId, reminderKey.trim());
    }

    private void validateDoctorAccountId(Long doctorAccountId) {
        if (doctorAccountId == null || doctorAccountId <= 0) {
            throw new IllegalArgumentException("医生账号ID不能为空");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
