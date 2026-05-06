package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V20__PatientCustomGroups extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_custom_group (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    group_key VARCHAR(64) NOT NULL COMMENT '分组键',
                    group_name VARCHAR(50) NOT NULL COMMENT '分组名称',
                    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用1启用',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
                    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_patient_custom_group_key (group_key),
                    KEY idx_patient_custom_group_status_sort (status, sort_order, id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者自定义分组'
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_custom_group_member (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    group_id BIGINT NOT NULL COMMENT '分组ID',
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_patient_custom_group_member (group_id, patient_id),
                    KEY idx_patient_custom_group_member_patient_id (patient_id),
                    CONSTRAINT fk_patient_custom_group_member_group_id FOREIGN KEY (group_id) REFERENCES patient_custom_group (id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者自定义分组成员'
                """);
        ensureIndex(connection, "patient_custom_group", "idx_patient_custom_group_status_sort",
                "CREATE INDEX idx_patient_custom_group_status_sort ON patient_custom_group (status, sort_order, id)");
        ensureIndex(connection, "patient_custom_group_member", "idx_patient_custom_group_member_patient_id",
                "CREATE INDEX idx_patient_custom_group_member_patient_id ON patient_custom_group_member (patient_id)");
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
