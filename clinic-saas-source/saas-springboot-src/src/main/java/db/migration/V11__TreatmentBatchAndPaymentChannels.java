package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V11__TreatmentBatchAndPaymentChannels extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureTreatmentColumns(connection);
        ensureFinanceColumns(connection);
        ensurePaymentChannelTable(connection);
        seedDefaultPaymentChannels(connection);
    }

    private void ensureTreatmentColumns(Connection connection) throws SQLException {
        ensureColumn(connection, "treatment", "batch_no",
                "ALTER TABLE treatment ADD COLUMN batch_no VARCHAR(64) DEFAULT NULL COMMENT '同次处置批次号' AFTER patient_name");
        ensureColumn(connection, "treatment", "created_at",
                "ALTER TABLE treatment ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER treatment_fee");
        ensureColumn(connection, "treatment", "updated_at",
                "ALTER TABLE treatment ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at");
        ensureIndex(connection, "treatment", "idx_treatment_batch_no",
                "CREATE INDEX idx_treatment_batch_no ON treatment (batch_no)");
    }

    private void ensureFinanceColumns(Connection connection) throws SQLException {
        ensureColumn(connection, "finances", "payment_channel_id",
                "ALTER TABLE finances ADD COLUMN payment_channel_id BIGINT DEFAULT NULL COMMENT '收款渠道ID' AFTER treatment_id");
        ensureColumn(connection, "finances", "payment_channel_name",
                "ALTER TABLE finances ADD COLUMN payment_channel_name VARCHAR(100) DEFAULT NULL COMMENT '收款渠道名称' AFTER payment_channel_id");
        ensureIndex(connection, "finances", "idx_finances_payment_channel_id",
                "CREATE INDEX idx_finances_payment_channel_id ON finances (payment_channel_id)");
    }

    private void ensurePaymentChannelTable(Connection connection) throws SQLException {
        if (tableExists(connection, "payment_channel")) {
            return;
        }
        execute(connection, """
                CREATE TABLE IF NOT EXISTS payment_channel (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    channel_name VARCHAR(100) NOT NULL COMMENT '收款渠道名称',
                    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款渠道表'
                """);
    }

    private void seedDefaultPaymentChannels(Connection connection) throws SQLException {
        if (!tableExists(connection, "payment_channel")) {
            return;
        }
        execute(connection, """
                INSERT INTO payment_channel (channel_name, status, sort_order)
                SELECT '现金', 1, 10 FROM DUAL
                WHERE NOT EXISTS (SELECT 1 FROM payment_channel WHERE TRIM(channel_name) = '现金')
                """);
        execute(connection, """
                INSERT INTO payment_channel (channel_name, status, sort_order)
                SELECT '微信', 1, 20 FROM DUAL
                WHERE NOT EXISTS (SELECT 1 FROM payment_channel WHERE TRIM(channel_name) = '微信')
                """);
        execute(connection, """
                INSERT INTO payment_channel (channel_name, status, sort_order)
                SELECT '支付宝', 1, 30 FROM DUAL
                WHERE NOT EXISTS (SELECT 1 FROM payment_channel WHERE TRIM(channel_name) = '支付宝')
                """);
        execute(connection, """
                INSERT INTO payment_channel (channel_name, status, sort_order)
                SELECT '银行卡', 1, 40 FROM DUAL
                WHERE NOT EXISTS (SELECT 1 FROM payment_channel WHERE TRIM(channel_name) = '银行卡')
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
