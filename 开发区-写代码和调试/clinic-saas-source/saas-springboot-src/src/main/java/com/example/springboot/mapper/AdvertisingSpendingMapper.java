package com.example.springboot.mapper;

import com.example.springboot.entity.AdvertisingSpending;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdvertisingSpendingMapper {

    @Select("SELECT * FROM advertising_spending ORDER BY start_date DESC, id DESC")
    List<AdvertisingSpending> selectAll();

    @Select("SELECT * FROM advertising_spending WHERE id = #{id}")
    AdvertisingSpending selectById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT * FROM advertising_spending",
            "WHERE 1 = 1",
            "<if test='startDate != null'> AND start_date &gt;= #{startDate} </if>",
            "<if test='endDate != null'> AND end_date &lt;= #{endDate} </if>",
            "<if test='platform != null and platform != \"\"'> AND platform = #{platform} </if>",
            "<if test='keyword != null and keyword != \"\"'> AND (campaign_name LIKE CONCAT('%', #{keyword}, '%') OR platform LIKE CONCAT('%', #{keyword}, '%') OR remark LIKE CONCAT('%', #{keyword}, '%')) </if>",
            "ORDER BY start_date DESC, id DESC",
            "</script>"
    })
    List<AdvertisingSpending> search(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("platform") String platform,
                                     @Param("keyword") String keyword);

    @Insert("""
            INSERT INTO advertising_spending (
                platform, campaign_name, start_date, end_date, amount,
                target_project, target_audience, remark, finance_record_id,
                created_by, created_by_name
            ) VALUES (
                #{platform}, #{campaign_name}, #{start_date}, #{end_date}, #{amount},
                #{target_project}, #{target_audience}, #{remark}, #{finance_record_id},
                #{created_by}, #{created_by_name}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AdvertisingSpending spending);

    @Update("""
            UPDATE advertising_spending
            SET platform = #{platform},
                campaign_name = #{campaign_name},
                start_date = #{start_date},
                end_date = #{end_date},
                amount = #{amount},
                target_project = #{target_project},
                target_audience = #{target_audience},
                remark = #{remark},
                finance_record_id = #{finance_record_id},
                created_by = #{created_by},
                created_by_name = #{created_by_name}
            WHERE id = #{id}
            """)
    void update(AdvertisingSpending spending);

    @Delete("DELETE FROM advertising_spending WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
