package com.example.springboot.mapper;

import com.example.springboot.entity.MaterialPurchase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialPurchaseMapper {

    @Select("SELECT * FROM material_purchases ORDER BY purchase_date DESC, id DESC")
    List<MaterialPurchase> selectAll();

    @Select("SELECT * FROM material_purchases WHERE id = #{id}")
    MaterialPurchase selectById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT * FROM material_purchases",
            "WHERE 1 = 1",
            "<if test='startDate != null'> AND purchase_date &gt;= #{startDate} </if>",
            "<if test='endDate != null'> AND purchase_date &lt;= #{endDate} </if>",
            "<if test='supplierName != null and supplierName != \"\"'> AND supplier_name LIKE CONCAT('%', #{supplierName}, '%') </if>",
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>",
            "<if test='keyword != null and keyword != \"\"'> AND (supplier_name LIKE CONCAT('%', #{keyword}, '%') OR remark LIKE CONCAT('%', #{keyword}, '%')) </if>",
            "ORDER BY purchase_date DESC, id DESC",
            "</script>"
    })
    List<MaterialPurchase> search(@Param("startDate") String startDate,
                                  @Param("endDate") String endDate,
                                  @Param("supplierName") String supplierName,
                                  @Param("status") String status,
                                  @Param("keyword") String keyword);

    @Insert("INSERT INTO material_purchases(supplier_name, purchase_date, total_amount, payment_method, invoice_image_url, remark, finance_record_id, status, created_by, created_by_name, voided_by, voided_by_name, voided_at) VALUES(#{supplier_name}, #{purchase_date}, #{total_amount}, #{payment_method}, #{invoice_image_url}, #{remark}, #{finance_record_id}, #{status}, #{created_by}, #{created_by_name}, #{voided_by}, #{voided_by_name}, #{voided_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MaterialPurchase item);

    @Update("UPDATE material_purchases SET supplier_name = #{supplier_name}, purchase_date = #{purchase_date}, total_amount = #{total_amount}, payment_method = #{payment_method}, invoice_image_url = #{invoice_image_url}, remark = #{remark}, finance_record_id = #{finance_record_id}, status = #{status}, created_by = #{created_by}, created_by_name = #{created_by_name}, voided_by = #{voided_by}, voided_by_name = #{voided_by_name}, voided_at = #{voided_at} WHERE id = #{id}")
    void update(MaterialPurchase item);
}
