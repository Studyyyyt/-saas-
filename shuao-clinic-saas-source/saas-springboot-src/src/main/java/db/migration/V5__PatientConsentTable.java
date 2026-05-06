package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V5__PatientConsentTable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createTable(connection);
        ensureIndexes(connection);
    }

    private void createTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_consent (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    patient_name VARCHAR(50) DEFAULT NULL COMMENT '患者姓名',
                    doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID',
                    doctor_name VARCHAR(50) DEFAULT NULL COMMENT '医生姓名',
                    title VARCHAR(100) NOT NULL COMMENT '同意书标题',
                    content TEXT NOT NULL COMMENT '同意书正文快照',
                    status VARCHAR(20) NOT NULL DEFAULT '待签署' COMMENT '状态：待签署/已签署',
                    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下发时间',
                    read_at DATETIME DEFAULT NULL COMMENT '阅读时间',
                    signed_at DATETIME DEFAULT NULL COMMENT '签署时间',
                    signature_name VARCHAR(50) DEFAULT NULL COMMENT '签署姓名',
                    signature_data LONGTEXT COMMENT '签名图片(base64)',
                    signature_remark VARCHAR(500) DEFAULT NULL COMMENT '签署备注',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者电子知情同意书'
                """);
    }

    private void ensureIndexes(Connection connection) throws SQLException {
        ensureIndex(connection, "patient_consent", "idx_patient_consent_patient_id",
                "CREATE INDEX idx_patient_consent_patient_id ON patient_consent (patient_id)");
        ensureIndex(connection, "patient_consent", "idx_patient_consent_status",
                "CREATE INDEX idx_patient_consent_status ON patient_consent (status)");
        ensureIndex(connection, "patient_consent", "idx_patient_consent_doctor_account_id",
                "CREATE INDEX idx_patient_consent_doctor_account_id ON patient_consent (doctor_account_id)");
        ensureIndex(connection, "patient_consent", "idx_patient_consent_signed_at",
                "CREATE INDEX idx_patient_consent_signed_at ON patient_consent (signed_at)");
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
