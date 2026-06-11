package com.example.springboot.service;

import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.entity.MedicalRecordTemplate;
import com.example.springboot.mapper.MedicalRecordTemplateMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalRecordTemplateService {

    private final MedicalRecordTemplateMapper mapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public MedicalRecordTemplateService(MedicalRecordTemplateMapper mapper,
                                        ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public List<MedicalRecordTemplate> selectAll() {
        List<MedicalRecordTemplate> rows = mapper.selectAll();
        rows.forEach(this::hydrateOperationItems);
        return rows;
    }

    public List<MedicalRecordTemplate> selectEnabled() {
        List<MedicalRecordTemplate> rows = mapper.selectEnabled();
        rows.forEach(this::hydrateOperationItems);
        return rows;
    }

    public MedicalRecordTemplate selectById(Long id) {
        MedicalRecordTemplate item = mapper.selectById(id);
        hydrateOperationItems(item);
        return item;
    }

    public MedicalRecordTemplate add(MedicalRecordTemplate item) {
        normalize(item, true);
        mapper.add(item);
        hydrateOperationItems(item);
        return item;
    }

    public MedicalRecordTemplate edit(MedicalRecordTemplate item) {
        normalize(item, false);
        mapper.edit(item);
        hydrateOperationItems(item);
        return item;
    }

    public void delete(Long id) {
        mapper.delete(id);
    }

    private void normalize(MedicalRecordTemplate item, boolean creating) {
        if (item == null) {
            throw new IllegalArgumentException("模板数据不能为空");
        }
        if (!StringUtils.hasText(item.getTemplate_name())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (!creating && (item.getId() == null || item.getId() <= 0)) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        item.setTemplate_name(item.getTemplate_name().trim());
        item.setTemplate_category(trimToNull(item.getTemplate_category()) == null ? "常用模板" : item.getTemplate_category().trim());
        item.setChief_complaint(trimToNull(item.getChief_complaint()));
        item.setPresent_illness_history(trimToNull(item.getPresent_illness_history()));
        item.setPast_history(trimToNull(item.getPast_history()));
        item.setInfectious_history(trimToNull(item.getInfectious_history()));
        item.setAllergy_history(trimToNull(item.getAllergy_history()));
        item.setGeneral_condition(trimToNull(item.getGeneral_condition()));
        item.setExamination(trimToNull(item.getExamination()));
        item.setAuxiliary_examination(trimToNull(item.getAuxiliary_examination()));
        item.setDiagnosis(trimToNull(item.getDiagnosis()));
        item.setTreatment_plan(trimToNull(item.getTreatment_plan()));
        item.setTreatment(trimToNull(item.getTreatment()));
        item.setTooth_positions(trimToNull(item.getTooth_positions()));
        item.setMedical_advice(trimToNull(item.getMedical_advice()));
        item.setPrescription(trimToNull(item.getPrescription()));
        item.setRecord_tags(trimToNull(item.getRecord_tags()));
        item.setImage_summary(trimToNull(item.getImage_summary()));
        item.setNotes(trimToNull(item.getNotes()));
        item.setRecord_type(trimToNull(item.getRecord_type()));
        item.setCreated_by_name(trimToNull(item.getCreated_by_name()));
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        item.setOperation_items_json(writeOperationItems(item.getOperation_items()));
    }

    private void hydrateOperationItems(MedicalRecordTemplate item) {
        if (item == null) {
            return;
        }
        item.setOperation_items(readOperationItems(item.getOperation_items_json()));
    }

    private String writeOperationItems(List<MedicalRecordOperation> items) {
        try {
            return objectMapper.writeValueAsString(items == null ? List.of() : items);
        } catch (Exception exception) {
            throw new IllegalArgumentException("病历模板结构化操作保存失败");
        }
    }

    private List<MedicalRecordOperation> readOperationItems(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            List<MedicalRecordOperation> rows = objectMapper.readValue(json, new TypeReference<List<MedicalRecordOperation>>() {});
            return rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
