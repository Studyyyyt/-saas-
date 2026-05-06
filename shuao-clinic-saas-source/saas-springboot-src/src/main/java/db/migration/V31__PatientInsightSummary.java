package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V31__PatientInsightSummary extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_insight_summary (
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    last_visit_date DATETIME DEFAULT NULL COMMENT '最近到店时间',
                    total_visit_count INT NOT NULL DEFAULT 0 COMMENT '累计到店次数',
                    total_spent DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计净消费',
                    last_treatment_date DATE DEFAULT NULL COMMENT '最近治疗日期',
                    visit_count_last_6m INT NOT NULL DEFAULT 0 COMMENT '近6月到店次数',
                    high_value_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '高价值客户标记',
                    lost_risk_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '流失风险标记',
                    referred_count INT NOT NULL DEFAULT 0 COMMENT '累计转介绍人数',
                    referred_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '转介绍累计净消费',
                    word_of_mouth_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '口碑客户标记',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (patient_id),
                    KEY idx_pis_last_visit_date (last_visit_date),
                    KEY idx_pis_total_spent (total_spent),
                    KEY idx_pis_high_value_flag (high_value_flag),
                    KEY idx_pis_lost_risk_flag (lost_risk_flag),
                    KEY idx_pis_word_of_mouth_flag (word_of_mouth_flag)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者洞察汇总表'
                """);

        ensureIndex(connection, "patient_insight_summary", "idx_pis_last_visit_date",
                "CREATE INDEX idx_pis_last_visit_date ON patient_insight_summary (last_visit_date)");
        ensureIndex(connection, "patient_insight_summary", "idx_pis_total_spent",
                "CREATE INDEX idx_pis_total_spent ON patient_insight_summary (total_spent)");
        ensureIndex(connection, "patient_insight_summary", "idx_pis_high_value_flag",
                "CREATE INDEX idx_pis_high_value_flag ON patient_insight_summary (high_value_flag)");
        ensureIndex(connection, "patient_insight_summary", "idx_pis_lost_risk_flag",
                "CREATE INDEX idx_pis_lost_risk_flag ON patient_insight_summary (lost_risk_flag)");
        ensureIndex(connection, "patient_insight_summary", "idx_pis_word_of_mouth_flag",
                "CREATE INDEX idx_pis_word_of_mouth_flag ON patient_insight_summary (word_of_mouth_flag)");
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
