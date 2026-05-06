package com.example.springboot.mapper;

import com.example.springboot.entity.LabBill;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabBillMapper {

    @Select("SELECT * FROM lab_bills ORDER BY imported_at DESC, id DESC")
    List<LabBill> selectAll();

    @Select("SELECT * FROM lab_bills WHERE id = #{id}")
    LabBill selectById(@Param("id") Long id);

    @Select("SELECT * FROM lab_bills WHERE factory_id = #{factoryId} AND bill_month = #{billMonth} LIMIT 1")
    LabBill selectByFactoryIdAndMonth(@Param("factoryId") Long factoryId, @Param("billMonth") String billMonth);

    @Insert("INSERT INTO lab_bills(factory_id, factory_name, template_id, bill_month, total_amount, bill_file_url, status, matched_count, mismatched_count, only_in_system_count, only_in_bill_count, imported_by, imported_by_name, imported_at, confirmed_by, confirmed_by_name, confirmed_at) VALUES(#{factory_id}, #{factory_name}, #{template_id}, #{bill_month}, #{total_amount}, #{bill_file_url}, #{status}, #{matched_count}, #{mismatched_count}, #{only_in_system_count}, #{only_in_bill_count}, #{imported_by}, #{imported_by_name}, #{imported_at}, #{confirmed_by}, #{confirmed_by_name}, #{confirmed_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabBill item);

    @Update("UPDATE lab_bills SET total_amount = #{total_amount}, bill_file_url = #{bill_file_url}, status = #{status}, matched_count = #{matched_count}, mismatched_count = #{mismatched_count}, only_in_system_count = #{only_in_system_count}, only_in_bill_count = #{only_in_bill_count}, confirmed_by = #{confirmed_by}, confirmed_by_name = #{confirmed_by_name}, confirmed_at = #{confirmed_at} WHERE id = #{id}")
    void update(LabBill item);
}
