package com.example.springboot.mapper;

import com.example.springboot.entity.BusinessDailyAnalysis;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Date;
import java.util.List;

@Mapper
public interface BusinessDailyAnalysisMapper {

    @Select("SELECT * FROM business_daily_analysis ORDER BY analysis_date DESC, id DESC LIMIT #{limit}")
    List<BusinessDailyAnalysis> selectRecent(@Param("limit") int limit);

    @Select("SELECT * FROM business_daily_analysis WHERE id = #{id}")
    BusinessDailyAnalysis selectById(@Param("id") Long id);

    @Select("SELECT * FROM business_daily_analysis WHERE analysis_date = #{analysisDate} LIMIT 1")
    BusinessDailyAnalysis selectByAnalysisDate(@Param("analysisDate") Date analysisDate);

    @Select("SELECT * FROM business_daily_analysis ORDER BY analysis_date DESC, id DESC LIMIT 1")
    BusinessDailyAnalysis selectLatest();

    @Insert("INSERT INTO business_daily_analysis (" +
            "analysis_date, analysis_status, source_type, trigger_type, model_name, operating_score, trend, headline, summary, " +
            "metrics_json, analysis_json, raw_response, error_message" +
            ") VALUES (" +
            "#{analysis_date}, #{analysis_status}, #{source_type}, #{trigger_type}, #{model_name}, #{operating_score}, #{trend}, #{headline}, #{summary}, " +
            "#{metrics_json}, #{analysis_json}, #{raw_response}, #{error_message}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BusinessDailyAnalysis analysis);

    @Update("UPDATE business_daily_analysis SET " +
            "analysis_status = #{analysis_status}, source_type = #{source_type}, trigger_type = #{trigger_type}, model_name = #{model_name}, " +
            "operating_score = #{operating_score}, trend = #{trend}, headline = #{headline}, summary = #{summary}, " +
            "metrics_json = #{metrics_json}, analysis_json = #{analysis_json}, raw_response = #{raw_response}, error_message = #{error_message} " +
            "WHERE id = #{id}")
    void update(BusinessDailyAnalysis analysis);
}
