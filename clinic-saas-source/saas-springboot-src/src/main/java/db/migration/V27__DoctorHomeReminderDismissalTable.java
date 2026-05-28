package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V27__DoctorHomeReminderDismissalTable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE IF NOT EXISTS doctor_home_reminder_dismissal (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    doctor_account_id BIGINT NOT NULL COMMENT '医生账号ID',
                    doctor_name VARCHAR(64) DEFAULT NULL COMMENT '医生姓名',
                    patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
                    patient_name VARCHAR(64) DEFAULT NULL COMMENT '患者姓名',
                    reminder_key VARCHAR(191) NOT NULL COMMENT '提醒唯一键',
                    dismissed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '标记完成时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生首页提醒消失状态'
                """);
        ensureIndex(connection, "doctor_home_reminder_dismissal", "uk_doctor_home_reminder_dismissal",
                "CREATE UNIQUE INDEX uk_doctor_home_reminder_dismissal ON doctor_home_reminder_dismissal (doctor_account_id, reminder_key)");
        ensureIndex(connection, "doctor_home_reminder_dismissal", "idx_doctor_home_reminder_patient",
                "CREATE INDEX idx_doctor_home_reminder_patient ON doctor_home_reminder_dismissal (doctor_account_id, patient_id)");
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
