package com.example.springboot.mapper;

import com.example.springboot.entity.TreatmentCatalog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreatmentCatalogMapper {

    String BASE_COLUMNS = "id, item_name, default_fee, default_content, default_product, status, sort_order, medical_insurance_code, medical_insurance_name, medical_insurance_category, self_pay_ratio, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from treatment_catalog order by status desc, sort_order asc, id desc")
    List<TreatmentCatalog> selectAll();

    @Select("select " + BASE_COLUMNS + " from treatment_catalog where status = 1 order by sort_order asc, id desc")
    List<TreatmentCatalog> selectEnabled();

    @Select("select " + BASE_COLUMNS + " from treatment_catalog where id = #{id} limit 1")
    TreatmentCatalog selectById(@Param("id") Long id);

    @Insert("INSERT INTO treatment_catalog (item_name, default_fee, default_content, default_product, status, sort_order) VALUES (#{item_name}, #{default_fee}, #{default_content}, #{default_product}, #{status}, #{sort_order})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(TreatmentCatalog item);

    @Update("UPDATE treatment_catalog SET item_name=#{item_name}, default_fee=#{default_fee}, default_content=#{default_content}, default_product=#{default_product}, status=#{status}, sort_order=#{sort_order} WHERE id=#{id}")
    void edit(TreatmentCatalog item);

    @Delete("DELETE FROM treatment_catalog WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
