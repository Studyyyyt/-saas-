package com.example.springboot.mapper;

import com.example.springboot.entity.LabBillTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabBillTemplateMapper {

    @Select("SELECT * FROM lab_bill_templates WHERE factory_id = #{factoryId} ORDER BY updated_at DESC, id DESC")
    List<LabBillTemplate> selectByFactoryId(@Param("factoryId") Long factoryId);

    @Select("SELECT * FROM lab_bill_templates WHERE id = #{id}")
    LabBillTemplate selectById(@Param("id") Long id);

    @Insert("INSERT INTO lab_bill_templates(factory_id, template_name, column_mapping, header_row, data_start_row) VALUES(#{factory_id}, #{template_name}, #{column_mapping}, #{header_row}, #{data_start_row})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabBillTemplate item);

    @Update("UPDATE lab_bill_templates SET template_name = #{template_name}, column_mapping = #{column_mapping}, header_row = #{header_row}, data_start_row = #{data_start_row} WHERE id = #{id}")
    void update(LabBillTemplate item);

    @Delete("DELETE FROM lab_bill_templates WHERE id = #{id}")
    void delete(@Param("id") Long id);

    @Delete("DELETE FROM lab_bill_templates WHERE factory_id = #{factoryId}")
    void deleteByFactoryId(@Param("factoryId") Long factoryId);
}
