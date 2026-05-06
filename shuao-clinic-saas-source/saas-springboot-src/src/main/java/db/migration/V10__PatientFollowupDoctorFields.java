package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V10__PatientFollowupDoctorFields extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureDoctorColumns(connection);
        backfillDoctorName(connection);
        backfillDoctorAccountId(connection);
        ensureDoctorIndex(connection);
    }

    private void ensureDoctorColumns(Connection connection) throws SQLException {
        if (!tableExists(connection, "patient_followup")) {
            return;
        }
        if (!columnExists(connection, "patient_followup", "doctor_account_id")) {
            execute(connection, "ALTER TABLE patient_followup ADD COLUMN doctor_account_id BIGINT DEFAULT NULL COMMENT '负责医生账号ID' AFTER patient_id");
        }
        if (!columnExists(connection, "patient_followup", "doctor_name")) {
            execute(connection, "ALTER TABLE patient_followup ADD COLUMN doctor_name VARCHAR(100) DEFAULT NULL COMMENT '负责医生姓名' AFTER doctor_account_id");
        }
    }

    private void backfillDoctorName(Connection connection) throws SQLException {
        if (!tableExists(connection, "patient_followup")
                || !columnExists(connection, "patient_followup", "doctor_name")) {
            return;
        }
        execute(connection, """
                UPDATE patient_followup pf
                SET pf.doctor_name = COALESCE(
                    (
                        SELECT TRIM(mr.doctor_name)
                        FROM medical_records mr
                        WHERE mr.patient_id = pf.patient_id
                          AND mr.doctor_name IS NOT NULL
                          AND TRIM(mr.doctor_name) <> ''
                        ORDER BY mr.visit_date DESC, mr.id DESC
                        LIMIT 1
                    ),
                    (
                        SELECT TRIM(t.doctor_name)
                        FROM treatment t
                        WHERE t.patient_id = pf.patient_id
                          AND t.doctor_name IS NOT NULL
                          AND TRIM(t.doctor_name) <> ''
                        ORDER BY t.treatment_date DESC, t.id DESC
                        LIMIT 1
                    ),
                    (
                        SELECT TRIM(a.doctor_name)
                        FROM appointment a
                        WHERE a.patient_id = pf.patient_id
                          AND a.doctor_name IS NOT NULL
                          AND TRIM(a.doctor_name) <> ''
                        ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC
                        LIMIT 1
                    )
                )
                WHERE (pf.doctor_name IS NULL OR TRIM(pf.doctor_name) = '')
                """);
    }

    private void backfillDoctorAccountId(Connection connection) throws SQLException {
        if (!tableExists(connection, "patient_followup")
                || !tableExists(connection, "users")
                || !columnExists(connection, "patient_followup", "doctor_account_id")
                || !columnExists(connection, "patient_followup", "doctor_name")) {
            return;
        }
        execute(connection, """
                UPDATE patient_followup pf
                JOIN (
                    SELECT MAX(u.id) AS doctor_account_id, TRIM(u.name) AS doctor_name
                    FROM users u
                    WHERE u.role IN ('doctor', '医生')
                      AND u.name IS NOT NULL
                      AND TRIM(u.name) <> ''
                    GROUP BY TRIM(u.name)
                ) d ON d.doctor_name = TRIM(pf.doctor_name)
                SET pf.doctor_account_id = d.doctor_account_id
                WHERE (pf.doctor_account_id IS NULL OR pf.doctor_account_id <= 0)
                  AND pf.doctor_name IS NOT NULL
                  AND TRIM(pf.doctor_name) <> ''
                """);
    }

    private void ensureDoctorIndex(Connection connection) throws SQLException {
        if (!tableExists(connection, "patient_followup")
                || !columnExists(connection, "patient_followup", "doctor_account_id")
                || indexExists(connection, "patient_followup", "idx_followup_doctor_account_id")) {
            return;
        }
        execute(connection, "CREATE INDEX idx_followup_doctor_account_id ON patient_followup (doctor_account_id)");
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
