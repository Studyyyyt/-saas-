package com.example.springboot.mapper;


import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.PatientArrearsSummary;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreatmentMapper {

    @Select("SELECT * FROM treatment WHERE patient_id = #{patientId} ORDER BY treatment_date DESC, id DESC LIMIT #{limit}")
    List<Treatment> selectRecentByPatientId(@Param("patientId") Long patientId, @Param("limit") int limit);

    @Select("SELECT * FROM treatment WHERE patient_id = #{patientId} ORDER BY treatment_date DESC, id DESC")
    List<Treatment> selectByPatientId(@Param("patientId") Long patientId);

    default List<Treatment> selectByPatientReference(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return List.of();
        }
        return selectByPatientId(patientId);
    }

    @Select("select * from treatment order by treatment_date desc, id desc")
    List<Treatment> selectAll();

    @Select("SELECT * FROM treatment WHERE patient_name LIKE CONCAT('%', #{patientName}, '%') ORDER BY treatment_date DESC, id DESC")
    List<Treatment> selectByPatientName(@Param("patientName") String patientName);

    @Select("select * from treatment where id = #{id}")
    List<Treatment> selectById(@Param("id") Long id);

    @Select("select * from treatment where batch_no = #{batchNo} order by id asc")
    List<Treatment> selectByBatchNo(@Param("batchNo") String batchNo);

    @Select("select * from treatment where patient_name LIKE CONCAT('%', #{name}, '%') order by treatment_date desc, id desc")
    List<Treatment> selectByName(@Param("name") String name);

    @Select({
            "<script>",
            "SELECT x.patient_id, ROUND(SUM(x.arrears_amount), 2) AS arrears_amount",
            "FROM (",
            "  SELECT p_exist.id AS patient_id,",
            "         CASE",
            "           WHEN TRIM(COALESCE(t.status, '')) IN ('取消', '已取消') THEN 0",
            "           WHEN COALESCE(CAST(NULLIF(TRIM(t.treatment_fee), '') AS DECIMAL(18, 2)), 0) &lt;= 0 THEN 0",
            "           ELSE GREATEST(",
            "             ROUND(COALESCE(CAST(NULLIF(TRIM(t.treatment_fee), '') AS DECIMAL(18, 2)), 0), 2)",
            "             - GREATEST(ROUND(COALESCE(fin.charge_amount, 0), 2) - ROUND(COALESCE(fin.refund_amount, 0), 2), 0),",
            "             0",
            "           )",
            "         END AS arrears_amount",
            "  FROM treatment t",
            "  LEFT JOIN patients p_exist ON p_exist.id = t.patient_id",
            "  LEFT JOIN (",
            "      SELECT f.treatment_id,",
            "             SUM(CASE",
            "                   WHEN f.amount &gt; 0",
            "                    AND (UPPER(TRIM(COALESCE(f.biz_type, ''))) = 'TREATMENT_CHARGE'",
            "                      OR (UPPER(TRIM(COALESCE(f.biz_type, ''))) &lt;&gt; 'TREATMENT_REFUND'",
            "                       AND (TRIM(COALESCE(f.type, '')) LIKE '%收入%' OR TRIM(COALESCE(f.type, '')) LIKE '%收费%')))",
            "                   THEN f.amount ELSE 0 END) AS charge_amount,",
            "             SUM(CASE",
            "                   WHEN f.amount &gt; 0",
            "                    AND (UPPER(TRIM(COALESCE(f.biz_type, ''))) = 'TREATMENT_REFUND'",
            "                      OR (UPPER(TRIM(COALESCE(f.biz_type, ''))) &lt;&gt; 'TREATMENT_CHARGE'",
            "                       AND TRIM(COALESCE(f.type, '')) LIKE '%退款%'))",
            "                   THEN f.amount ELSE 0 END) AS refund_amount",
            "      FROM finances f",
            "      WHERE f.treatment_id IS NOT NULL AND f.treatment_id &gt; 0",
            "      GROUP BY f.treatment_id",
            "  ) fin ON fin.treatment_id = t.id",
            ") x",
            "WHERE x.patient_id IS NOT NULL",
            "<if test='patientIds != null and patientIds.size() &gt; 0'>",
            "AND x.patient_id IN ",
            "<foreach item='patientId' collection='patientIds' open='(' separator=',' close=')'>#{patientId}</foreach>",
            "</if>",
            "GROUP BY x.patient_id",
            "</script>"
    })
    List<PatientArrearsSummary> selectPatientArrearsByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Insert("INSERT INTO treatment (patient_id, patient_name, batch_no, medical_record_id, project_id, appointment_purpose, status, doctor_account_id, doctor_name, treatment_date, treatment_content, tooth_positions, treatment_product, treatment_fee) " +
            "VALUES (#{patient_id}, #{patient_name}, #{batch_no}, #{medical_record_id}, #{project_id}, #{appointment_purpose}, #{status}, #{doctor_account_id}, #{doctor_name}, #{treatment_date}, #{treatment_content}, #{tooth_positions}, #{treatment_product}, #{treatment_fee})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addTreatment(Treatment treatment);

    @Update("UPDATE treatment SET patient_id = #{patient_id}, patient_name = #{patient_name}, batch_no = #{batch_no}, medical_record_id = #{medical_record_id}, project_id = #{project_id}, appointment_purpose = #{appointment_purpose}, status = #{status}, doctor_account_id = #{doctor_account_id}, doctor_name = #{doctor_name}, treatment_date = #{treatment_date}, treatment_content = #{treatment_content}, tooth_positions = #{tooth_positions}, treatment_product = #{treatment_product}, treatment_fee = #{treatment_fee} WHERE id = #{id}")
    void editTreatment(Treatment treatment);

    @Delete("DELETE FROM treatment WHERE id = #{id}")
    void deleteTreatment(@Param("id") Long id);

    @Delete("DELETE FROM treatment WHERE patient_id = #{patientId}")
    void deleteByPatientReference(@Param("patientId") Long patientId);

    @Update("UPDATE treatment SET patient_name = #{patientName} WHERE patient_id = #{patientId}")
    void updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName);
}
