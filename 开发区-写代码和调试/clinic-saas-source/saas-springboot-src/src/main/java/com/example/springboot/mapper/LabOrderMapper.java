package com.example.springboot.mapper;

import com.example.springboot.entity.LabOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabOrderMapper {

    @Select("SELECT * FROM lab_orders ORDER BY order_date DESC, id DESC")
    List<LabOrder> selectAll();

    @Select("SELECT * FROM lab_orders WHERE id = #{id}")
    LabOrder selectById(@Param("id") Long id);

    @Select("SELECT * FROM lab_orders WHERE medical_record_operation_id = #{medicalRecordOperationId} ORDER BY id DESC")
    List<LabOrder> selectByMedicalRecordOperationId(@Param("medicalRecordOperationId") Long medicalRecordOperationId);

    @Insert("INSERT INTO lab_orders(factory_id, factory_name, patient_id, patient_name, treatment_id, medical_record_operation_id, medical_record_id, project_id, project_name, operation_id, operation_name, tooth_positions, product_name, product_spec, unit_price, quantity, total_amount, order_date, expected_delivery_date, actual_delivery_date, status, remark, created_by, created_by_name) VALUES(#{factory_id}, #{factory_name}, #{patient_id}, #{patient_name}, #{treatment_id}, #{medical_record_operation_id}, #{medical_record_id}, #{project_id}, #{project_name}, #{operation_id}, #{operation_name}, #{tooth_positions}, #{product_name}, #{product_spec}, #{unit_price}, #{quantity}, #{total_amount}, #{order_date}, #{expected_delivery_date}, #{actual_delivery_date}, #{status}, #{remark}, #{created_by}, #{created_by_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabOrder item);

    @Update("UPDATE lab_orders SET factory_id = #{factory_id}, factory_name = #{factory_name}, patient_id = #{patient_id}, patient_name = #{patient_name}, treatment_id = #{treatment_id}, medical_record_operation_id = #{medical_record_operation_id}, medical_record_id = #{medical_record_id}, project_id = #{project_id}, project_name = #{project_name}, operation_id = #{operation_id}, operation_name = #{operation_name}, tooth_positions = #{tooth_positions}, product_name = #{product_name}, product_spec = #{product_spec}, unit_price = #{unit_price}, quantity = #{quantity}, total_amount = #{total_amount}, order_date = #{order_date}, expected_delivery_date = #{expected_delivery_date}, actual_delivery_date = #{actual_delivery_date}, status = #{status}, remark = #{remark}, created_by = #{created_by}, created_by_name = #{created_by_name} WHERE id = #{id}")
    void update(LabOrder item);

    @Update("UPDATE lab_orders SET status = #{status}, actual_delivery_date = #{actualDeliveryDate} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("actualDeliveryDate") java.util.Date actualDeliveryDate);

    @Delete("DELETE FROM lab_orders WHERE id = #{id}")
    void delete(@Param("id") Long id);

    @Delete("DELETE FROM lab_orders WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);

    @Update("UPDATE lab_orders SET patient_name = #{patientName} WHERE patient_id = #{patientId}")
    void updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName);

    /**
     * 带过滤条件的义齿加工订单搜索
     *
     * @param startDate  下单开始日期（yyyy-MM-dd）
     * @param endDate    下单结束日期（yyyy-MM-dd）
     * @param status     订单状态
     * @param factoryId  加工厂 ID
     * @param keyword    模糊匹配关键词（患者姓名、订单编号/ID、加工项目、产品名称）
     * @return 符合条件的订单列表，按下单日期倒序
     */
    @Select("<script>" +
            "SELECT * FROM lab_orders " +
            "<where>" +
            "  <if test='startDate != null and startDate != \"\"'>" +
            "    AND DATE(order_date) &gt;= #{startDate} " +
            "  </if>" +
            "  <if test='endDate != null and endDate != \"\"'>" +
            "    AND DATE(order_date) &lt;= #{endDate} " +
            "  </if>" +
            "  <if test='status != null and status != \"\"'>" +
            "    AND status = #{status} " +
            "  </if>" +
            "  <if test='factoryId != null'>" +
            "    AND factory_id = #{factoryId} " +
            "  </if>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (" +
            "      patient_name LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR CAST(id AS CHAR) LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR project_name LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR product_name LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR operation_name LIKE CONCAT('%', #{keyword}, '%') " +
            "    )" +
            "  </if>" +
            "</where>" +
            "ORDER BY order_date DESC, id DESC" +
            "</script>")
    List<LabOrder> search(@Param("startDate") String startDate,
                          @Param("endDate") String endDate,
                          @Param("status") String status,
                          @Param("factoryId") Long factoryId,
                          @Param("keyword") String keyword);
}
