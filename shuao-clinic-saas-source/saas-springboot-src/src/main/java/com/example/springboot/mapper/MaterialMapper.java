package com.example.springboot.mapper;

import com.example.springboot.entity.Material;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialMapper {

    @Select("SELECT * FROM materials ORDER BY updated_at DESC, id DESC")
    List<Material> selectAll();

    @Select("SELECT * FROM materials WHERE id = #{id}")
    Material selectById(@Param("id") Long id);

    @Insert("INSERT INTO materials(name, spec, brand, category_id, category_name, unit, min_stock_alert, current_stock, status, remark) VALUES(#{name}, #{spec}, #{brand}, #{category_id}, #{category_name}, #{unit}, #{min_stock_alert}, #{current_stock}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Material item);

    @Update("UPDATE materials SET name = #{name}, spec = #{spec}, brand = #{brand}, category_id = #{category_id}, category_name = #{category_name}, unit = #{unit}, min_stock_alert = #{min_stock_alert}, current_stock = #{current_stock}, status = #{status}, remark = #{remark} WHERE id = #{id}")
    void update(Material item);

    @Delete("DELETE FROM materials WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
