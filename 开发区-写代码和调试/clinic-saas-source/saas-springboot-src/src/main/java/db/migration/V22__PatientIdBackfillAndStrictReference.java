package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V22__PatientIdBackfillAndStrictReference extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        backfillPatientReference(connection, "appointment", "patient_id", "patient_name");
        backfillPatientReference(connection, "treatment", "patient_id", "patient_name");
        backfillPatientReference(connection, "medical_records", "patient_id", "patient_name");
        backfillPatientReference(connection, "lab_orders", "patient_id", "patient_name");
        backfillPatientReference(connection, "patients", "related_patient_id", "related_patient_name");
    }

    private void backfillPatientReference(Connection connection,
                                          String tableName,
                                          String idColumn,
                                          String nameColumn) throws SQLException {
        if (!tableExists(connection, "patients")
                || !tableExists(connection, tableName)
                || !columnExists(connection, tableName, idColumn)
                || !columnExists(connection, tableName, nameColumn)) {
            return;
        }
        execute(connection, """
                UPDATE %s t
                JOIN (
                    SELECT TRIM(COALESCE(name, '')) AS patient_name, MAX(id) AS patient_id
                    FROM patients
                    WHERE TRIM(COALESCE(name, '')) <> ''
                    GROUP BY TRIM(COALESCE(name, ''))
                    HAVING COUNT(*) = 1
                ) p ON p.patient_name = TRIM(COALESCE(t.%s, ''))
                SET t.%s = p.patient_id
                WHERE (t.%s IS NULL OR t.%s <= 0)
                  AND TRIM(COALESCE(t.%s, '')) <> ''
                """.formatted(tableName, nameColumn, idColumn, idColumn, idColumn, nameColumn));
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
