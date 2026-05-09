package com.example.springboot.mapper;

import com.example.springboot.entity.ConsultationQuery;
import com.example.springboot.entity.ConsultationRecord;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface ConsultationRecordMapper {

    @Select("""
            SELECT cr.*,
                   p.name AS patient_name,
                   p.phone AS patient_phone,
                   p.customer_source AS patient_customer_source,
                   (SELECT cf.next_followup_time
                    FROM consultation_followups cf
                    WHERE cf.consultation_id = cr.id
                    ORDER BY cf.followup_time DESC, cf.id DESC
                    LIMIT 1) AS next_followup_time
            FROM consultation_records cr
            LEFT JOIN patients p ON p.id = cr.patient_id
            WHERE cr.id = #{id}
            """)
    ConsultationRecord selectById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT cr.*,",
            "       p.name AS patient_name,",
            "       p.phone AS patient_phone,",
            "       p.customer_source AS patient_customer_source,",
            "       (SELECT cf.next_followup_time",
            "        FROM consultation_followups cf",
            "        WHERE cf.consultation_id = cr.id",
            "        ORDER BY cf.followup_time DESC, cf.id DESC",
            "        LIMIT 1) AS next_followup_time",
            "FROM consultation_records cr",
            "LEFT JOIN patients p ON p.id = cr.patient_id",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND (",
            "    cr.contact_name LIKE CONCAT('%', #{keyword}, '%')",
            "    OR cr.contact_phone LIKE CONCAT('%', #{keyword}, '%')",
            "    OR COALESCE(p.name, '') LIKE CONCAT('%', #{keyword}, '%')",
            "    OR COALESCE(p.phone, '') LIKE CONCAT('%', #{keyword}, '%')",
            "    OR COALESCE(cr.referrer_patient_name, '') LIKE CONCAT('%', #{keyword}, '%')",
            "    OR COALESCE(cr.external_referrer_name, '') LIKE CONCAT('%', #{keyword}, '%')",
            "  )",
            "</if>",
            "<if test='startTime != null and startTime != \"\"'>",
            "  AND cr.consultation_time <![CDATA[>=]]> #{startTime}",
            "</if>",
            "<if test='endTime != null and endTime != \"\"'>",
            "  AND cr.consultation_time <![CDATA[<=]]> #{endTime}",
            "</if>",
            "<if test='channel != null and channel != \"\"'>",
            "  AND cr.consultation_channel = #{channel}",
            "</if>",
            "<if test='chiefProject != null and chiefProject != \"\"'>",
            "  AND cr.chief_project = #{chiefProject}",
            "</if>",
            "<if test='intentLevel != null and intentLevel != \"\"'>",
            "  AND cr.intent_level = #{intentLevel}",
            "</if>",
            "<if test='handlingResult != null and handlingResult != \"\"'>",
            "  AND cr.handling_result = #{handlingResult}",
            "</if>",
            "<if test='hasDeal != null'>",
            "  <choose>",
            "    <when test='hasDeal'> AND cr.deal_at IS NOT NULL </when>",
            "    <otherwise> AND cr.deal_at IS NULL </otherwise>",
            "  </choose>",
            "</if>",
            "<if test='createdBy != null and createdBy &gt; 0'>",
            "  AND cr.created_by = #{createdBy}",
            "</if>",
            "ORDER BY cr.consultation_time DESC, cr.id DESC",
            "</script>"
    })
    List<ConsultationRecord> search(ConsultationQuery query);

    @Select("""
            SELECT cr.*,
                   p.name AS patient_name,
                   p.phone AS patient_phone,
                   p.customer_source AS patient_customer_source,
                   (SELECT cf.next_followup_time
                    FROM consultation_followups cf
                    WHERE cf.consultation_id = cr.id
                    ORDER BY cf.followup_time DESC, cf.id DESC
                    LIMIT 1) AS next_followup_time
            FROM consultation_records cr
            LEFT JOIN patients p ON p.id = cr.patient_id
            WHERE cr.patient_id = #{patientId}
            ORDER BY cr.consultation_time DESC, cr.id DESC
            """)
    List<ConsultationRecord> selectByPatientId(@Param("patientId") Long patientId);

    @Select({
            "<script>",
            "SELECT cr.*,",
            "       p.name AS patient_name,",
            "       p.phone AS patient_phone,",
            "       p.customer_source AS patient_customer_source,",
            "       (SELECT cf.next_followup_time",
            "        FROM consultation_followups cf",
            "        WHERE cf.consultation_id = cr.id",
            "        ORDER BY cf.followup_time DESC, cf.id DESC",
            "        LIMIT 1) AS next_followup_time",
            "FROM consultation_records cr",
            "LEFT JOIN patients p ON p.id = cr.patient_id",
            "WHERE 1=1",
            "<if test='phone != null and phone != \"\"'>",
            "  AND cr.contact_phone = #{phone}",
            "</if>",
            "<if test='name != null and name != \"\"'>",
            "  AND cr.contact_name LIKE CONCAT('%', #{name}, '%')",
            "</if>",
            "<if test='startTime != null and startTime != \"\"'>",
            "  AND cr.consultation_time <![CDATA[>=]]> #{startTime}",
            "</if>",
            "<if test='endTime != null and endTime != \"\"'>",
            "  AND cr.consultation_time <![CDATA[<=]]> #{endTime}",
            "</if>",
            "ORDER BY cr.consultation_time DESC, cr.id DESC",
            "</script>"
    })
    List<ConsultationRecord> searchForPatientCreate(ConsultationQuery query);

    @Select("""
            SELECT COUNT(*)
            FROM consultation_records
            WHERE contact_phone = #{phone}
              AND deal_at IS NULL
            """)
    int countOpenConsultationsByPhone(@Param("phone") String phone);

    @Select("""
            SELECT COUNT(*)
            FROM consultation_records
            WHERE consultation_time >= #{startTime}
              AND consultation_time < #{endTime}
            """)
    int countByConsultationTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    @Insert("""
            INSERT INTO consultation_records (
                patient_id,
                consultation_time,
                consultation_channel,
                referrer_type,
                referrer_patient_id,
                referrer_patient_name,
                external_referrer_type,
                external_referrer_name,
                external_referrer_contact,
                chief_project,
                intent_level,
                handling_result,
                contact_name,
                contact_phone,
                remarks,
                estimated_amount,
                customer_concerns,
                ai_analysis_summary,
                ai_analysis_score,
                arrived_at,
                deal_at,
                created_by,
                created_by_name,
                updated_by
            ) VALUES (
                #{patient_id},
                #{consultation_time},
                #{consultation_channel},
                #{referrer_type},
                #{referrer_patient_id},
                #{referrer_patient_name},
                #{external_referrer_type},
                #{external_referrer_name},
                #{external_referrer_contact},
                #{chief_project},
                #{intent_level},
                #{handling_result},
                #{contact_name},
                #{contact_phone},
                #{remarks},
                #{estimated_amount},
                #{customer_concerns},
                #{ai_analysis_summary},
                #{ai_analysis_score},
                #{arrived_at},
                #{deal_at},
                #{created_by},
                #{created_by_name},
                #{updated_by}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ConsultationRecord record);

    @Update("""
            UPDATE consultation_records
            SET consultation_time = #{consultation_time},
                patient_id = #{patient_id},
                consultation_channel = #{consultation_channel},
                referrer_type = #{referrer_type},
                referrer_patient_id = #{referrer_patient_id},
                referrer_patient_name = #{referrer_patient_name},
                external_referrer_type = #{external_referrer_type},
                external_referrer_name = #{external_referrer_name},
                external_referrer_contact = #{external_referrer_contact},
                chief_project = #{chief_project},
                intent_level = #{intent_level},
                handling_result = #{handling_result},
                contact_name = #{contact_name},
                contact_phone = #{contact_phone},
                remarks = #{remarks},
                estimated_amount = #{estimated_amount},
                customer_concerns = #{customer_concerns},
                ai_analysis_summary = #{ai_analysis_summary},
                ai_analysis_score = #{ai_analysis_score},
                arrived_at = #{arrived_at},
                deal_at = #{deal_at},
                created_by = #{created_by},
                created_by_name = #{created_by_name},
                updated_by = #{updated_by}
            WHERE id = #{id}
            """)
    void updateEditableFields(ConsultationRecord record);

    @Update("""
            UPDATE consultation_records
            SET patient_id = #{patientId},
                handling_result = #{handlingResult},
                arrived_at = #{arrivedAt},
                updated_by = #{updatedBy}
            WHERE id = #{id}
            """)
    void linkPatient(@Param("id") Long id,
                     @Param("patientId") Long patientId,
                     @Param("handlingResult") String handlingResult,
                     @Param("arrivedAt") Date arrivedAt,
                     @Param("updatedBy") Long updatedBy);

    @Update("""
            UPDATE consultation_records
            SET deal_at = #{dealAt}
            WHERE id = #{id}
              AND deal_at IS NULL
            """)
    int markDealAtIfAbsent(@Param("id") Long id, @Param("dealAt") Date dealAt);

    @Update("""
            UPDATE consultation_records
            SET arrived_at = #{arrivedAt},
                updated_by = #{updatedBy}
            WHERE id = #{id}
              AND arrived_at IS NULL
            """)
    int markArrivedAtIfAbsent(@Param("id") Long id,
                              @Param("arrivedAt") Date arrivedAt,
                              @Param("updatedBy") Long updatedBy);

    @Update("""
            UPDATE consultation_records
            SET patient_id = NULL,
                updated_by = #{updatedBy}
            WHERE patient_id = #{patientId}
            """)
    void clearPatientLinkByPatientId(@Param("patientId") Long patientId, @Param("updatedBy") Long updatedBy);

    @Select("""
            SELECT cr.*,
                   p.name AS patient_name,
                   p.phone AS patient_phone,
                   p.customer_source AS patient_customer_source,
                   (SELECT cf.next_followup_time
                    FROM consultation_followups cf
                    WHERE cf.consultation_id = cr.id
                    ORDER BY cf.followup_time DESC, cf.id DESC
                    LIMIT 1) AS next_followup_time
            FROM consultation_records cr
            LEFT JOIN patients p ON p.id = cr.patient_id
            WHERE cr.patient_id = #{patientId}
              AND cr.deal_at IS NULL
              AND cr.consultation_time <= #{beforeTime}
            ORDER BY cr.consultation_time DESC, cr.id DESC
            LIMIT 1
            """)
    ConsultationRecord selectLatestOpenByPatientBeforeTime(@Param("patientId") Long patientId,
                                                           @Param("beforeTime") Date beforeTime);

    @Select("""
            SELECT cr.*,
                   p.name AS patient_name,
                   p.phone AS patient_phone,
                   p.customer_source AS patient_customer_source,
                   (SELECT cf.next_followup_time
                    FROM consultation_followups cf
                    WHERE cf.consultation_id = cr.id
                    ORDER BY cf.followup_time DESC, cf.id DESC
                    LIMIT 1) AS next_followup_time
            FROM consultation_records cr
            LEFT JOIN patients p ON p.id = cr.patient_id
            WHERE cr.contact_phone = #{phone}
            ORDER BY cr.consultation_time DESC, cr.id DESC
            """)
    List<ConsultationRecord> selectByPhone(@Param("phone") String phone);
}
