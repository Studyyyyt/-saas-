package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V24__PatientForeignKeysPhase2 extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        deletePatientOrphans(connection, "patient_images");
        deletePatientOrphans(connection, "patient_consent");
        deletePatientOrphans(connection, "patient_followup");
        deletePatientOrphans(connection, "patient_risk_tag");
        deletePatientOrphans(connection, "patient_timeline");
        deletePatientOrphans(connection, "patient_wechat_bind_scene");
        deletePatientOrphans(connection, "insurance_patient_profile");
        deletePatientOrphans(connection, "insurance_settlement");

        nullifyPatientOrphans(connection, "finances");
        nullifyPatientOrphans(connection, "consultation_records");

        ensurePatientReferencesReady(connection, "patient_images", true);
        ensurePatientReferencesReady(connection, "patient_consent", true);
        ensurePatientReferencesReady(connection, "patient_followup", true);
        ensurePatientReferencesReady(connection, "patient_risk_tag", true);
        ensurePatientReferencesReady(connection, "patient_timeline", true);
        ensurePatientReferencesReady(connection, "patient_wechat_bind_scene", true);
        ensurePatientReferencesReady(connection, "insurance_patient_profile", true);
        ensurePatientReferencesReady(connection, "insurance_settlement", true);
        ensurePatientReferencesReady(connection, "finances", false);
        ensurePatientReferencesReady(connection, "consultation_records", false);

        alterPatientIdColumn(connection, "patient_images", false);
        alterPatientIdColumn(connection, "patient_consent", false);
        alterPatientIdColumn(connection, "patient_followup", false);
        alterPatientIdColumn(connection, "patient_risk_tag", false);
        alterPatientIdColumn(connection, "patient_timeline", false);
        alterPatientIdColumn(connection, "patient_wechat_bind_scene", false);
        alterPatientIdColumn(connection, "insurance_patient_profile", false);
        alterPatientIdColumn(connection, "insurance_settlement", false);
        alterPatientIdColumn(connection, "finances", true);
        alterPatientIdColumn(connection, "consultation_records", true);

        ensureForeignKey(connection, "patient_images", "fk_patient_images_patient",
                "ALTER TABLE patient_images ADD CONSTRAINT fk_patient_images_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "patient_consent", "fk_patient_consent_patient",
                "ALTER TABLE patient_consent ADD CONSTRAINT fk_patient_consent_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "patient_followup", "fk_patient_followup_patient",
                "ALTER TABLE patient_followup ADD CONSTRAINT fk_patient_followup_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "patient_risk_tag", "fk_patient_risk_tag_patient",
                "ALTER TABLE patient_risk_tag ADD CONSTRAINT fk_patient_risk_tag_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "patient_timeline", "fk_patient_timeline_patient",
                "ALTER TABLE patient_timeline ADD CONSTRAINT fk_patient_timeline_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "patient_wechat_bind_scene", "fk_patient_wechat_bind_scene_patient",
                "ALTER TABLE patient_wechat_bind_scene ADD CONSTRAINT fk_patient_wechat_bind_scene_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "insurance_patient_profile", "fk_insurance_patient_profile_patient",
                "ALTER TABLE insurance_patient_profile ADD CONSTRAINT fk_insurance_patient_profile_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "insurance_settlement", "fk_insurance_settlement_patient",
                "ALTER TABLE insurance_settlement ADD CONSTRAINT fk_insurance_settlement_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "finances", "fk_finances_patient",
                "ALTER TABLE finances ADD CONSTRAINT fk_finances_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "consultation_records", "fk_consultation_records_patient_relaxed",
                "ALTER TABLE consultation_records ADD CONSTRAINT fk_consultation_records_patient_relaxed FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL");
    }

    private void deletePatientOrphans(Connection connection, String tableName) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, "patient_id") || !tableExists(connection, "patients")) {
            return;
        }
        execute(connection, """
                DELETE t
                FROM %s t
                LEFT JOIN patients p ON p.id = t.patient_id
                WHERE t.patient_id IS NOT NULL
                  AND t.patient_id > 0
                  AND p.id IS NULL
                """.formatted(tableName));
    }

    private void nullifyPatientOrphans(Connection connection, String tableName) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, "patient_id") || !tableExists(connection, "patients")) {
            return;
        }
        execute(connection, """
                UPDATE %s t
                LEFT JOIN patients p ON p.id = t.patient_id
                SET t.patient_id = NULL
                WHERE t.patient_id IS NOT NULL
                  AND t.patient_id > 0
                  AND p.id IS NULL
                """.formatted(tableName));
    }

    private void ensurePatientReferencesReady(Connection connection, String tableName, boolean requireNotNull) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, "patient_id")) {
            return;
        }
        if (requireNotNull) {
            long unresolved = queryCount(connection, """
                    SELECT COUNT(1)
                    FROM %s
                    WHERE patient_id IS NULL OR patient_id <= 0
                    """.formatted(tableName));
            if (unresolved > 0) {
                throw new SQLException(tableName + ".patient_id 仍有 " + unresolved + " 条空值/非法值，禁止收紧约束");
            }
        }
        long orphans = queryCount(connection, """
                SELECT COUNT(1)
                FROM %s t
                LEFT JOIN patients p ON p.id = t.patient_id
                WHERE t.patient_id IS NOT NULL
                  AND t.patient_id > 0
                  AND p.id IS NULL
                """.formatted(tableName));
        if (orphans > 0) {
            throw new SQLException(tableName + ".patient_id 仍有 " + orphans + " 条孤儿引用，禁止添加外键");
        }
    }

    private void alterPatientIdColumn(Connection connection, String tableName, boolean nullable) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, "patient_id")) {
            return;
        }
        String nullSql = nullable ? "DEFAULT NULL" : "NOT NULL";
        String comment = readColumnComment(connection, tableName, "patient_id");
        String commentSql = comment == null || comment.isBlank() ? "" : " COMMENT '" + comment.replace("'", "''") + "'";
        execute(connection, "ALTER TABLE " + tableName + " MODIFY COLUMN patient_id INT " + nullSql + commentSql);
    }

    private String readColumnComment(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            if (rs.next()) {
                return rs.getString("REMARKS");
            }
        }
        return null;
    }

    private long queryCount(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private void ensureForeignKey(Connection connection, String tableName, String constraintName, String sql) throws SQLException {
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
