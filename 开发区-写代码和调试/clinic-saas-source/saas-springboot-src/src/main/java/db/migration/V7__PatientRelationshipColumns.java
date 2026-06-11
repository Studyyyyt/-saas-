package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V7__PatientRelationshipColumns extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureColumn(connection, "patients", "relation_type",
                "ALTER TABLE patients ADD COLUMN relation_type VARCHAR(30) DEFAULT NULL COMMENT '患者关系类型' AFTER address");
        ensureColumn(connection, "patients", "related_patient_id",
                "ALTER TABLE patients ADD COLUMN related_patient_id BIGINT DEFAULT NULL COMMENT '关联患者ID' AFTER relation_type");
        ensureColumn(connection, "patients", "related_patient_name",
                "ALTER TABLE patients ADD COLUMN related_patient_name VARCHAR(50) DEFAULT NULL COMMENT '关联患者姓名' AFTER related_patient_id");
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || columnExists(connection, tableName, columnName)) {
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

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
