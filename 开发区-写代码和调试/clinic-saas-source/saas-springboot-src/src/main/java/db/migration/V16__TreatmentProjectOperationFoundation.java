package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V16__TreatmentProjectOperationFoundation extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createTables(connection);
    }

    private void createTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS treatment_project_categories (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    name VARCHAR(100) NOT NULL COMMENT '分类名称',
                    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0=一级分类',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
                    status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用/已删除',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
                    updated_by BIGINT DEFAULT NULL COMMENT '修改人ID',
                    updated_by_name VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_treatment_project_categories_parent_name (parent_id, name),
                    KEY idx_treatment_project_categories_parent_id (parent_id),
                    KEY idx_treatment_project_categories_status (status),
                    KEY idx_treatment_project_categories_sort_order (sort_order)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗项目分类'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS treatment_projects (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    legacy_treatment_catalog_id BIGINT DEFAULT NULL COMMENT '历史treatment_catalog ID',
                    project_code VARCHAR(64) NOT NULL COMMENT '项目编码',
                    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
                    category_id BIGINT DEFAULT NULL COMMENT '所属分类ID',
                    category_path VARCHAR(200) DEFAULT NULL COMMENT '分类路径',
                    default_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '默认价格',
                    estimated_visit_count INT NOT NULL DEFAULT 1 COMMENT '预计治疗次数',
                    estimated_cycle_days INT NOT NULL DEFAULT 0 COMMENT '预计周期天数',
                    status VARCHAR(20) NOT NULL DEFAULT '在用' COMMENT '状态：在用/停用',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
                    updated_by BIGINT DEFAULT NULL COMMENT '修改人ID',
                    updated_by_name VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_treatment_projects_code (project_code),
                    KEY idx_treatment_projects_legacy_catalog_id (legacy_treatment_catalog_id),
                    KEY idx_treatment_projects_category_id (category_id),
                    KEY idx_treatment_projects_status (status),
                    KEY idx_treatment_projects_sort_order (sort_order),
                    KEY idx_treatment_projects_name (project_name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗项目库'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS treatment_operations (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    operation_code VARCHAR(64) NOT NULL COMMENT '操作编码',
                    operation_name VARCHAR(100) NOT NULL COMMENT '操作名称',
                    operation_category VARCHAR(100) NOT NULL DEFAULT '' COMMENT '操作大类',
                    need_lab_processing TINYINT NOT NULL DEFAULT 0 COMMENT '是否触发外加工：0否1是',
                    default_processing_days INT NOT NULL DEFAULT 0 COMMENT '默认加工天数',
                    status VARCHAR(20) NOT NULL DEFAULT '在用' COMMENT '状态：在用/停用',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
                    updated_by BIGINT DEFAULT NULL COMMENT '修改人ID',
                    updated_by_name VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_treatment_operations_code (operation_code),
                    KEY idx_treatment_operations_name (operation_name),
                    KEY idx_treatment_operations_category (operation_category),
                    KEY idx_treatment_operations_need_lab (need_lab_processing),
                    KEY idx_treatment_operations_status (status),
                    KEY idx_treatment_operations_sort_order (sort_order)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗操作字典'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS project_operation_relations (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    project_id BIGINT NOT NULL COMMENT '项目ID',
                    operation_id BIGINT NOT NULL COMMENT '操作ID',
                    operation_order INT NOT NULL DEFAULT 0 COMMENT '项目内操作顺序',
                    is_required TINYINT NOT NULL DEFAULT 1 COMMENT '是否必经：0否1是',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_project_operation_relations (project_id, operation_id),
                    KEY idx_project_operation_relations_operation_id (operation_id),
                    KEY idx_project_operation_relations_order (project_id, operation_order)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目-操作关联表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS medical_record_operations (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    medical_record_id BIGINT NOT NULL COMMENT '病历ID',
                    project_id BIGINT DEFAULT NULL COMMENT '所属项目ID',
                    project_name VARCHAR(100) DEFAULT NULL COMMENT '所属项目名称冗余',
                    operation_id BIGINT NOT NULL COMMENT '操作字典ID',
                    operation_name VARCHAR(100) NOT NULL COMMENT '操作名称冗余',
                    tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位，可空',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注，可空',
                    lab_order_status TINYINT NOT NULL DEFAULT 0 COMMENT '加工登记状态：0未登记1已登记2本次跳过',
                    skip_reason VARCHAR(100) DEFAULT NULL COMMENT '跳过原因',
                    lab_order_registered_at DATETIME DEFAULT NULL COMMENT '首次登记加工时间',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
                    updated_by BIGINT DEFAULT NULL COMMENT '修改人ID',
                    updated_by_name VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                    PRIMARY KEY (id),
                    KEY idx_mro_medical_record_id (medical_record_id),
                    KEY idx_mro_project_id (project_id),
                    KEY idx_mro_operation_id (operation_id),
                    KEY idx_mro_lab_order_status (lab_order_status),
                    KEY idx_mro_pending_lab (lab_order_status, created_at),
                    KEY idx_mro_medical_record_project (medical_record_id, project_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历操作记录表'
                """);
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
