package com.example.springboot.service;

import com.example.springboot.entity.MedicalRecordPhrase;
import com.example.springboot.mapper.MedicalRecordPhraseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MedicalRecordPhraseService {

    private final MedicalRecordPhraseMapper mapper;

    @Autowired
    public MedicalRecordPhraseService(MedicalRecordPhraseMapper mapper) {
        this.mapper = mapper;
    }

    public List<MedicalRecordPhrase> selectByFieldType(String fieldType) {
        return mapper.selectByFieldType(fieldType);
    }

    public List<MedicalRecordPhrase> selectAll() {
        return mapper.selectAll();
    }

    public MedicalRecordPhrase selectById(Long id) {
        return mapper.selectById(id);
    }

    public MedicalRecordPhrase add(MedicalRecordPhrase item) {
        normalize(item, true);
        mapper.add(item);
        return item;
    }

    public MedicalRecordPhrase edit(MedicalRecordPhrase item) {
        normalize(item, false);
        mapper.edit(item);
        return item;
    }

    public void delete(Long id) {
        mapper.delete(id);
    }

    private void normalize(MedicalRecordPhrase item, boolean creating) {
        if (item == null) {
            throw new IllegalArgumentException("词条数据不能为空");
        }
        if (!StringUtils.hasText(item.getField_type())) {
            throw new IllegalArgumentException("字段类型不能为空");
        }
        if (!StringUtils.hasText(item.getContent())) {
            throw new IllegalArgumentException("词条内容不能为空");
        }
        if (!creating && (item.getId() == null || item.getId() <= 0)) {
            throw new IllegalArgumentException("词条ID不能为空");
        }
        item.setField_type(item.getField_type().trim());
        item.setContent(item.getContent().trim());
        item.setCategory(trimToEmpty(item.getCategory()));
        if (item.getSort_order() == null) {
            item.setSort_order(0);
        }
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
