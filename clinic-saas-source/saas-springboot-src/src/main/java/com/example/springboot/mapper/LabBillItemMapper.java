package com.example.springboot.mapper;

import com.example.springboot.entity.LabBillItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabBillItemMapper {

    @Select("SELECT * FROM lab_bill_items WHERE bill_id = #{billId} ORDER BY raw_row_number ASC, id ASC")
    List<LabBillItem> selectByBillId(@Param("billId") Long billId);

    @Select("SELECT * FROM lab_bill_items WHERE id = #{id}")
    LabBillItem selectById(@Param("id") Long id);

    @Insert("INSERT INTO lab_bill_items(bill_id, raw_row_number, product_name, product_spec, quantity, unit_price, total_amount, delivery_date, patient_name, match_status, matched_lab_order_id, resolution_status, resolution_remark, resolved_by, resolved_by_name, resolved_at) VALUES(#{bill_id}, #{raw_row_number}, #{product_name}, #{product_spec}, #{quantity}, #{unit_price}, #{total_amount}, #{delivery_date}, #{patient_name}, #{match_status}, #{matched_lab_order_id}, #{resolution_status}, #{resolution_remark}, #{resolved_by}, #{resolved_by_name}, #{resolved_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabBillItem item);

    @Update("UPDATE lab_bill_items SET match_status = #{match_status}, matched_lab_order_id = #{matched_lab_order_id}, resolution_status = #{resolution_status}, resolution_remark = #{resolution_remark}, resolved_by = #{resolved_by}, resolved_by_name = #{resolved_by_name}, resolved_at = #{resolved_at} WHERE id = #{id}")
    void update(LabBillItem item);
}
