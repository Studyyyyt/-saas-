package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V8__AppointmentDurationAndImageDelivery extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureAppointmentDuration(connection);
        ensurePatientImageDeliveryColumns(connection);
        ensurePatientImageDeliveryIndex(connection);
    }

    private void ensureAppointmentDuration(Connection connection) throws SQLException {
        if (!tableExists(connection, "appointment")) {
            return;
        }
        if (!columnExists(connection, "appointment", "duration_minutes")) {
            execute(connection, "ALTER TABLE appointment ADD COLUMN duration_minutes INT NOT NULL DEFAULT 60 COMMENT '预约时长（分钟）' AFTER appointment_time");
        }
        execute(connection, """
                UPDATE appointment
                SET duration_minutes = 60
                WHERE duration_minutes IS NULL OR duration_minutes <= 0
                """);
    }

    private void ensurePatientImageDeliveryColumns(Connection connection) throws SQLException {
        if (!tableExists(connection, "patient_images")) {
            return;
        }
        if (!columnExists(connection, "patient_images", "sent_to_patient")) {
            execute(connection, "ALTER TABLE patient_images ADD COLUMN sent_to_patient TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送给患者' AFTER notes");
        }
        if (!columnExists(connection, "patient_images", "sent_at")) {
            execute(connection, "ALTER TABLE patient_images ADD COLUMN sent_at DATETIME DEFAULT NULL COMMENT '发送时间' AFTER sent_to_patient");
        }
        execute(connection, """
                UPDATE patient_images
                SET sent_to_patient = 0
                WHERE sent_to_patient IS NULL
                """);
    }

    private void ensurePatientImageDeliveryIndex(Connection connection) throws SQLException {
        if (!tableExists(connection, "patient_images")
                || indexExists(connection, "patient_images", "idx_patient_images_sent")) {
            return;
        }
        execute(connection, "CREATE INDEX idx_patient_images_sent ON patient_images (patient_id, sent_to_patient)");
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
