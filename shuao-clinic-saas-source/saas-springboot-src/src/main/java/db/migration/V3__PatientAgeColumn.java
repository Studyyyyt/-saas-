package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V3__PatientAgeColumn extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureAgeColumn(connection);
        backfillAge(connection);
    }

    private void ensureAgeColumn(Connection connection) throws SQLException {
        if (!tableExists(connection, "patients") || columnExists(connection, "patients", "age")) {
            return;
        }
        execute(connection, "ALTER TABLE patients ADD COLUMN age INT DEFAULT NULL COMMENT '年龄' AFTER gender");
    }

    private void backfillAge(Connection connection) throws SQLException {
        if (!tableExists(connection, "patients")
                || !columnExists(connection, "patients", "age")
                || !columnExists(connection, "patients", "date_of_birth")) {
            return;
        }
        execute(connection, """
                UPDATE patients
                SET age = TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE())
                WHERE age IS NULL
                  AND date_of_birth IS NOT NULL
                """);
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
