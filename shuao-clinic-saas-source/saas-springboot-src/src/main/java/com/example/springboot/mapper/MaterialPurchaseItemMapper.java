package com.example.springboot.mapper;

import com.example.springboot.entity.MaterialPurchaseItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialPurchaseItemMapper {

    @Select("SELECT * FROM material_purchase_items WHERE purchase_id = #{purchaseId} ORDER BY id ASC")
    List<MaterialPurchaseItem> selectByPurchaseId(@Param("purchaseId") Long purchaseId);

    @Insert("INSERT INTO material_purchase_items(purchase_id, material_id, material_name, material_spec, unit_price, quantity, subtotal) VALUES(#{purchase_id}, #{material_id}, #{material_name}, #{material_spec}, #{unit_price}, #{quantity}, #{subtotal})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MaterialPurchaseItem item);

    @Delete("DELETE FROM material_purchase_items WHERE purchase_id = #{purchaseId}")
    void deleteByPurchaseId(@Param("purchaseId") Long purchaseId);
}
