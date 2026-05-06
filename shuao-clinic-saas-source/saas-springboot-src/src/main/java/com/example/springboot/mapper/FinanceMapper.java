package com.example.springboot.mapper;


import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Inventory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FinanceMapper {

    @Select("SELECT * FROM finances")
    List<Finance> getAllFinances();

    @Select("SELECT * FROM finances WHERE treatment_id = #{treatmentId} ORDER BY id ASC")
    List<Finance> getFinancesByTreatmentId(@Param("treatmentId") Long treatmentId);

    @Select({
            "<script>",
            "SELECT * FROM finances",
            "WHERE treatment_id IN ",
            "<foreach item='treatmentId' collection='treatmentIds' open='(' separator=',' close=')'>#{treatmentId}</foreach>",
            "ORDER BY treatment_id ASC, id ASC",
            "</script>"
    })
    List<Finance> selectByTreatmentIds(@Param("treatmentIds") List<Long> treatmentIds);

    @Select("SELECT * FROM finances WHERE patient_id = #{patientId} ORDER BY date DESC, id DESC LIMIT #{limit}")
    List<Finance> getRecentFinancesByPatientId(@Param("patientId") Long patientId, @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT * FROM finances WHERE patient_id IN ",
            "<foreach item='patientId' collection='patientIds' open='(' separator=',' close=')'>#{patientId}</foreach>",
            "</script>"
    })
    List<Finance> selectByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Select("SELECT * FROM finances WHERE id = #{id}")
    List<Finance> getFinanceByid(@Param("id") Long id);

    @Select("select * from finances where name = #{name}")
    List<Finance> getFinanceByname(String name);

    @Select("select * from finances where amount = #{amount}")
    List<Finance> getFinanceByamount(double amount);

    @Select("select * from finances where type = #{type}")
    List<Finance> getFinanceBytype(String type);

    @Select("select * from finances where date = #{date}")
    List<Finance> getFinanceBydate(String date);


    @Insert("INSERT INTO finances(patient_id, treatment_id, payment_channel_id, payment_channel_name, name, amount, date, type, biz_type, remark) " +
            "VALUES(#{patient_id}, #{treatment_id}, #{payment_channel_id}, #{payment_channel_name}, #{name}, #{amount}, #{date}, #{type}, #{biz_type}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addFinance(Finance finance);

    @Update("update finances SET patient_id = #{patient_id}, treatment_id = #{treatment_id}, payment_channel_id = #{payment_channel_id}, payment_channel_name = #{payment_channel_name}, " +
            "name = #{name}, amount = #{amount}, date = #{date}, type = #{type}, biz_type = #{biz_type}, remark = #{remark} WHERE id = #{id}")
    void editFinance(Finance finance);

    @Update("UPDATE finances SET patient_id = #{patient_id}, treatment_id = #{treatment_id}, payment_channel_id = #{payment_channel_id}, payment_channel_name = #{payment_channel_name}, " +
            "name = #{name}, amount = #{amount}, date = #{date}, type = #{type}, biz_type = #{biz_type}, remark = #{remark} WHERE id = #{id}")
    void updateFinance(Finance finance);

    @Delete("DELETE FROM finances WHERE id = #{id}")
    void deleteFinance(int id);

    @Delete("DELETE FROM finances WHERE treatment_id = #{treatmentId}")
    void deleteByTreatmentId(@Param("treatmentId") Long treatmentId);

    @Delete("DELETE FROM finances WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);

//    @Select("SELECT * FROM finances WHERE SUBSTRING(date, 1, 4) = #{year} AND SUBSTRING(date, 6, 2) = #{month}")
//    List<Finance> getFinanceByYearMonth(int year, int month);
    @Select("SELECT * FROM finances WHERE MONTH(date) = #{month} AND YEAR(date) = #{year}")
    List<Finance> getFinancesByMonth(Integer year, Integer month);

    @Select("SELECT * FROM finances WHERE id = #{id} and MONTH(date) = #{month} AND YEAR(date) = #{year}")
    List<Finance> getFinancesByidAndMonth(Long id, Integer year, Integer month);

    @Select("SELECT * FROM finances WHERE name = #{name} and MONTH(date) = #{month} AND YEAR(date) = #{year}")
    List<Finance> getFinancesBynameAndMonth(String name, Integer year, Integer month);

    @Select("SELECT * FROM finances WHERE amount = #{amount} and MONTH(date) = #{month} AND YEAR(date) = #{year}")
    List<Finance> getFinancesByamountAndMonth(String amount, Integer year, Integer month);

    @Select("SELECT * FROM finances WHERE type = #{type} and MONTH(date) = #{month} AND YEAR(date) = #{year}")
    List<Finance> getFinancesBytypeAndMonth(String type, Integer year, Integer month);

    @Select("SELECT * FROM finances WHERE date = #{date} and MONTH(date) = #{month} AND YEAR(date) = #{year}")
    List<Finance> getFinancesBydateAndMonth(String date, Integer year, Integer month);


}
