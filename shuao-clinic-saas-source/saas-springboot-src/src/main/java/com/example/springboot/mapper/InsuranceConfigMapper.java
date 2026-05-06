package com.example.springboot.mapper;

import com.example.springboot.entity.InsuranceConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface InsuranceConfigMapper {

    @Select("select * from insurance_config order by enabled desc, id asc limit 1")
    InsuranceConfig selectPrimary();

    @Insert("INSERT INTO insurance_config (platform_code, platform_name, api_base_url, org_code, org_name, app_id, app_secret, sign_key, encryption_type, region_code, enabled, ext_json) VALUES (#{platform_code}, #{platform_name}, #{api_base_url}, #{org_code}, #{org_name}, #{app_id}, #{app_secret}, #{sign_key}, #{encryption_type}, #{region_code}, #{enabled}, #{ext_json})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InsuranceConfig config);

    @Update("UPDATE insurance_config SET platform_code=#{platform_code}, platform_name=#{platform_name}, api_base_url=#{api_base_url}, org_code=#{org_code}, org_name=#{org_name}, app_id=#{app_id}, app_secret=#{app_secret}, sign_key=#{sign_key}, encryption_type=#{encryption_type}, region_code=#{region_code}, enabled=#{enabled}, ext_json=#{ext_json} WHERE id=#{id}")
    void update(InsuranceConfig config);
}
