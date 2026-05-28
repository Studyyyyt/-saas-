package com.example.springboot.mapper;

import com.example.springboot.entity.LabFactoryProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabFactoryProductMapper {

    @Select("SELECT * FROM lab_factory_products WHERE factory_id = #{factoryId} ORDER BY status DESC, product_name ASC, id DESC")
    List<LabFactoryProduct> selectByFactoryId(@Param("factoryId") Long factoryId);

    @Select("SELECT * FROM lab_factory_products WHERE factory_id = #{factoryId} AND status = '启用' ORDER BY product_name ASC, id DESC")
    List<LabFactoryProduct> selectEnabledByFactoryId(@Param("factoryId") Long factoryId);

    @Select("SELECT * FROM lab_factory_products WHERE id = #{id}")
    LabFactoryProduct selectById(@Param("id") Long id);

    @Insert("INSERT INTO lab_factory_products(factory_id, product_name, product_spec, unit_price, unit, status) VALUES(#{factory_id}, #{product_name}, #{product_spec}, #{unit_price}, #{unit}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabFactoryProduct item);

    @Update("UPDATE lab_factory_products SET product_name = #{product_name}, product_spec = #{product_spec}, unit_price = #{unit_price}, unit = #{unit}, status = #{status} WHERE id = #{id}")
    void update(LabFactoryProduct item);

    @Delete("DELETE FROM lab_factory_products WHERE id = #{id}")
    void delete(@Param("id") Long id);

    @Delete("DELETE FROM lab_factory_products WHERE factory_id = #{factoryId}")
    void deleteByFactoryId(@Param("factoryId") Long factoryId);

    @Select("SELECT * FROM lab_factory_products ORDER BY id DESC")
    List<LabFactoryProduct> selectAll();
}
