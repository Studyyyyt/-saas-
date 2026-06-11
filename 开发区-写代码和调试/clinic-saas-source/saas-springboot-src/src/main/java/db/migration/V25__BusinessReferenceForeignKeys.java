package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V25__BusinessReferenceForeignKeys extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        ensureReferenceReady(connection, "finances", "treatment_id", "treatment", "id", true);
        ensureReferenceReady(connection, "finances", "payment_channel_id", "payment_channel", "id", true);
        ensureReferenceReady(connection, "insurance_settlement", "finance_id", "finances", "id", true);
        ensureReferenceReady(connection, "insurance_settlement", "treatment_id", "treatment", "id", true);
        ensureReferenceReady(connection, "treatment", "project_id", "treatment_projects", "id", true);
        ensureReferenceReady(connection, "medical_record_operations", "medical_record_id", "medical_records", "id", false);
        ensureReferenceReady(connection, "project_operation_relations", "project_id", "treatment_projects", "id", false);
        ensureReferenceReady(connection, "project_operation_relations", "operation_id", "treatment_operations", "id", false);
        ensureReferenceReady(connection, "lab_orders", "treatment_id", "treatment", "id", true);
        ensureReferenceReady(connection, "lab_orders", "medical_record_id", "medical_records", "id", true);
        ensureReferenceReady(connection, "lab_orders", "medical_record_operation_id", "medical_record_operations", "id", true);
        ensureReferenceReady(connection, "lab_orders", "project_id", "treatment_projects", "id", true);
        ensureReferenceReady(connection, "lab_orders", "operation_id", "treatment_operations", "id", true);

        alterColumnType(connection, "insurance_settlement", "finance_id", "INT DEFAULT NULL");

        ensureForeignKey(connection, "finances", "fk_finances_treatment",
                "ALTER TABLE finances ADD CONSTRAINT fk_finances_treatment FOREIGN KEY (treatment_id) REFERENCES treatment(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "finances", "fk_finances_payment_channel",
                "ALTER TABLE finances ADD CONSTRAINT fk_finances_payment_channel FOREIGN KEY (payment_channel_id) REFERENCES payment_channel(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "insurance_settlement", "fk_insurance_settlement_finance",
                "ALTER TABLE insurance_settlement ADD CONSTRAINT fk_insurance_settlement_finance FOREIGN KEY (finance_id) REFERENCES finances(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "insurance_settlement", "fk_insurance_settlement_treatment",
                "ALTER TABLE insurance_settlement ADD CONSTRAINT fk_insurance_settlement_treatment FOREIGN KEY (treatment_id) REFERENCES treatment(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "treatment", "fk_treatment_project",
                "ALTER TABLE treatment ADD CONSTRAINT fk_treatment_project FOREIGN KEY (project_id) REFERENCES treatment_projects(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "medical_record_operations", "fk_mro_medical_record",
                "ALTER TABLE medical_record_operations ADD CONSTRAINT fk_mro_medical_record FOREIGN KEY (medical_record_id) REFERENCES medical_records(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "project_operation_relations", "fk_por_project",
                "ALTER TABLE project_operation_relations ADD CONSTRAINT fk_por_project FOREIGN KEY (project_id) REFERENCES treatment_projects(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "project_operation_relations", "fk_por_operation",
                "ALTER TABLE project_operation_relations ADD CONSTRAINT fk_por_operation FOREIGN KEY (operation_id) REFERENCES treatment_operations(id) ON DELETE CASCADE");
        ensureForeignKey(connection, "lab_orders", "fk_lab_orders_treatment",
                "ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_treatment FOREIGN KEY (treatment_id) REFERENCES treatment(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "lab_orders", "fk_lab_orders_medical_record",
                "ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_medical_record FOREIGN KEY (medical_record_id) REFERENCES medical_records(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "lab_orders", "fk_lab_orders_mro",
                "ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_mro FOREIGN KEY (medical_record_operation_id) REFERENCES medical_record_operations(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "lab_orders", "fk_lab_orders_project",
                "ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_project FOREIGN KEY (project_id) REFERENCES treatment_projects(id) ON DELETE SET NULL");
        ensureForeignKey(connection, "lab_orders", "fk_lab_orders_operation",
                "ALTER TABLE lab_orders ADD CONSTRAINT fk_lab_orders_operation FOREIGN KEY (operation_id) REFERENCES treatment_operations(id) ON DELETE SET NULL");
    }

    private void ensureReferenceReady(Connection connection,
                                      String tableName,
                                      String columnName,
                                      String referencedTable,
                                      String referencedColumn,
                                      boolean nullableAllowed) throws SQLException {
        if (!tableExists(connection, tableName)
                || !tableExists(connection, referencedTable)
                || !columnExists(connection, tableName, columnName)
                || !columnExists(connection, referencedTable, referencedColumn)) {
            return;
        }
        if (!nullableAllowed) {
            long invalid = queryCount(connection, """
                    SELECT COUNT(1)
                    FROM %s
                    WHERE %s IS NULL OR %s <= 0
                    """.formatted(tableName, columnName, columnName));
            if (invalid > 0) {
                throw new SQLException(tableName + "." + columnName + " 仍有 " + invalid + " 条空值/非法值，禁止添加强外键");
            }
        }
        long orphans = queryCount(connection, """
                SELECT COUNT(1)
                FROM %s t
                LEFT JOIN %s r ON r.%s = t.%s
                WHERE t.%s IS NOT NULL
                  AND t.%s > 0
                  AND r.%s IS NULL
                """.formatted(tableName, referencedTable, referencedColumn, columnName, columnName, columnName, referencedColumn));
        if (orphans > 0) {
            throw new SQLException(tableName + "." + columnName + " 仍有 " + orphans + " 条孤儿引用，禁止添加外键");
        }
    }

    private void alterColumnType(Connection connection, String tableName, String columnName, String sqlType) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, columnName)) {
            return;
        }
        execute(connection, "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + sqlType);
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
