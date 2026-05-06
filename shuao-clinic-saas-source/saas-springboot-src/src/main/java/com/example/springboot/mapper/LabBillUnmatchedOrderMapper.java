package com.example.springboot.mapper;

import com.example.springboot.entity.LabBillUnmatchedOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabBillUnmatchedOrderMapper {

    @Select("SELECT * FROM lab_bill_unmatched_orders WHERE bill_id = #{billId} ORDER BY id ASC")
    List<LabBillUnmatchedOrder> selectByBillId(@Param("billId") Long billId);

    @Select("SELECT * FROM lab_bill_unmatched_orders WHERE id = #{id}")
    LabBillUnmatchedOrder selectById(@Param("id") Long id);

    @Insert("INSERT INTO lab_bill_unmatched_orders(bill_id, lab_order_id, resolution_status, resolution_remark, resolved_by, resolved_by_name, resolved_at) VALUES(#{bill_id}, #{lab_order_id}, #{resolution_status}, #{resolution_remark}, #{resolved_by}, #{resolved_by_name}, #{resolved_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabBillUnmatchedOrder item);

    @Update("UPDATE lab_bill_unmatched_orders SET resolution_status = #{resolution_status}, resolution_remark = #{resolution_remark}, resolved_by = #{resolved_by}, resolved_by_name = #{resolved_by_name}, resolved_at = #{resolved_at} WHERE id = #{id}")
    void update(LabBillUnmatchedOrder item);

    @Delete("DELETE FROM lab_bill_unmatched_orders WHERE bill_id = #{billId}")
    void deleteByBillId(@Param("billId") Long billId);
}
