package com.example.springboot.service;

import com.example.springboot.entity.ConsultationFollowup;
import com.example.springboot.entity.ConsultationRecord;
import com.example.springboot.mapper.ConsultationFollowupMapper;
import com.example.springboot.mapper.ConsultationRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class ConsultationFollowupService {

    @Autowired
    private ConsultationFollowupMapper consultationFollowupMapper;

    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    @Transactional
    public ConsultationFollowup add(ConsultationFollowup followup) {
        if (followup == null) {
            throw new IllegalArgumentException("跟进记录不能为空");
        }
        if (followup.getConsultation_id() == null || followup.getConsultation_id() <= 0) {
            throw new IllegalArgumentException("关联咨询记录ID不能为空");
        }
        ConsultationRecord consultation = consultationRecordMapper.selectById(followup.getConsultation_id());
        if (consultation == null) {
            throw new IllegalArgumentException("关联的咨询记录不存在");
        }
        if (!StringUtils.hasText(followup.getContent())) {
            throw new IllegalArgumentException("跟进内容不能为空");
        }
        Long operatorId = normalizePositiveId(followup.getCreated_by());
        if (operatorId == null) {
            throw new IllegalArgumentException("跟进人ID不能为空");
        }
        followup.setCreated_by(operatorId);
        followup.setCreated_by_name(trim(followup.getCreated_by_name()));
        followup.setFollowup_time(followup.getFollowup_time() != null ? followup.getFollowup_time() : new Date());
        followup.setNext_followup_time(followup.getNext_followup_time());
        followup.setContent(trim(followup.getContent()));

        consultationFollowupMapper.insert(followup);
        return followup;
    }

    public List<ConsultationFollowup> listByConsultationId(Long consultationId) {
        if (consultationId == null || consultationId <= 0) {
            return List.of();
        }
        return consultationFollowupMapper.selectByConsultationId(consultationId);
    }

    public int countByConsultationId(Long consultationId) {
        if (consultationId == null || consultationId <= 0) {
            return 0;
        }
        return consultationFollowupMapper.countByConsultationId(consultationId);
    }

    @Transactional
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            return;
        }
        consultationFollowupMapper.deleteById(id);
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
