package com.example.springboot.mapper;

import com.example.springboot.entity.MedicalRecordAIField;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MedicalRecordAIFieldMapper {

    @Results(id = "fieldMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "fieldKey", column = "field_key"),
            @Result(property = "fieldName", column = "field_name"),
            @Result(property = "isEnabled", column = "is_enabled"),
            @Result(property = "maxLength", column = "max_length"),
            @Result(property = "isRequired", column = "is_required"),
            @Result(property = "validationRule", column = "validation_rule"),
            @Result(property = "validationHint", column = "validation_hint"),
            @Result(property = "defaultValue", column = "default_value"),
            @Result(property = "sortOrder", column = "sort_order")
    })
    @Select("SELECT id, field_key, field_name, is_enabled, max_length, is_required, validation_rule, validation_hint, default_value, sort_order FROM medical_record_ai_field ORDER BY sort_order, id")
    List<MedicalRecordAIField> selectAll();

    @ResultMap("fieldMap")
    @Select("SELECT id, field_key, field_name, is_enabled, max_length, is_required, validation_rule, validation_hint, default_value, sort_order FROM medical_record_ai_field WHERE is_enabled = 1 ORDER BY sort_order, id")
    List<MedicalRecordAIField> selectAllEnabled();

    @Insert("INSERT INTO medical_record_ai_field (field_key, field_name, is_enabled, max_length, is_required, validation_rule, validation_hint, default_value, sort_order) VALUES (#{fieldKey}, #{fieldName}, #{isEnabled}, #{maxLength}, #{isRequired}, #{validationRule}, #{validationHint}, #{defaultValue}, #{sortOrder}) ON DUPLICATE KEY UPDATE field_name = #{fieldName}, is_enabled = #{isEnabled}, max_length = #{maxLength}, is_required = #{isRequired}, validation_rule = #{validationRule}, validation_hint = #{validationHint}, default_value = #{defaultValue}, sort_order = #{sortOrder}")
    void upsert(MedicalRecordAIField field);
}
