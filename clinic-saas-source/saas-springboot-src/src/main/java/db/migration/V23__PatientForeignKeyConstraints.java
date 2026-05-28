package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V23__PatientForeignKeyConstraints extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        rebindOrphanReferenceByUniqueName(connection, "appointment", "patient_id", "patient_name");
        rebindOrphanReferenceByUniqueName(connection, "treatment", "patient_id", "patient_name");
        rebindOrphanReferenceByUniqueName(connection, "medical_records", "patient_id", "patient_name");
        rebindOrphanReferenceByUniqueName(connection, "lab_orders", "patient_id", "patient_name");
        rebindOrphanReferenceByUniqueName(connection, "patients", "related_patient_id", "related_patient_name");
        ensurePatientReferencesReady(connection, "appointment", "patient_id", true);
        ensurePatientReferencesReady(connection, "treatment", "patient_id", true);
        ensurePatientReferencesReady(connection, "medical_records", "patient_id", true);
        ensurePatientReferencesReady(connection, "lab_orders", "patient_id", true);
        ensurePatientReferencesReady(connection, "patients", "related_patient_id", false);

        execute(connection, "ALTER TABLE appointment MODIFY COLUMN patient_id INT NOT NULL COMMENT '患者ID'");
        execute(connection, "ALTER TABLE treatment MODIFY COLUMN patient_id INT NOT NULL COMMENT '患者ID'");
        execute(connection, "ALTER TABLE medical_records MODIFY COLUMN patient_id INT NOT NULL COMMENT '患者ID'");
        execute(connection, "ALTER TABLE lab_orders MODIFY COLUMN patient_id INT NOT NULL COMMENT '患者ID'");
        execute(connection, "ALTER TABLE patients MODIFY COLUMN related_patient_id INT DEFAULT NULL COMMENT '关联患者ID'");

        ensureForeignKey(connection, "appointment", "fk_appointment_patient",
                "ALTER TABLE appointment ADD CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id)");
        ensureForeignKey(connection, "treatment", "fk_treatment_patient",
                "ALTER TABLE treatment ADD CONSTRAINT fk_treatment_patient FOREIGN KEY (patient_id) REFERENCES patients(id)");
        ensureForeignKey(connection, "medical_records", "fk_medical_records_patient",
                "ALTER TABLE medical_records ADD CONSTRAINT fk_medical_records_patient FOREIGN KEY (patient_id) REFERENCES patients(id)");
        ensureForeignKey(connection, "lab_orders", "fk_lab_orders_patient",
                "ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_patient FOREIGN KEY (patient_id) REFERENCES patients(id)");
        ensureForeignKey(connection, "patients", "fk_patients_related_patient",
                "ALTER TABLE patients ADD CONSTRAINT fk_patients_related_patient FOREIGN KEY (related_patient_id) REFERENCES patients(id) ON DELETE SET NULL");
    }

    private void rebindOrphanReferenceByUniqueName(Connection connection,
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
                LEFT JOIN patients existing ON existing.id = t.%s
                SET t.%s = p.patient_id
                WHERE t.%s IS NOT NULL
                  AND t.%s > 0
                  AND existing.id IS NULL
                  AND TRIM(COALESCE(t.%s, '')) <> ''
                """.formatted(tableName, nameColumn, idColumn, idColumn, idColumn, idColumn, nameColumn));
    }

    private void ensurePatientReferencesReady(Connection connection,
                                              String tableName,
                                              String columnName,
                                              boolean requireNotNull) throws SQLException {
        if (!tableExists(connection, tableName)
                || !tableExists(connection, "patients")
                || !columnExists(connection, tableName, columnName)) {
            return;
        }
        if (requireNotNull) {
            long unresolved = queryCount(connection, """
                    SELECT COUNT(1)
                    FROM %s
                    WHERE %s IS NULL OR %s <= 0
                    """.formatted(tableName, columnName, columnName));
            if (unresolved > 0) {
                throw new SQLException(tableName + "." + columnName + " 仍有 " + unresolved + " 条空值/非法值，禁止收紧约束");
            }
        }
        long orphans = queryCount(connection, """
                SELECT COUNT(1)
                FROM %s t
                LEFT JOIN patients p ON p.id = t.%s
                WHERE t.%s IS NOT NULL
                  AND t.%s > 0
                  AND p.id IS NULL
                """.formatted(tableName, columnName, columnName, columnName));
        if (orphans > 0) {
            throw new SQLException(tableName + "." + columnName + " 仍有 " + orphans + " 条孤儿引用，禁止添加外键");
        }
    }

    private long queryCount(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private void ensureForeignKey(Connection connection,
                                  String tableName,
                                  String constraintName,
                                  String sql) throws SQLException {
        if (!tableExists(connection, tableName) || foreignKeyExists(connection, tableName, constraintName)) {
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

    private boolean foreignKeyExists(Connection connection, String tableName, String constraintName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (rs.next()) {
                String currentName = rs.getString("FK_NAME");
                if (currentName != null && constraintName.equalsIgnoreCase(currentName)) {
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
