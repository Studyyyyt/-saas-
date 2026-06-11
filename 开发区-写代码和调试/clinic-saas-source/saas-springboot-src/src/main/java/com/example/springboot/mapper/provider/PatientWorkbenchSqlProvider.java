package com.example.springboot.mapper.provider;

import com.example.springboot.entity.PatientWorkbenchQuery;
import org.springframework.util.StringUtils;

public class PatientWorkbenchSqlProvider {

    private static final String REGEX_IMPLANT = "种植|植骨|上颌窦|基台";
    private static final String REGEX_ROOT_CANAL = "根管|牙髓";
    private static final String REGEX_ORTHO = "正畸|矫治|托槽|保持器";
    private static final String REGEX_REPAIR = "修复|全瓷|贴面|牙冠|烤瓷|嵌体|冠桥";
    private static final String REGEX_CLEANING = "洁治|洗牙|牙洁|龈上洁治";
    private static final String REGEX_REMOVABLE = "活动|义齿|吸附|胶托";
    private static final String REGEX_EXTRACTION = "拔牙|阻生牙|拔除";
    private static final String REGEX_FILLING = "补牙|充填|树脂";
    private static final String REGEX_PERIODONTAL = "牙周|冠周|龈下刮治|龈瓣";

    private static final String INNER_SELECT = """
            SELECT p.*,
                   (
                       SELECT COALESCE(NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, ''))
                       FROM treatment t
                       WHERE t.patient_id = p.id
                       ORDER BY t.treatment_date DESC, t.id DESC
                       LIMIT 1
                   ) AS latest_treatment,
                   pis.last_visit_date AS last_visit_date,
                   (
                       SELECT xv.doctor_account_id
                       FROM (
                           SELECT mr.visit_date AS visit_at, mr.doctor_account_id, mr.doctor_name, 3 AS source_order
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           UNION ALL
                           SELECT TIMESTAMP(t.treatment_date, '00:00:00') AS visit_at, t.doctor_account_id, t.doctor_name, 2 AS source_order
                           FROM treatment t
                           WHERE t.patient_id = p.id
                       ) xv
                       WHERE xv.visit_at IS NOT NULL
                         AND (xv.doctor_account_id IS NOT NULL OR TRIM(COALESCE(xv.doctor_name, '')) <> '')
                       ORDER BY xv.visit_at DESC, xv.source_order DESC
                       LIMIT 1
                   ) AS latest_visit_doctor_account_id,
                   (
                       SELECT xv.doctor_name
                       FROM (
                           SELECT mr.visit_date AS visit_at, mr.doctor_account_id, mr.doctor_name, 3 AS source_order
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           UNION ALL
                           SELECT TIMESTAMP(t.treatment_date, '00:00:00') AS visit_at, t.doctor_account_id, t.doctor_name, 2 AS source_order
                           FROM treatment t
                           WHERE t.patient_id = p.id
                       ) xv
                       WHERE xv.visit_at IS NOT NULL
                         AND (xv.doctor_account_id IS NOT NULL OR TRIM(COALESCE(xv.doctor_name, '')) <> '')
                       ORDER BY xv.visit_at DESC, xv.source_order DESC
                       LIMIT 1
                   ) AS latest_visit_doctor_name,
                   (
                       SELECT xv.source_name
                       FROM (
                           SELECT mr.visit_date AS visit_at, 'medicalRecord' AS source_name, 3 AS source_order
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           UNION ALL
                           SELECT TIMESTAMP(t.treatment_date, '00:00:00') AS visit_at, 'treatment' AS source_name, 2 AS source_order
                           FROM treatment t
                           WHERE t.patient_id = p.id
                       ) xv
                       WHERE xv.visit_at IS NOT NULL
                       ORDER BY xv.visit_at DESC, xv.source_order DESC
                       LIMIT 1
                   ) AS latest_visit_source,
                   (
                       SELECT pf.next_followup_date
                       FROM patient_followup pf
                       WHERE pf.patient_id = p.id
                         AND pf.next_followup_date IS NOT NULL
                       ORDER BY CASE WHEN pf.next_followup_date >= NOW() THEN 0 ELSE 1 END,
                                pf.next_followup_date ASC,
                                pf.id DESC
                       LIMIT 1
                   ) AS next_followup_date,
                   (
                       SELECT pf.doctor_account_id
                       FROM patient_followup pf
                       WHERE pf.patient_id = p.id
                         AND pf.next_followup_date IS NOT NULL
                       ORDER BY CASE WHEN pf.next_followup_date >= NOW() THEN 0 ELSE 1 END,
                                pf.next_followup_date ASC,
                                pf.id DESC
                       LIMIT 1
                   ) AS followup_doctor_account_id,
                   (
                       SELECT pf.doctor_name
                       FROM patient_followup pf
                       WHERE pf.patient_id = p.id
                         AND pf.next_followup_date IS NOT NULL
                       ORDER BY CASE WHEN pf.next_followup_date >= NOW() THEN 0 ELSE 1 END,
                                pf.next_followup_date ASC,
                                pf.id DESC
                       LIMIT 1
                   ) AS followup_doctor_name,
                   (
                       SELECT MAX(pf.followup_date)
                       FROM patient_followup pf
                       WHERE pf.patient_id = p.id
                   ) AS last_followup_date,
                   COALESCE(pis.total_visit_count, 0) AS visit_count,
                   COALESCE(pis.total_spent, 0) AS total_spent,
                   pis.last_treatment_date AS last_treatment_date,
                   COALESCE(pis.visit_count_last_6m, 0) AS visit_count_last_6m,
                   COALESCE(pis.high_value_flag, 0) AS high_value_flag,
                   COALESCE(pis.lost_risk_flag, 0) AS lost_risk_flag,
                   COALESCE(pis.referred_count, 0) AS referred_count,
                   COALESCE(pis.referred_revenue, 0) AS referred_revenue,
                   COALESCE(pis.word_of_mouth_flag, 0) AS word_of_mouth_flag,
                   (
                       SELECT GROUP_CONCAT(DISTINCT g.group_key ORDER BY g.sort_order ASC, g.id ASC SEPARATOR ',')
                       FROM patient_custom_group_member gm
                       JOIN patient_custom_group g ON g.id = gm.group_id
                       WHERE gm.patient_id = p.id AND g.status = 1
                   ) AS custom_group_keys_text,
                   CONCAT_WS(' ',
                       (
                           SELECT GROUP_CONCAT(DISTINCT CONCAT_WS(' ', NULLIF(mro.project_name, ''), NULLIF(mro.operation_name, ''), NULLIF(o.operation_category, '')) SEPARATOR ' ')
                           FROM medical_records mr
                           JOIN medical_record_operations mro ON mro.medical_record_id = mr.id
                           LEFT JOIN treatment_operations o ON o.id = mro.operation_id
                       WHERE mr.patient_id = p.id
                       ),
                       (
                           SELECT GROUP_CONCAT(DISTINCT CONCAT_WS(' ', NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, '')) SEPARATOR ' ')
                           FROM treatment t
                       WHERE t.patient_id = p.id
                       )
                   ) AS group_signal_text,
                   (
                       SELECT ROUND(SUM(
                           CASE
                             WHEN TRIM(COALESCE(t.status, '')) IN ('取消', '已取消') THEN 0
                             WHEN COALESCE(CAST(NULLIF(TRIM(t.treatment_fee), '') AS DECIMAL(18, 2)), 0) <= 0 THEN 0
                             ELSE GREATEST(
                               ROUND(COALESCE(CAST(NULLIF(TRIM(t.treatment_fee), '') AS DECIMAL(18, 2)), 0), 2)
                               - GREATEST(ROUND(COALESCE(fin.charge_amount, 0), 2) - ROUND(COALESCE(fin.refund_amount, 0), 2), 0),
                               0
                             )
                           END
                       ), 2)
                       FROM treatment t
                       LEFT JOIN (
                           SELECT f.treatment_id,
                                  SUM(CASE
                                        WHEN f.amount > 0
                                         AND (UPPER(TRIM(COALESCE(f.biz_type, ''))) = 'TREATMENT_CHARGE'
                                           OR (UPPER(TRIM(COALESCE(f.biz_type, ''))) <> 'TREATMENT_REFUND'
                                            AND (TRIM(COALESCE(f.type, '')) LIKE '%收入%' OR TRIM(COALESCE(f.type, '')) LIKE '%收费%')))
                                        THEN f.amount ELSE 0 END) AS charge_amount,
                                  SUM(CASE
                                        WHEN f.amount > 0
                                         AND (UPPER(TRIM(COALESCE(f.biz_type, ''))) = 'TREATMENT_REFUND'
                                           OR (UPPER(TRIM(COALESCE(f.biz_type, ''))) <> 'TREATMENT_CHARGE'
                                            AND TRIM(COALESCE(f.type, '')) LIKE '%退款%'))
                                        THEN f.amount ELSE 0 END) AS refund_amount
                           FROM finances f
                           WHERE f.treatment_id IS NOT NULL AND f.treatment_id > 0
                           GROUP BY f.treatment_id
                       ) fin ON fin.treatment_id = t.id
                       WHERE t.patient_id = p.id
                   ) AS arrears_amount,
                   GREATEST(
                       COALESCE(p.updated_at, TIMESTAMP('1000-01-01 00:00:00')),
                       COALESCE(p.created_at, TIMESTAMP('1000-01-01 00:00:00')),
                       COALESCE((
                           SELECT xv.visit_at
                           FROM (
                               SELECT mr.visit_date AS visit_at, 3 AS source_order
                               FROM medical_records mr
                               WHERE mr.patient_id = p.id
                               UNION ALL
                               SELECT TIMESTAMP(t.treatment_date, '00:00:00') AS visit_at, 2 AS source_order
                               FROM treatment t
                               WHERE t.patient_id = p.id
                               UNION ALL
                               SELECT TIMESTAMP(a.appointment_date, IFNULL(a.appointment_time, '00:00:00')) AS visit_at, 1 AS source_order
                               FROM appointment a
                               WHERE a.patient_id = p.id
                           ) xv
                           WHERE xv.visit_at IS NOT NULL
                           ORDER BY xv.visit_at DESC, xv.source_order DESC
                           LIMIT 1
                       ), TIMESTAMP('1000-01-01 00:00:00')),
                       COALESCE((
                           SELECT MAX(pf.followup_date)
                           FROM patient_followup pf
                           WHERE pf.patient_id = p.id
                       ), TIMESTAMP('1000-01-01 00:00:00'))
                   ) AS last_activity_at
            FROM patients p
            LEFT JOIN patient_insight_summary pis ON pis.patient_id = p.id
            """;

    public String selectBaseRows(PatientWorkbenchQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (").append(INNER_SELECT).append(") base WHERE 1 = 1");
        appendFilters(sql, query, true);
        if (query == null) {
            sql.append(" ORDER BY base.id DESC");
            return sql.toString();
        }
        switch (query.getSortMode()) {
            case "recent":
                sql.append(" ORDER BY COALESCE(base.last_activity_at, TIMESTAMP('1000-01-01 00:00:00')) DESC, base.id DESC");
                break;
            case "totalSpentDesc":
                sql.append(" ORDER BY COALESCE(base.total_spent, 0) DESC, base.id DESC");
                break;
            case "visitCountDesc":
                sql.append(" ORDER BY COALESCE(base.visit_count, 0) DESC, base.id DESC");
                break;
            case "lastVisitDesc":
                sql.append(" ORDER BY COALESCE(base.last_visit_date, TIMESTAMP('1000-01-01 00:00:00')) DESC, base.id DESC");
                break;
            default:
                sql.append(" ORDER BY base.id DESC");
                break;
        }
        return sql.toString();
    }

    public String selectBuiltinGroupCounts(PatientWorkbenchQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) AS all_count,");
        sql.append(" SUM(CASE WHEN base.last_activity_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) AS recent_count,");
        sql.append(" SUM(CASE WHEN TRIM(COALESCE(base.latest_visit_doctor_name, '')) = '' AND COALESCE(base.visit_count, 0) <= 0 AND TRIM(COALESCE(base.latest_treatment, '')) = '' AND base.next_followup_date IS NULL THEN 1 ELSE 0 END) AS public_count,");
        appendCategoryCount(sql, REGEX_IMPLANT, "implant_count");
        appendCategoryCount(sql, REGEX_ROOT_CANAL, "rootCanal_count");
        appendCategoryCount(sql, REGEX_ORTHO, "ortho_count");
        appendCategoryCount(sql, REGEX_REPAIR, "repair_count");
        appendCategoryCount(sql, REGEX_CLEANING, "cleaning_count");
        appendCategoryCount(sql, REGEX_REMOVABLE, "removable_count");
        appendCategoryCount(sql, REGEX_EXTRACTION, "extraction_count");
        appendCategoryCount(sql, REGEX_FILLING, "filling_count");
        appendCategoryCount(sql, REGEX_PERIODONTAL, "periodontal_count");
        sql.append(" SUM(CASE WHEN TRIM(COALESCE(base.latest_treatment, '')) = '' AND COALESCE(base.visit_count, 0) <= 0 THEN 1 ELSE 0 END) AS unconverted_count,");
        sql.append(" SUM(CASE WHEN COALESCE(base.high_value_flag, 0) = 1 THEN 1 ELSE 0 END) AS highValue_count,");
        sql.append(" SUM(CASE WHEN COALESCE(base.lost_risk_flag, 0) = 1 THEN 1 ELSE 0 END) AS lostRisk_count,");
        sql.append(" SUM(CASE WHEN COALESCE(base.word_of_mouth_flag, 0) = 1 THEN 1 ELSE 0 END) AS wordOfMouth_count,");
        sql.append(" SUM(CASE WHEN ");
        appendOtherGroupPredicate(sql, "base");
        sql.append(" THEN 1 ELSE 0 END) AS other_count");
        sql.append(" FROM (").append(INNER_SELECT).append(") base WHERE 1 = 1");
        appendFilters(sql, query, false);
        return sql.toString();
    }

    public String selectCustomGroupCounts(PatientWorkbenchQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT g.group_key AS group_key, COUNT(DISTINCT base.id) AS total_count");
        sql.append(" FROM (").append(INNER_SELECT).append(") base");
        sql.append(" JOIN patient_custom_group_member gm ON gm.patient_id = base.id");
        sql.append(" JOIN patient_custom_group g ON g.id = gm.group_id AND g.status = 1");
        sql.append(" WHERE 1 = 1");
        appendFilters(sql, query, false);
        sql.append(" GROUP BY g.group_key");
        return sql.toString();
    }

    public String selectDoctorOptions(PatientWorkbenchQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT base.latest_visit_doctor_account_id AS doctor_account_id, base.latest_visit_doctor_name AS doctor_name");
        sql.append(" FROM (").append(INNER_SELECT).append(") base WHERE 1 = 1");
        appendFilters(sql, query, false);
        sql.append(" AND (base.latest_visit_doctor_account_id IS NOT NULL OR TRIM(COALESCE(base.latest_visit_doctor_name, '')) <> '')");
        sql.append(" ORDER BY base.latest_visit_doctor_name ASC");
        return sql.toString();
    }

    public String selectSourceOptions(PatientWorkbenchQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT TRIM(COALESCE(base.customer_source, '')) AS value");
        sql.append(" FROM (").append(INNER_SELECT).append(") base WHERE 1 = 1");
        appendFilters(sql, query, false);
        sql.append(" AND TRIM(COALESCE(base.customer_source, '')) <> ''");
        sql.append(" ORDER BY value ASC");
        return sql.toString();
    }

    public String selectRelationOptions(PatientWorkbenchQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT TRIM(COALESCE(base.relation_type, '')) AS value");
        sql.append(" FROM (").append(INNER_SELECT).append(") base WHERE 1 = 1");
        appendFilters(sql, query, false);
        sql.append(" AND TRIM(COALESCE(base.relation_type, '')) <> ''");
        sql.append(" ORDER BY value ASC");
        return sql.toString();
    }

    private void appendFilters(StringBuilder sql, PatientWorkbenchQuery query, boolean includeGroupFilter) {
        if (query == null) {
            return;
        }
        if (StringUtils.hasText(query.getClinicId())) {
            sql.append(" AND TRIM(COALESCE(base.clinic_id, '')) = #{clinicId}");
        }
        if (StringUtils.hasText(query.getKeyword())) {
            if ("id".equals(query.getSearchType())) {
                sql.append(" AND CAST(base.id AS CHAR) = #{keyword}");
            } else {
                sql.append(" AND (");
                sql.append("TRIM(COALESCE(base.name, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.phone, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.email, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.address, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.relation_type, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.related_patient_name, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.customer_source, '')) LIKE CONCAT('%', #{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.name_pinyin, '')) LIKE CONCAT(#{keyword}, '%')");
                sql.append(" OR TRIM(COALESCE(base.name_initials, '')) LIKE CONCAT(#{keyword}, '%')");
                sql.append(")");
            }
        }
        if ("today".equals(query.getQuickScope())) {
            sql.append(" AND DATE(base.last_activity_at) = CURDATE()");
        } else if ("recent".equals(query.getQuickScope())) {
            sql.append(" AND base.last_activity_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
        }
        if (query.getDoctorAccountId() != null && query.getDoctorAccountId() > 0) {
            sql.append(" AND base.latest_visit_doctor_account_id = #{doctorAccountId}");
        } else if (StringUtils.hasText(query.getDoctorName())) {
            sql.append(" AND TRIM(COALESCE(base.latest_visit_doctor_name, '')) = #{doctorName}");
        }
        if (StringUtils.hasText(query.getSourceFilter())) {
            sql.append(" AND TRIM(COALESCE(base.customer_source, '')) = #{sourceFilter}");
        }
        if (StringUtils.hasText(query.getRelationFilter())) {
            sql.append(" AND TRIM(COALESCE(base.relation_type, '')) = #{relationFilter}");
        }
        if ("arrears".equals(query.getArrearsFilter())) {
            sql.append(" AND COALESCE(base.arrears_amount, 0) > 0.0001");
        } else if ("normal".equals(query.getArrearsFilter())) {
            sql.append(" AND COALESCE(base.arrears_amount, 0) <= 0.0001");
        }
        if (!includeGroupFilter || !StringUtils.hasText(query.getGroupKey()) || "all".equals(query.getGroupKey())) {
            return;
        }
        switch (query.getGroupKey()) {
            case "recent":
                sql.append(" AND base.last_activity_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)");
                break;
            case "public":
                sql.append(" AND TRIM(COALESCE(base.latest_visit_doctor_name, '')) = ''");
                sql.append(" AND COALESCE(base.visit_count, 0) <= 0");
                sql.append(" AND TRIM(COALESCE(base.latest_treatment, '')) = ''");
                sql.append(" AND base.next_followup_date IS NULL");
                break;
            case "unconverted":
                sql.append(" AND TRIM(COALESCE(base.latest_treatment, '')) = ''");
                sql.append(" AND COALESCE(base.visit_count, 0) <= 0");
                break;
            case "other":
                sql.append(" AND ");
                appendOtherGroupPredicate(sql, "base");
                break;
            case "highValue":
                sql.append(" AND COALESCE(base.high_value_flag, 0) = 1");
                break;
            case "lostRisk":
                sql.append(" AND COALESCE(base.lost_risk_flag, 0) = 1");
                break;
            case "wordOfMouth":
                sql.append(" AND COALESCE(base.word_of_mouth_flag, 0) = 1");
                break;
            default:
                String regex = categoryRegex(query.getGroupKey());
                if (regex != null) {
                    sql.append(" AND base.group_signal_text REGEXP '").append(regex).append("'");
                } else {
                    sql.append(" AND FIND_IN_SET(#{groupKey}, COALESCE(base.custom_group_keys_text, '')) > 0");
                }
                break;
        }
    }

    private void appendCategoryCount(StringBuilder sql, String regex, String alias) {
        sql.append(" SUM(CASE WHEN base.group_signal_text REGEXP '")
                .append(regex)
                .append("' THEN 1 ELSE 0 END) AS ")
                .append(alias)
                .append(",");
    }

    private void appendOtherGroupPredicate(StringBuilder sql, String alias) {
        sql.append("((")
                .append(alias).append(".group_signal_text IS NULL")
                .append(" OR NOT (")
                .append(alias).append(".group_signal_text REGEXP '").append(REGEX_IMPLANT).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_ROOT_CANAL).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_ORTHO).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_REPAIR).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_CLEANING).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_REMOVABLE).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_EXTRACTION).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_FILLING).append("'")
                .append(" OR ").append(alias).append(".group_signal_text REGEXP '").append(REGEX_PERIODONTAL).append("'")
                .append("))")
                .append(" AND (").append(alias).append(".last_visit_date IS NOT NULL OR TRIM(COALESCE(").append(alias).append(".latest_treatment, '')) <> '')")
                .append(")");
    }

    private String categoryRegex(String groupKey) {
        if (!StringUtils.hasText(groupKey)) {
            return null;
        }
        switch (groupKey) {
            case "implant":
                return REGEX_IMPLANT;
            case "rootCanal":
                return REGEX_ROOT_CANAL;
            case "ortho":
                return REGEX_ORTHO;
            case "repair":
                return REGEX_REPAIR;
            case "cleaning":
                return REGEX_CLEANING;
            case "removable":
                return REGEX_REMOVABLE;
            case "extraction":
                return REGEX_EXTRACTION;
            case "filling":
                return REGEX_FILLING;
            case "periodontal":
                return REGEX_PERIODONTAL;
            default:
                return null;
        }
    }
}
