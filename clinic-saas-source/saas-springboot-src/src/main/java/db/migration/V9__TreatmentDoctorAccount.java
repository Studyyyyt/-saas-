package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V9__TreatmentDoctorAccount extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureTreatmentDoctorAccountColumn(connection);
        backfillTreatmentDoctorAccount(connection);
        ensureTreatmentDoctorAccountIndex(connection);
    }

    private void ensureTreatmentDoctorAccountColumn(Connection connection) throws SQLException {
        if (!tableExists(connection, "treatment")) {
            return;
        }
        if (!columnExists(connection, "treatment", "doctor_account_id")) {
            execute(connection, "ALTER TABLE treatment ADD COLUMN doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID' AFTER status");
        }
    }

    private void backfillTreatmentDoctorAccount(Connection connection) throws SQLException {
        if (!tableExists(connection, "treatment")
                || !tableExists(connection, "users")
                || !columnExists(connection, "treatment", "doctor_account_id")
                || !columnExists(connection, "treatment", "doctor_name")) {
            return;
        }
        execute(connection, """
                UPDATE treatment t
                JOIN (
                    SELECT MAX(u.id) AS doctor_account_id, TRIM(u.name) AS doctor_name
                    FROM users u
                    WHERE u.role IN ('doctor', '医生')
                      AND u.name IS NOT NULL
                      AND TRIM(u.name) <> ''
                    GROUP BY TRIM(u.name)
                ) d ON d.doctor_name = TRIM(t.doctor_name)
                SET t.doctor_account_id = d.doctor_account_id
                WHERE (t.doctor_account_id IS NULL OR t.doctor_account_id <= 0)
                  AND t.doctor_name IS NOT NULL
                  AND TRIM(t.doctor_name) <> ''
                  AND TRIM(t.doctor_name) <> '未指定医生'
                """);
    }

    private void ensureTreatmentDoctorAccountIndex(Connection connection) throws SQLException {
        if (!tableExists(connection, "treatment")
                || !columnExists(connection, "treatment", "doctor_account_id")
                || indexExists(connection, "treatment", "idx_treatment_doctor_account_id")) {
            return;
        }
        execute(connection, "CREATE INDEX idx_treatment_doctor_account_id ON treatment (doctor_account_id)");
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
