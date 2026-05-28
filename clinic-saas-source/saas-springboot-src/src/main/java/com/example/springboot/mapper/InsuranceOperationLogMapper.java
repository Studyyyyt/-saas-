package com.example.springboot.mapper;

import com.example.springboot.entity.InsuranceOperationLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InsuranceOperationLogMapper {

    @Select("select * from insurance_operation_log order by id desc limit #{limit}")
    List<InsuranceOperationLog> selectRecent(@Param("limit") int limit);

    @Insert("INSERT INTO insurance_operation_log (operation_type, ref_type, ref_id, request_url, request_method, request_payload, response_payload, response_code, response_message, status) VALUES (#{operation_type}, #{ref_type}, #{ref_id}, #{request_url}, #{request_method}, #{request_payload}, #{response_payload}, #{response_code}, #{response_message}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InsuranceOperationLog log);
}
