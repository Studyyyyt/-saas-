package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V28__MedicalRecordWorkbenchFields extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        ensureColumn(connection, "medical_records", "nurse_name",
                "ALTER TABLE medical_records ADD COLUMN nurse_name VARCHAR(64) DEFAULT NULL COMMENT '护士' AFTER doctor_name");
        ensureColumn(connection, "medical_records", "assistant_name",
                "ALTER TABLE medical_records ADD COLUMN assistant_name VARCHAR(64) DEFAULT NULL COMMENT '助理' AFTER nurse_name");
        ensureColumn(connection, "medical_records", "record_type",
                "ALTER TABLE medical_records ADD COLUMN record_type VARCHAR(20) DEFAULT '初诊' COMMENT '病历类型：初诊/复诊' AFTER visit_date");
        ensureColumn(connection, "medical_records", "present_illness_history",
                "ALTER TABLE medical_records ADD COLUMN present_illness_history TEXT DEFAULT NULL COMMENT '现病史' AFTER chief_complaint");
        ensureColumn(connection, "medical_records", "past_history",
                "ALTER TABLE medical_records ADD COLUMN past_history TEXT DEFAULT NULL COMMENT '既往史' AFTER present_illness_history");
        ensureColumn(connection, "medical_records", "infectious_history",
                "ALTER TABLE medical_records ADD COLUMN infectious_history TEXT DEFAULT NULL COMMENT '流行病史' AFTER past_history");
        ensureColumn(connection, "medical_records", "allergy_history",
                "ALTER TABLE medical_records ADD COLUMN allergy_history TEXT DEFAULT NULL COMMENT '过敏史' AFTER infectious_history");
        ensureColumn(connection, "medical_records", "general_condition",
                "ALTER TABLE medical_records ADD COLUMN general_condition VARCHAR(255) DEFAULT NULL COMMENT '一般情况' AFTER allergy_history");
        ensureColumn(connection, "medical_records", "examination",
                "ALTER TABLE medical_records ADD COLUMN examination TEXT DEFAULT NULL COMMENT '检查' AFTER general_condition");
        ensureColumn(connection, "medical_records", "auxiliary_examination",
                "ALTER TABLE medical_records ADD COLUMN auxiliary_examination TEXT DEFAULT NULL COMMENT '辅助检查' AFTER examination");
        ensureColumn(connection, "medical_records", "treatment_plan",
                "ALTER TABLE medical_records ADD COLUMN treatment_plan TEXT DEFAULT NULL COMMENT '治疗方案' AFTER diagnosis");
        ensureColumn(connection, "medical_records", "medical_advice",
                "ALTER TABLE medical_records ADD COLUMN medical_advice TEXT DEFAULT NULL COMMENT '医嘱' AFTER tooth_positions");
        ensureColumn(connection, "medical_records", "record_tags",
                "ALTER TABLE medical_records ADD COLUMN record_tags VARCHAR(255) DEFAULT NULL COMMENT '病历标签' AFTER prescription");
        ensureColumn(connection, "medical_records", "image_summary",
                "ALTER TABLE medical_records ADD COLUMN image_summary TEXT DEFAULT NULL COMMENT '影像说明' AFTER record_tags");
        ensureColumn(connection, "medical_records", "record_status",
                "ALTER TABLE medical_records ADD COLUMN record_status VARCHAR(20) DEFAULT 'final' COMMENT '保存状态：draft/final' AFTER notes");

        ensureColumn(connection, "medical_record_template", "template_category",
                "ALTER TABLE medical_record_template ADD COLUMN template_category VARCHAR(64) DEFAULT '常用模板' COMMENT '模板分类' AFTER template_name");
        ensureColumn(connection, "medical_record_template", "present_illness_history",
                "ALTER TABLE medical_record_template ADD COLUMN present_illness_history TEXT DEFAULT NULL COMMENT '现病史' AFTER chief_complaint");
        ensureColumn(connection, "medical_record_template", "past_history",
                "ALTER TABLE medical_record_template ADD COLUMN past_history TEXT DEFAULT NULL COMMENT '既往史' AFTER present_illness_history");
        ensureColumn(connection, "medical_record_template", "infectious_history",
                "ALTER TABLE medical_record_template ADD COLUMN infectious_history TEXT DEFAULT NULL COMMENT '流行病史' AFTER past_history");
        ensureColumn(connection, "medical_record_template", "allergy_history",
                "ALTER TABLE medical_record_template ADD COLUMN allergy_history TEXT DEFAULT NULL COMMENT '过敏史' AFTER infectious_history");
        ensureColumn(connection, "medical_record_template", "general_condition",
                "ALTER TABLE medical_record_template ADD COLUMN general_condition VARCHAR(255) DEFAULT NULL COMMENT '一般情况' AFTER allergy_history");
        ensureColumn(connection, "medical_record_template", "examination",
                "ALTER TABLE medical_record_template ADD COLUMN examination TEXT DEFAULT NULL COMMENT '检查' AFTER general_condition");
        ensureColumn(connection, "medical_record_template", "auxiliary_examination",
                "ALTER TABLE medical_record_template ADD COLUMN auxiliary_examination TEXT DEFAULT NULL COMMENT '辅助检查' AFTER examination");
        ensureColumn(connection, "medical_record_template", "treatment_plan",
                "ALTER TABLE medical_record_template ADD COLUMN treatment_plan TEXT DEFAULT NULL COMMENT '治疗方案' AFTER diagnosis");
        ensureColumn(connection, "medical_record_template", "medical_advice",
                "ALTER TABLE medical_record_template ADD COLUMN medical_advice TEXT DEFAULT NULL COMMENT '医嘱' AFTER tooth_positions");
        ensureColumn(connection, "medical_record_template", "record_tags",
                "ALTER TABLE medical_record_template ADD COLUMN record_tags VARCHAR(255) DEFAULT NULL COMMENT '病历标签' AFTER prescription");
        ensureColumn(connection, "medical_record_template", "image_summary",
                "ALTER TABLE medical_record_template ADD COLUMN image_summary TEXT DEFAULT NULL COMMENT '影像说明' AFTER record_tags");
        ensureColumn(connection, "medical_record_template", "record_type",
                "ALTER TABLE medical_record_template ADD COLUMN record_type VARCHAR(20) DEFAULT '初诊' COMMENT '病历类型' AFTER notes");
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || columnExists(connection, tableName, columnName)) {
            return;
        }
        execute(connection, sql);
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            while (rs.next()) {
                String currentName = rs.getString("COLUMN_NAME");
                if (currentName != null && columnName.equalsIgnoreCase(currentName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
