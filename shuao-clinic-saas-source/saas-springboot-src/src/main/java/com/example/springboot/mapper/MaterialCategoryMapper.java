package com.example.springboot.mapper;

import com.example.springboot.entity.MaterialCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialCategoryMapper {

    @Select("SELECT * FROM material_categories ORDER BY sort_order ASC, id ASC")
    List<MaterialCategory> selectAll();

    @Select("SELECT * FROM material_categories WHERE id = #{id}")
    MaterialCategory selectById(@Param("id") Long id);

    @Insert("INSERT INTO material_categories(name, parent_id, sort_order, status) VALUES(#{name}, #{parent_id}, #{sort_order}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MaterialCategory item);

    @Update("UPDATE material_categories SET name = #{name}, parent_id = #{parent_id}, sort_order = #{sort_order}, status = #{status} WHERE id = #{id}")
    void update(MaterialCategory item);

    @Delete("DELETE FROM material_categories WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
