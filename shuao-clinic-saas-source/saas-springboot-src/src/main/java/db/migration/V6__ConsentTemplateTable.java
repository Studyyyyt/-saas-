package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V6__ConsentTemplateTable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createTable(connection);
        ensureIndexes(connection);
    }

    private void createTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consent_template (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    title VARCHAR(100) NOT NULL COMMENT '模板标题',
                    content TEXT NOT NULL COMMENT '模板正文',
                    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知情同意书模板库'
                """);
    }

    private void ensureIndexes(Connection connection) throws SQLException {
        ensureIndex(connection, "consent_template", "idx_consent_template_status",
                "CREATE INDEX idx_consent_template_status ON consent_template (status)");
        ensureIndex(connection, "consent_template", "idx_consent_template_sort_order",
                "CREATE INDEX idx_consent_template_sort_order ON consent_template (sort_order)");
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
