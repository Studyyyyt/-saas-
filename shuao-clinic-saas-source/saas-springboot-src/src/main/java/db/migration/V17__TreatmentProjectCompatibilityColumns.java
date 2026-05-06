package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V17__TreatmentProjectCompatibilityColumns extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureTreatmentColumns(connection);
        ensureLabOrderColumns(connection);
    }

    private void ensureTreatmentColumns(Connection connection) throws SQLException {
        ensureColumn(connection, "treatment", "project_id",
                "ALTER TABLE treatment ADD COLUMN project_id BIGINT DEFAULT NULL COMMENT '项目库ID' AFTER batch_no");
        ensureIndex(connection, "treatment", "idx_treatment_project_id",
                "CREATE INDEX idx_treatment_project_id ON treatment (project_id)");
    }

    private void ensureLabOrderColumns(Connection connection) throws SQLException {
        ensureColumn(connection, "lab_orders", "medical_record_operation_id",
                "ALTER TABLE lab_orders ADD COLUMN medical_record_operation_id BIGINT DEFAULT NULL COMMENT '关联病历操作记录ID' AFTER treatment_id");
        ensureColumn(connection, "lab_orders", "medical_record_id",
                "ALTER TABLE lab_orders ADD COLUMN medical_record_id BIGINT DEFAULT NULL COMMENT '关联病历ID冗余' AFTER medical_record_operation_id");
        ensureColumn(connection, "lab_orders", "project_id",
                "ALTER TABLE lab_orders ADD COLUMN project_id BIGINT DEFAULT NULL COMMENT '项目库ID冗余' AFTER medical_record_id");
        ensureColumn(connection, "lab_orders", "project_name",
                "ALTER TABLE lab_orders ADD COLUMN project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称冗余' AFTER project_id");
        ensureColumn(connection, "lab_orders", "operation_id",
                "ALTER TABLE lab_orders ADD COLUMN operation_id BIGINT DEFAULT NULL COMMENT '操作字典ID冗余' AFTER project_name");
        ensureColumn(connection, "lab_orders", "operation_name",
                "ALTER TABLE lab_orders ADD COLUMN operation_name VARCHAR(100) DEFAULT NULL COMMENT '操作名称冗余' AFTER operation_id");
        ensureColumn(connection, "lab_orders", "tooth_positions",
                "ALTER TABLE lab_orders ADD COLUMN tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位冗余' AFTER operation_name");
        ensureIndex(connection, "lab_orders", "idx_lab_orders_medical_record_operation_id",
                "CREATE INDEX idx_lab_orders_medical_record_operation_id ON lab_orders (medical_record_operation_id)");
        ensureIndex(connection, "lab_orders", "idx_lab_orders_medical_record_id",
                "CREATE INDEX idx_lab_orders_medical_record_id ON lab_orders (medical_record_id)");
        ensureIndex(connection, "lab_orders", "idx_lab_orders_project_id",
                "CREATE INDEX idx_lab_orders_project_id ON lab_orders (project_id)");
        ensureIndex(connection, "lab_orders", "idx_lab_orders_operation_id",
                "CREATE INDEX idx_lab_orders_operation_id ON lab_orders (operation_id)");
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || columnExists(connection, tableName, columnName)) {
            return;
        }
        execute(connection, sql);
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
