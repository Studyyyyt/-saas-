package com.example.springboot.mapper;

import com.example.springboot.entity.TreatmentProjectCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreatmentProjectCategoryMapper {

    String BASE_COLUMNS = "id, name, parent_id, sort_order, status, created_by, created_by_name, updated_by, updated_by_name, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from treatment_project_categories order by sort_order asc, id asc")
    List<TreatmentProjectCategory> selectAll();

    @Select("select " + BASE_COLUMNS + " from treatment_project_categories where id = #{id} limit 1")
    TreatmentProjectCategory selectById(@Param("id") Long id);

    @Insert("INSERT INTO treatment_project_categories(name, parent_id, sort_order, status, created_by, created_by_name, updated_by, updated_by_name) " +
            "VALUES(#{name}, #{parent_id}, #{sort_order}, #{status}, #{created_by}, #{created_by_name}, #{updated_by}, #{updated_by_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TreatmentProjectCategory item);

    @Update("UPDATE treatment_project_categories SET name = #{name}, parent_id = #{parent_id}, sort_order = #{sort_order}, status = #{status}, " +
            "updated_by = #{updated_by}, updated_by_name = #{updated_by_name} WHERE id = #{id}")
    void update(TreatmentProjectCategory item);

    @Delete("DELETE FROM treatment_project_categories WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
