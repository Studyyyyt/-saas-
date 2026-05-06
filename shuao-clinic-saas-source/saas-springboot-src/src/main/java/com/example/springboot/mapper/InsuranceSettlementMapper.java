package com.example.springboot.mapper;

import com.example.springboot.entity.InsuranceSettlement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InsuranceSettlementMapper {

    @Select("select * from insurance_settlement order by id desc")
    List<InsuranceSettlement> selectAll();

    @Select("select * from insurance_settlement where patient_id = #{patientId} order by id desc")
    List<InsuranceSettlement> selectByPatientId(@Param("patientId") Long patientId);

    @Select("select count(*) from insurance_settlement")
    int countAll();

    @Select("select count(*) from insurance_settlement where settlement_status = #{status}")
    int countByStatus(@Param("status") String status);

    @Insert("INSERT INTO insurance_settlement (patient_id, finance_id, treatment_id, settlement_no, visit_no, biz_type, settlement_status, total_amount, insurance_amount, personal_amount, cash_amount, upload_status, upload_payload, response_payload, remark, settlement_time) VALUES (#{patient_id}, #{finance_id}, #{treatment_id}, #{settlement_no}, #{visit_no}, #{biz_type}, #{settlement_status}, #{total_amount}, #{insurance_amount}, #{personal_amount}, #{cash_amount}, #{upload_status}, #{upload_payload}, #{response_payload}, #{remark}, #{settlement_time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InsuranceSettlement settlement);

    @Update("UPDATE insurance_settlement SET settlement_no=#{settlement_no}, visit_no=#{visit_no}, biz_type=#{biz_type}, settlement_status=#{settlement_status}, total_amount=#{total_amount}, insurance_amount=#{insurance_amount}, personal_amount=#{personal_amount}, cash_amount=#{cash_amount}, upload_status=#{upload_status}, upload_payload=#{upload_payload}, response_payload=#{response_payload}, remark=#{remark}, settlement_time=#{settlement_time} WHERE id=#{id}")
    void update(InsuranceSettlement settlement);
}
