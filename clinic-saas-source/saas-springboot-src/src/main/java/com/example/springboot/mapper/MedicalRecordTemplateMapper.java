package com.example.springboot.mapper;

import com.example.springboot.entity.MedicalRecordTemplate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MedicalRecordTemplateMapper {

    String BASE_COLUMNS = "id, template_name, template_category, chief_complaint, present_illness_history, past_history, infectious_history, allergy_history, general_condition, examination, auxiliary_examination, diagnosis, treatment_plan, treatment, tooth_positions, medical_advice, prescription, record_tags, image_summary, notes, record_type, operation_items_json, status, created_by, created_by_name, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from medical_record_template order by status desc, updated_at desc, id desc")
    List<MedicalRecordTemplate> selectAll();

    @Select("select " + BASE_COLUMNS + " from medical_record_template where status = 1 order by updated_at desc, id desc")
    List<MedicalRecordTemplate> selectEnabled();

    @Select("select " + BASE_COLUMNS + " from medical_record_template where id = #{id} limit 1")
    MedicalRecordTemplate selectById(@Param("id") Long id);

    @Insert("insert into medical_record_template (template_name, template_category, chief_complaint, present_illness_history, past_history, infectious_history, allergy_history, general_condition, examination, auxiliary_examination, diagnosis, treatment_plan, treatment, tooth_positions, medical_advice, prescription, record_tags, image_summary, notes, record_type, operation_items_json, status, created_by, created_by_name) " +
            "values (#{template_name}, #{template_category}, #{chief_complaint}, #{present_illness_history}, #{past_history}, #{infectious_history}, #{allergy_history}, #{general_condition}, #{examination}, #{auxiliary_examination}, #{diagnosis}, #{treatment_plan}, #{treatment}, #{tooth_positions}, #{medical_advice}, #{prescription}, #{record_tags}, #{image_summary}, #{notes}, #{record_type}, #{operation_items_json}, #{status}, #{created_by}, #{created_by_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(MedicalRecordTemplate item);

    @Update("update medical_record_template set template_name=#{template_name}, template_category=#{template_category}, chief_complaint=#{chief_complaint}, present_illness_history=#{present_illness_history}, past_history=#{past_history}, infectious_history=#{infectious_history}, allergy_history=#{allergy_history}, general_condition=#{general_condition}, examination=#{examination}, auxiliary_examination=#{auxiliary_examination}, diagnosis=#{diagnosis}, treatment_plan=#{treatment_plan}, treatment=#{treatment}, " +
            "tooth_positions=#{tooth_positions}, medical_advice=#{medical_advice}, prescription=#{prescription}, record_tags=#{record_tags}, image_summary=#{image_summary}, notes=#{notes}, record_type=#{record_type}, operation_items_json=#{operation_items_json}, status=#{status}, created_by=#{created_by}, created_by_name=#{created_by_name} where id=#{id}")
    void edit(MedicalRecordTemplate item);

    @Delete("delete from medical_record_template where id = #{id}")
    void delete(@Param("id") Long id);
}
