package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V32__PatientReferralRecordsAndConsultationReferrer extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_referral_records (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT NOT NULL COMMENT '被介绍患者ID',
                    consultation_record_id BIGINT DEFAULT NULL COMMENT '关联咨询记录ID',
                    referrer_type VARCHAR(20) DEFAULT NULL COMMENT '介绍人类型：patient/external',
                    referrer_patient_id BIGINT DEFAULT NULL COMMENT '介绍患者ID',
                    referrer_patient_name VARCHAR(50) DEFAULT NULL COMMENT '介绍患者姓名',
                    external_referrer_type VARCHAR(30) DEFAULT NULL COMMENT '外部介绍人类型',
                    external_referrer_name VARCHAR(50) DEFAULT NULL COMMENT '外部介绍人姓名',
                    external_referrer_contact VARCHAR(50) DEFAULT NULL COMMENT '外部介绍人联系方式',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    created_by BIGINT DEFAULT NULL COMMENT '录入人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '录入人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_referral_patient_id (patient_id),
                    KEY idx_referrer_patient_id (referrer_patient_id),
                    KEY idx_referral_created_at (created_at),
                    KEY idx_referral_consultation_id (consultation_record_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者转介绍记录表'
                """);

        ensureColumn(connection, "consultation_records", "referrer_type",
                "ALTER TABLE consultation_records ADD COLUMN referrer_type VARCHAR(20) DEFAULT NULL COMMENT '介绍人类型：patient/external' AFTER consultation_channel");
        ensureColumn(connection, "consultation_records", "referrer_patient_id",
                "ALTER TABLE consultation_records ADD COLUMN referrer_patient_id BIGINT DEFAULT NULL COMMENT '介绍患者ID' AFTER referrer_type");
        ensureColumn(connection, "consultation_records", "referrer_patient_name",
                "ALTER TABLE consultation_records ADD COLUMN referrer_patient_name VARCHAR(50) DEFAULT NULL COMMENT '介绍患者姓名' AFTER referrer_patient_id");
        ensureColumn(connection, "consultation_records", "external_referrer_type",
                "ALTER TABLE consultation_records ADD COLUMN external_referrer_type VARCHAR(30) DEFAULT NULL COMMENT '外部介绍人类型' AFTER referrer_patient_name");
        ensureColumn(connection, "consultation_records", "external_referrer_name",
                "ALTER TABLE consultation_records ADD COLUMN external_referrer_name VARCHAR(50) DEFAULT NULL COMMENT '外部介绍人姓名' AFTER external_referrer_type");
        ensureColumn(connection, "consultation_records", "external_referrer_contact",
                "ALTER TABLE consultation_records ADD COLUMN external_referrer_contact VARCHAR(50) DEFAULT NULL COMMENT '外部介绍人联系方式' AFTER external_referrer_name");

        ensureIndex(connection, "patient_referral_records", "uk_referral_patient_id",
                "CREATE UNIQUE INDEX uk_referral_patient_id ON patient_referral_records (patient_id)");
        ensureIndex(connection, "patient_referral_records", "idx_referrer_patient_id",
                "CREATE INDEX idx_referrer_patient_id ON patient_referral_records (referrer_patient_id)");
        ensureIndex(connection, "patient_referral_records", "idx_referral_created_at",
                "CREATE INDEX idx_referral_created_at ON patient_referral_records (created_at)");
        ensureIndex(connection, "patient_referral_records", "idx_referral_consultation_id",
                "CREATE INDEX idx_referral_consultation_id ON patient_referral_records (consultation_record_id)");
        ensureIndex(connection, "consultation_records", "idx_consultation_referrer_patient_id",
                "CREATE INDEX idx_consultation_referrer_patient_id ON consultation_records (referrer_patient_id)");
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || columnExists(connection, tableName, columnName)) {
            return;
        }
        execute(connection, sql);
    }

    private void ensureIndex(Connection connection, String tableName, String indexName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || indexExists(connection, tableName, indexName)) {
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
                if (columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (rs.next()) {
                String currentName = rs.getString("INDEX_NAME");
                if (currentName != null && indexName.equalsIgnoreCase(currentName)) {
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
