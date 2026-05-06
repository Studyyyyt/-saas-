package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V13__ConsultationRecordsModule extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createConsultationTable(connection);
        ensureConsultationColumns(connection);
        ensureConsultationIndexes(connection);
        ensurePatientForeignKey(connection);
    }

    private void createConsultationTable(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS consultation_records (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT DEFAULT NULL COMMENT '关联患者ID',
                    consultation_time DATETIME NOT NULL COMMENT '咨询时间',
                    consultation_channel VARCHAR(30) NOT NULL COMMENT '咨询渠道',
                    chief_project VARCHAR(30) NOT NULL COMMENT '主诉项目',
                    intent_level VARCHAR(10) NOT NULL COMMENT '意向强度：高/中/低',
                    handling_result VARCHAR(20) NOT NULL DEFAULT '待跟进' COMMENT '处理结果：已预约到店/待跟进/不再跟进',
                    contact_name VARCHAR(50) DEFAULT NULL COMMENT '咨询人姓名/昵称',
                    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
                    remarks VARCHAR(200) DEFAULT NULL COMMENT '备注',
                    arrived_at DATETIME DEFAULT NULL COMMENT '首次进入已预约到店时间',
                    deal_at DATETIME DEFAULT NULL COMMENT '首次成交时间',
                    created_by BIGINT NOT NULL COMMENT '录入人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '录入人姓名',
                    updated_by BIGINT DEFAULT NULL COMMENT '最后更新人ID',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_consultation_time (consultation_time),
                    KEY idx_consultation_channel_time (consultation_channel, consultation_time),
                    KEY idx_contact_phone (contact_phone),
                    KEY idx_intent_result (intent_level, handling_result),
                    KEY idx_handling_result (handling_result),
                    KEY idx_arrived_at (arrived_at),
                    KEY idx_deal_at (deal_at),
                    KEY idx_patient_id (patient_id),
                    KEY idx_created_by (created_by)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询记录表'
                """);
    }

    private void ensureConsultationColumns(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_records")) {
            return;
        }
        ensureColumn(connection, "consultation_records", "patient_id",
                "ALTER TABLE consultation_records ADD COLUMN patient_id BIGINT DEFAULT NULL COMMENT '关联患者ID' AFTER id");
        ensureColumn(connection, "consultation_records", "consultation_time",
                "ALTER TABLE consultation_records ADD COLUMN consultation_time DATETIME NOT NULL COMMENT '咨询时间' AFTER patient_id");
        ensureColumn(connection, "consultation_records", "consultation_channel",
                "ALTER TABLE consultation_records ADD COLUMN consultation_channel VARCHAR(30) NOT NULL COMMENT '咨询渠道' AFTER consultation_time");
        ensureColumn(connection, "consultation_records", "chief_project",
                "ALTER TABLE consultation_records ADD COLUMN chief_project VARCHAR(30) NOT NULL COMMENT '主诉项目' AFTER consultation_channel");
        ensureColumn(connection, "consultation_records", "intent_level",
                "ALTER TABLE consultation_records ADD COLUMN intent_level VARCHAR(10) NOT NULL COMMENT '意向强度：高/中/低' AFTER chief_project");
        ensureColumn(connection, "consultation_records", "handling_result",
                "ALTER TABLE consultation_records ADD COLUMN handling_result VARCHAR(20) NOT NULL DEFAULT '待跟进' COMMENT '处理结果：已预约到店/待跟进/不再跟进' AFTER intent_level");
        ensureColumn(connection, "consultation_records", "contact_name",
                "ALTER TABLE consultation_records ADD COLUMN contact_name VARCHAR(50) DEFAULT NULL COMMENT '咨询人姓名/昵称' AFTER handling_result");
        ensureColumn(connection, "consultation_records", "contact_phone",
                "ALTER TABLE consultation_records ADD COLUMN contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系方式' AFTER contact_name");
        ensureColumn(connection, "consultation_records", "remarks",
                "ALTER TABLE consultation_records ADD COLUMN remarks VARCHAR(200) DEFAULT NULL COMMENT '备注' AFTER contact_phone");
        ensureColumn(connection, "consultation_records", "arrived_at",
                "ALTER TABLE consultation_records ADD COLUMN arrived_at DATETIME DEFAULT NULL COMMENT '首次进入已预约到店时间' AFTER remarks");
        ensureColumn(connection, "consultation_records", "deal_at",
                "ALTER TABLE consultation_records ADD COLUMN deal_at DATETIME DEFAULT NULL COMMENT '首次成交时间' AFTER arrived_at");
        ensureColumn(connection, "consultation_records", "created_by",
                "ALTER TABLE consultation_records ADD COLUMN created_by BIGINT NOT NULL DEFAULT 0 COMMENT '录入人ID' AFTER deal_at");
        ensureColumn(connection, "consultation_records", "created_by_name",
                "ALTER TABLE consultation_records ADD COLUMN created_by_name VARCHAR(50) DEFAULT NULL COMMENT '录入人姓名' AFTER created_by");
        ensureColumn(connection, "consultation_records", "updated_by",
                "ALTER TABLE consultation_records ADD COLUMN updated_by BIGINT DEFAULT NULL COMMENT '最后更新人ID' AFTER created_by_name");
        ensureColumn(connection, "consultation_records", "created_at",
                "ALTER TABLE consultation_records ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER updated_by");
        ensureColumn(connection, "consultation_records", "updated_at",
                "ALTER TABLE consultation_records ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at");
    }

    private void ensureConsultationIndexes(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_records")) {
            return;
        }
        ensureIndex(connection, "consultation_records", "idx_consultation_time",
                "CREATE INDEX idx_consultation_time ON consultation_records (consultation_time)");
        ensureIndex(connection, "consultation_records", "idx_consultation_channel_time",
                "CREATE INDEX idx_consultation_channel_time ON consultation_records (consultation_channel, consultation_time)");
        ensureIndex(connection, "consultation_records", "idx_contact_phone",
                "CREATE INDEX idx_contact_phone ON consultation_records (contact_phone)");
        ensureIndex(connection, "consultation_records", "idx_intent_result",
                "CREATE INDEX idx_intent_result ON consultation_records (intent_level, handling_result)");
        ensureIndex(connection, "consultation_records", "idx_handling_result",
                "CREATE INDEX idx_handling_result ON consultation_records (handling_result)");
        ensureIndex(connection, "consultation_records", "idx_arrived_at",
                "CREATE INDEX idx_arrived_at ON consultation_records (arrived_at)");
        ensureIndex(connection, "consultation_records", "idx_deal_at",
                "CREATE INDEX idx_deal_at ON consultation_records (deal_at)");
        ensureIndex(connection, "consultation_records", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON consultation_records (patient_id)");
        ensureIndex(connection, "consultation_records", "idx_created_by",
                "CREATE INDEX idx_created_by ON consultation_records (created_by)");
    }

    private void ensurePatientForeignKey(Connection connection) throws SQLException {
        if (!tableExists(connection, "consultation_records")
                || !tableExists(connection, "patients")
                || foreignKeyExists(connection, "consultation_records", "fk_consultation_patient")) {
            return;
        }
        if (!columnTypeCompatible(connection, "consultation_records", "patient_id", "patients", "id")) {
            return;
        }
        execute(connection,
                "ALTER TABLE consultation_records ADD CONSTRAINT fk_consultation_patient FOREIGN KEY (patient_id) REFERENCES patients(id)");
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

    private boolean columnTypeCompatible(Connection connection,
                                         String sourceTable,
                                         String sourceColumn,
                                         String targetTable,
                                         String targetColumn) throws SQLException {
        ColumnMeta sourceMeta = readColumnMeta(connection, sourceTable, sourceColumn);
        ColumnMeta targetMeta = readColumnMeta(connection, targetTable, targetColumn);
        if (sourceMeta == null || targetMeta == null) {
            return false;
        }
        return sourceMeta.sameType(targetMeta);
    }

    private ColumnMeta readColumnMeta(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return new ColumnMeta(
                            rs.getString("TYPE_NAME"),
                            rs.getInt("DATA_TYPE"),
                            rs.getInt("COLUMN_SIZE"),
                            rs.getInt("DECIMAL_DIGITS")
                    );
                }
            }
        }
        return null;
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

    private boolean foreignKeyExists(Connection connection, String tableName, String foreignKeyName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (rs.next()) {
                String currentName = rs.getString("FK_NAME");
                if (currentName != null && foreignKeyName.equalsIgnoreCase(currentName)) {
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

    private static class ColumnMeta {
        private final String typeName;
        private final int dataType;
        private final int columnSize;
        private final int decimalDigits;

        private ColumnMeta(String typeName, int dataType, int columnSize, int decimalDigits) {
            this.typeName = typeName == null ? "" : typeName.trim().toUpperCase();
            this.dataType = dataType;
            this.columnSize = columnSize;
            this.decimalDigits = decimalDigits;
        }

        private boolean sameType(ColumnMeta other) {
            return other != null
                    && dataType == other.dataType
                    && columnSize == other.columnSize
                    && decimalDigits == other.decimalDigits
                    && typeName.equals(other.typeName);
        }
    }
}
