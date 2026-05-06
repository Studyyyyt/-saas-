package com.example.springboot.mapper;

import com.example.springboot.entity.TreatmentProject;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreatmentProjectMapper {

    String BASE_COLUMNS = "id, legacy_treatment_catalog_id, project_code, project_name, category_id, category_path, default_price, " +
            "estimated_visit_count, estimated_cycle_days, status, sort_order, remark, created_by, created_by_name, updated_by, updated_by_name, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from treatment_projects order by sort_order asc, id desc")
    List<TreatmentProject> selectAll();

    @Select("select " + BASE_COLUMNS + " from treatment_projects where status = '在用' order by sort_order asc, id desc")
    List<TreatmentProject> selectEnabled();

    @Select("select " + BASE_COLUMNS + " from treatment_projects where id = #{id} limit 1")
    TreatmentProject selectById(@Param("id") Long id);

    @Insert("INSERT INTO treatment_projects(legacy_treatment_catalog_id, project_code, project_name, category_id, category_path, default_price, estimated_visit_count, estimated_cycle_days, status, sort_order, remark, created_by, created_by_name, updated_by, updated_by_name) " +
            "VALUES(#{legacy_treatment_catalog_id}, #{project_code}, #{project_name}, #{category_id}, #{category_path}, #{default_price}, #{estimated_visit_count}, #{estimated_cycle_days}, #{status}, #{sort_order}, #{remark}, #{created_by}, #{created_by_name}, #{updated_by}, #{updated_by_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TreatmentProject item);

    @Update("UPDATE treatment_projects SET legacy_treatment_catalog_id = #{legacy_treatment_catalog_id}, project_code = #{project_code}, project_name = #{project_name}, " +
            "category_id = #{category_id}, category_path = #{category_path}, default_price = #{default_price}, estimated_visit_count = #{estimated_visit_count}, " +
            "estimated_cycle_days = #{estimated_cycle_days}, status = #{status}, sort_order = #{sort_order}, remark = #{remark}, " +
            "updated_by = #{updated_by}, updated_by_name = #{updated_by_name} WHERE id = #{id}")
    void update(TreatmentProject item);

    @Delete("DELETE FROM treatment_projects WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
