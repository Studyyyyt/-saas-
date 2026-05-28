package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V26__MedicalRecordTemplateTable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE IF NOT EXISTS medical_record_template (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
                    chief_complaint VARCHAR(500) DEFAULT NULL COMMENT '主诉',
                    diagnosis TEXT DEFAULT NULL COMMENT '诊断',
                    treatment TEXT DEFAULT NULL COMMENT '治疗文稿',
                    tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位',
                    prescription TEXT DEFAULT NULL COMMENT '处方',
                    notes TEXT DEFAULT NULL COMMENT '备注',
                    operation_items_json LONGTEXT DEFAULT NULL COMMENT '结构化操作JSON',
                    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人账号ID',
                    created_by_name VARCHAR(64) DEFAULT NULL COMMENT '创建人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历模板库'
                """);
        ensureIndex(connection, "medical_record_template", "idx_medical_record_template_status",
                "CREATE INDEX idx_medical_record_template_status ON medical_record_template (status, id)");
        ensureIndex(connection, "medical_record_template", "idx_medical_record_template_creator",
                "CREATE INDEX idx_medical_record_template_creator ON medical_record_template (created_by, id)");
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
