package com.example.springboot.mapper;

import com.example.springboot.entity.InsurancePatientProfile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InsurancePatientProfileMapper {

    @Select("select * from insurance_patient_profile where patient_id = #{patientId} limit 1")
    InsurancePatientProfile selectByPatientId(@Param("patientId") Long patientId);

    @Select("select * from insurance_patient_profile order by id desc")
    List<InsurancePatientProfile> selectAll();

    @Select("select count(*) from insurance_patient_profile")
    int countAll();

    @Insert("INSERT INTO insurance_patient_profile (patient_id, insurance_person_no, id_card_no, insured_region_code, insured_type, card_no, card_type, person_name, gender, birthday, phone, status, last_auth_no, last_verified_at, ext_json) VALUES (#{patient_id}, #{insurance_person_no}, #{id_card_no}, #{insured_region_code}, #{insured_type}, #{card_no}, #{card_type}, #{person_name}, #{gender}, #{birthday}, #{phone}, #{status}, #{last_auth_no}, #{last_verified_at}, #{ext_json})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InsurancePatientProfile profile);

    @Update("UPDATE insurance_patient_profile SET insurance_person_no=#{insurance_person_no}, id_card_no=#{id_card_no}, insured_region_code=#{insured_region_code}, insured_type=#{insured_type}, card_no=#{card_no}, card_type=#{card_type}, person_name=#{person_name}, gender=#{gender}, birthday=#{birthday}, phone=#{phone}, status=#{status}, last_auth_no=#{last_auth_no}, last_verified_at=#{last_verified_at}, ext_json=#{ext_json} WHERE id=#{id}")
    void update(InsurancePatientProfile profile);
}
