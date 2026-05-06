package com.example.springboot.mapper;

import com.example.springboot.entity.BusinessPeriodReport;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BusinessPeriodReportMapper {

    @Select("SELECT * FROM business_period_report WHERE report_type = #{reportType} AND period_key = #{periodKey} LIMIT 1")
    BusinessPeriodReport selectByTypeAndPeriodKey(@Param("reportType") String reportType, @Param("periodKey") String periodKey);

    @Select("SELECT * FROM business_period_report WHERE report_type = #{reportType} ORDER BY period_end DESC, id DESC LIMIT 1")
    BusinessPeriodReport selectLatestByType(@Param("reportType") String reportType);

    @Select("SELECT * FROM business_period_report WHERE report_type = #{reportType} ORDER BY period_end DESC, id DESC LIMIT #{limit}")
    List<BusinessPeriodReport> selectRecentByType(@Param("reportType") String reportType, @Param("limit") int limit);

    @Insert("INSERT INTO business_period_report (" +
            "report_type, period_key, period_start, period_end, report_status, source_type, trigger_type, model_name, operating_score, trend, " +
            "headline, summary, metrics_json, analysis_json, raw_response, error_message" +
            ") VALUES (" +
            "#{report_type}, #{period_key}, #{period_start}, #{period_end}, #{report_status}, #{source_type}, #{trigger_type}, #{model_name}, #{operating_score}, #{trend}, " +
            "#{headline}, #{summary}, #{metrics_json}, #{analysis_json}, #{raw_response}, #{error_message}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BusinessPeriodReport report);

    @Update("UPDATE business_period_report SET " +
            "period_start = #{period_start}, period_end = #{period_end}, report_status = #{report_status}, source_type = #{source_type}, trigger_type = #{trigger_type}, " +
            "model_name = #{model_name}, operating_score = #{operating_score}, trend = #{trend}, headline = #{headline}, summary = #{summary}, " +
            "metrics_json = #{metrics_json}, analysis_json = #{analysis_json}, raw_response = #{raw_response}, error_message = #{error_message} " +
            "WHERE id = #{id}")
    void update(BusinessPeriodReport report);
}
