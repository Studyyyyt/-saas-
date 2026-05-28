package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V29__TreatmentOperationPerformanceAllocation extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureColumn(connection, "project_operation_relations", "performance_weight",
                "ALTER TABLE project_operation_relations ADD COLUMN performance_weight DECIMAL(10,4) NOT NULL DEFAULT 1.0000 COMMENT '业绩权重，0=不参与业绩' AFTER is_required");
        ensureColumn(connection, "treatment", "medical_record_id",
                "ALTER TABLE treatment ADD COLUMN medical_record_id BIGINT DEFAULT NULL COMMENT '来源病历ID' AFTER batch_no");
        ensureIndex(connection, "treatment", "idx_treatment_medical_record_id",
                "CREATE INDEX idx_treatment_medical_record_id ON treatment (medical_record_id)");

        execute(connection, """
                CREATE TABLE IF NOT EXISTS treatment_operation_allocations (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    treatment_id BIGINT NOT NULL COMMENT '治疗记录ID',
                    medical_record_id BIGINT DEFAULT NULL COMMENT '来源病历ID',
                    medical_record_operation_id BIGINT DEFAULT NULL COMMENT '来源病历操作ID',
                    patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
                    doctor_account_id BIGINT DEFAULT NULL COMMENT '业绩归属医生ID',
                    doctor_name VARCHAR(50) DEFAULT NULL COMMENT '业绩归属医生姓名',
                    project_id BIGINT DEFAULT NULL COMMENT '项目ID',
                    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称冗余',
                    operation_id BIGINT DEFAULT NULL COMMENT '操作字典ID',
                    operation_name VARCHAR(100) DEFAULT NULL COMMENT '操作名称冗余',
                    performance_weight DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '业绩权重快照',
                    allocation_ratio DECIMAL(12,6) NOT NULL DEFAULT 0.000000 COMMENT '分摊比例快照',
                    allocated_turnover_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '折后产值分摊金额',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                    PRIMARY KEY (id),
                    KEY idx_toa_treatment_id (treatment_id),
                    KEY idx_toa_doctor_account_id (doctor_account_id),
                    KEY idx_toa_medical_record_id (medical_record_id),
                    KEY idx_toa_medical_record_operation_id (medical_record_operation_id),
                    KEY idx_toa_project_id (project_id),
                    KEY idx_toa_operation_id (operation_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗记录-病历操作业绩分摊快照'
                """);
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
