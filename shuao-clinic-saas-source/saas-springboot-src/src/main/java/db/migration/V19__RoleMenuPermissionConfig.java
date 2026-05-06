package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V19__RoleMenuPermissionConfig extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE IF NOT EXISTS role_menu_permissions (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    role_code VARCHAR(20) NOT NULL COMMENT '角色编码：admin/doctor/nurse',
                    menu_key VARCHAR(64) NOT NULL COMMENT '导航键，使用前端路径标识',
                    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否1是',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_role_menu_permissions_role_menu (role_code, menu_key),
                    KEY idx_role_menu_permissions_role_code (role_code),
                    KEY idx_role_menu_permissions_menu_key (menu_key)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色导航权限配置'
                """);
        ensureIndex(connection, "role_menu_permissions", "idx_role_menu_permissions_role_code",
                "CREATE INDEX idx_role_menu_permissions_role_code ON role_menu_permissions (role_code)");
        ensureIndex(connection, "role_menu_permissions", "idx_role_menu_permissions_menu_key",
                "CREATE INDEX idx_role_menu_permissions_menu_key ON role_menu_permissions (menu_key)");
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
