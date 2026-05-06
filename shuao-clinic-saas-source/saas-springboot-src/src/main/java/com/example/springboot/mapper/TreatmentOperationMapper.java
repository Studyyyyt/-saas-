package com.example.springboot.mapper;

import com.example.springboot.entity.TreatmentOperation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreatmentOperationMapper {

    String BASE_COLUMNS = "id, operation_code, operation_name, operation_category, need_lab_processing, default_processing_days, status, sort_order, remark, " +
            "created_by, created_by_name, updated_by, updated_by_name, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from treatment_operations order by sort_order asc, id desc")
    List<TreatmentOperation> selectAll();

    @Select("select " + BASE_COLUMNS + " from treatment_operations where status = '在用' order by sort_order asc, id desc")
    List<TreatmentOperation> selectEnabled();

    @Select("select " + BASE_COLUMNS + " from treatment_operations where id = #{id} limit 1")
    TreatmentOperation selectById(@Param("id") Long id);

    @Insert("INSERT INTO treatment_operations(operation_code, operation_name, operation_category, need_lab_processing, default_processing_days, status, sort_order, remark, created_by, created_by_name, updated_by, updated_by_name) " +
            "VALUES(#{operation_code}, #{operation_name}, #{operation_category}, #{need_lab_processing}, #{default_processing_days}, #{status}, #{sort_order}, #{remark}, #{created_by}, #{created_by_name}, #{updated_by}, #{updated_by_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TreatmentOperation item);

    @Update("UPDATE treatment_operations SET operation_code = #{operation_code}, operation_name = #{operation_name}, operation_category = #{operation_category}, " +
            "need_lab_processing = #{need_lab_processing}, default_processing_days = #{default_processing_days}, status = #{status}, sort_order = #{sort_order}, " +
            "remark = #{remark}, updated_by = #{updated_by}, updated_by_name = #{updated_by_name} WHERE id = #{id}")
    void update(TreatmentOperation item);

    @Delete("DELETE FROM treatment_operations WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
