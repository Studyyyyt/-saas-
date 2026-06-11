package com.example.springboot.mapper;

import com.example.springboot.entity.ConsultationFollowup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConsultationFollowupMapper {

    @Insert("""
            INSERT INTO consultation_followups (
                consultation_id,
                followup_time,
                content,
                next_followup_time,
                created_by,
                created_by_name
            ) VALUES (
                #{consultation_id},
                #{followup_time},
                #{content},
                #{next_followup_time},
                #{created_by},
                #{created_by_name}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ConsultationFollowup followup);

    @Select("""
            SELECT cf.*,
                   cr.contact_name AS consultation_contact_name,
                   cr.contact_phone AS consultation_contact_phone
            FROM consultation_followups cf
            LEFT JOIN consultation_records cr ON cr.id = cf.consultation_id
            WHERE cf.consultation_id = #{consultationId}
            ORDER BY cf.followup_time DESC, cf.id DESC
            """)
    List<ConsultationFollowup> selectByConsultationId(@Param("consultationId") Long consultationId);

    @Select("SELECT COUNT(*) FROM consultation_followups WHERE consultation_id = #{consultationId}")
    int countByConsultationId(@Param("consultationId") Long consultationId);

    @Delete("DELETE FROM consultation_followups WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
