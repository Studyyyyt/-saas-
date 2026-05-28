package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V30__AdvertisingSpendingModule extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE IF NOT EXISTS advertising_spending (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    platform VARCHAR(30) NOT NULL COMMENT '投放平台',
                    campaign_name VARCHAR(100) DEFAULT NULL COMMENT '活动名称',
                    start_date DATE NOT NULL COMMENT '开始日期',
                    end_date DATE NOT NULL COMMENT '结束日期',
                    amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '投放金额',
                    target_project VARCHAR(50) DEFAULT NULL COMMENT '目标项目',
                    target_audience VARCHAR(100) DEFAULT NULL COMMENT '目标人群',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    finance_record_id BIGINT DEFAULT NULL COMMENT '同步财务记录ID',
                    created_by BIGINT DEFAULT NULL COMMENT '录入人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '录入人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_ad_platform_start (platform, start_date),
                    KEY idx_ad_period (start_date, end_date),
                    KEY idx_ad_created_by (created_by),
                    KEY idx_ad_finance_record_id (finance_record_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告投放记录表'
                """);

        ensureIndex(connection, "advertising_spending", "idx_ad_platform_start",
                "CREATE INDEX idx_ad_platform_start ON advertising_spending (platform, start_date)");
        ensureIndex(connection, "advertising_spending", "idx_ad_period",
                "CREATE INDEX idx_ad_period ON advertising_spending (start_date, end_date)");
        ensureIndex(connection, "advertising_spending", "idx_ad_created_by",
                "CREATE INDEX idx_ad_created_by ON advertising_spending (created_by)");
        ensureIndex(connection, "advertising_spending", "idx_ad_finance_record_id",
                "CREATE INDEX idx_ad_finance_record_id ON advertising_spending (finance_record_id)");
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
