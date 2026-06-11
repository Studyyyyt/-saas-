package db.migration;

import com.example.springboot.util.PatientSearchUtils;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V21__PatientSearchTokens extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ensureColumns(connection);
        ensureIndexes(connection);
        backfillTokens(connection);
    }

    private void ensureColumns(Connection connection) throws SQLException {
        ensureColumn(connection, "patients", "name_pinyin",
                "ALTER TABLE patients ADD COLUMN name_pinyin VARCHAR(200) DEFAULT NULL COMMENT '姓名全拼搜索字段' AFTER name");
        ensureColumn(connection, "patients", "name_initials",
                "ALTER TABLE patients ADD COLUMN name_initials VARCHAR(80) DEFAULT NULL COMMENT '姓名首拼搜索字段' AFTER name_pinyin");
    }

    private void ensureIndexes(Connection connection) throws SQLException {
        ensureIndex(connection, "patients", "idx_patients_name_pinyin",
                "CREATE INDEX idx_patients_name_pinyin ON patients (name_pinyin)");
        ensureIndex(connection, "patients", "idx_patients_name_initials",
                "CREATE INDEX idx_patients_name_initials ON patients (name_initials)");
    }

    private void backfillTokens(Connection connection) throws SQLException {
        if (!tableExists(connection, "patients")) {
            return;
        }
        try (PreparedStatement select = connection.prepareStatement("SELECT id, name FROM patients");
             ResultSet rs = select.executeQuery();
             PreparedStatement update = connection.prepareStatement("UPDATE patients SET name_pinyin = ?, name_initials = ? WHERE id = ?")) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                update.setString(1, normalizeToken(PatientSearchUtils.toPinyin(name)));
                update.setString(2, normalizeToken(PatientSearchUtils.toInitials(name)));
                update.setLong(3, id);
                update.addBatch();
            }
            update.executeBatch();
        }
    }

    private String normalizeToken(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
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
