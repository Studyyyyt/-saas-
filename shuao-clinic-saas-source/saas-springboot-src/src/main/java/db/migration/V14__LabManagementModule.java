package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V14__LabManagementModule extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createTables(connection);
    }

    private void createTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_factories (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    name VARCHAR(100) NOT NULL COMMENT '加工厂名称',
                    contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
                    contact_phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
                    address VARCHAR(255) DEFAULT NULL COMMENT '地址',
                    cooperation_start_date DATE DEFAULT NULL COMMENT '合作开始日期',
                    status VARCHAR(20) NOT NULL DEFAULT '合作中' COMMENT '状态：合作中/已停止合作',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_lab_factories_name (name),
                    KEY idx_lab_factories_status (status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='义齿加工厂档案'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_factory_products (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    factory_id BIGINT NOT NULL COMMENT '加工厂ID',
                    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
                    product_spec VARCHAR(100) DEFAULT NULL COMMENT '产品规格',
                    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
                    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
                    status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_lab_factory_products_factory_id (factory_id),
                    KEY idx_lab_factory_products_name (product_name),
                    KEY idx_lab_factory_products_status (status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加工厂产品价格表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_orders (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    factory_id BIGINT NOT NULL COMMENT '加工厂ID',
                    factory_name VARCHAR(100) NOT NULL COMMENT '加工厂名称冗余',
                    patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
                    patient_name VARCHAR(50) DEFAULT NULL COMMENT '患者姓名冗余',
                    treatment_id BIGINT DEFAULT NULL COMMENT '关联治疗ID',
                    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
                    product_spec VARCHAR(100) DEFAULT NULL COMMENT '产品规格',
                    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
                    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
                    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
                    order_date DATE NOT NULL COMMENT '下单日期',
                    expected_delivery_date DATE DEFAULT NULL COMMENT '预计完成日期',
                    actual_delivery_date DATE DEFAULT NULL COMMENT '实际收货日期',
                    status VARCHAR(20) NOT NULL DEFAULT '已下单' COMMENT '状态：已下单/加工中/已完成/已收货/已对账',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_lab_orders_factory_id (factory_id),
                    KEY idx_lab_orders_patient_id (patient_id),
                    KEY idx_lab_orders_treatment_id (treatment_id),
                    KEY idx_lab_orders_status (status),
                    KEY idx_lab_orders_order_date (order_date),
                    KEY idx_lab_orders_expected_delivery_date (expected_delivery_date),
                    KEY idx_lab_orders_actual_delivery_date (actual_delivery_date),
                    KEY idx_lab_orders_patient_name (patient_name),
                    KEY idx_lab_orders_product_name (product_name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='义齿加工订单'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_bill_templates (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    factory_id BIGINT NOT NULL COMMENT '加工厂ID',
                    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
                    column_mapping TEXT NOT NULL COMMENT '列映射JSON',
                    header_row INT NOT NULL DEFAULT 1 COMMENT '表头行号',
                    data_start_row INT NOT NULL DEFAULT 2 COMMENT '数据起始行号',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_lab_bill_templates_factory_id (factory_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='义齿账单模板配置'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_bills (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    factory_id BIGINT NOT NULL COMMENT '加工厂ID',
                    factory_name VARCHAR(100) NOT NULL COMMENT '加工厂名称冗余',
                    template_id BIGINT DEFAULT NULL COMMENT '使用模板ID',
                    bill_month VARCHAR(7) NOT NULL COMMENT '账单月份YYYY-MM',
                    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '账单总金额',
                    bill_file_url VARCHAR(255) DEFAULT NULL COMMENT '原始文件路径',
                    status VARCHAR(20) NOT NULL DEFAULT '待对账' COMMENT '状态：待对账/对账中/已完成对账',
                    matched_count INT NOT NULL DEFAULT 0 COMMENT '完全匹配条数',
                    mismatched_count INT NOT NULL DEFAULT 0 COMMENT '数量/金额不符条数',
                    only_in_system_count INT NOT NULL DEFAULT 0 COMMENT '仅系统有条数',
                    only_in_bill_count INT NOT NULL DEFAULT 0 COMMENT '仅账单有条数',
                    imported_by BIGINT DEFAULT NULL COMMENT '导入人ID',
                    imported_by_name VARCHAR(50) DEFAULT NULL COMMENT '导入人姓名',
                    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
                    confirmed_by BIGINT DEFAULT NULL COMMENT '确认人ID',
                    confirmed_by_name VARCHAR(50) DEFAULT NULL COMMENT '确认人姓名',
                    confirmed_at DATETIME DEFAULT NULL COMMENT '确认时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_lab_bills_factory_month (factory_id, bill_month),
                    KEY idx_lab_bills_status (status),
                    KEY idx_lab_bills_bill_month (bill_month)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='义齿月度账单'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_bill_items (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    bill_id BIGINT NOT NULL COMMENT '账单ID',
                    raw_row_number INT DEFAULT NULL COMMENT '原始Excel行号',
                    product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名称',
                    product_spec VARCHAR(100) DEFAULT NULL COMMENT '产品规格',
                    quantity INT DEFAULT 0 COMMENT '数量',
                    unit_price DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
                    total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '金额',
                    delivery_date DATE DEFAULT NULL COMMENT '送货日期',
                    patient_name VARCHAR(50) DEFAULT NULL COMMENT '患者姓名',
                    match_status VARCHAR(20) NOT NULL DEFAULT '仅账单有' COMMENT '匹配状态：完全匹配/数量不符/金额不符/仅账单有',
                    matched_lab_order_id BIGINT DEFAULT NULL COMMENT '匹配到的系统订单ID',
                    resolution_status VARCHAR(20) NOT NULL DEFAULT '待处理' COMMENT '异常处理状态：待处理/已处理/已忽略/无需处理',
                    resolution_remark VARCHAR(200) DEFAULT NULL COMMENT '处理备注',
                    resolved_by BIGINT DEFAULT NULL COMMENT '处理人ID',
                    resolved_by_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
                    resolved_at DATETIME DEFAULT NULL COMMENT '处理时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    PRIMARY KEY (id),
                    KEY idx_lab_bill_items_bill_id (bill_id),
                    KEY idx_lab_bill_items_match_status (match_status),
                    KEY idx_lab_bill_items_matched_order (matched_lab_order_id),
                    KEY idx_lab_bill_items_resolution_status (resolution_status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='义齿账单条目'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS lab_bill_unmatched_orders (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    bill_id BIGINT NOT NULL COMMENT '账单ID',
                    lab_order_id BIGINT NOT NULL COMMENT '系统订单ID',
                    resolution_status VARCHAR(20) NOT NULL DEFAULT '待处理' COMMENT '异常处理状态：待处理/已处理/已忽略',
                    resolution_remark VARCHAR(200) DEFAULT NULL COMMENT '处理备注',
                    resolved_by BIGINT DEFAULT NULL COMMENT '处理人ID',
                    resolved_by_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
                    resolved_at DATETIME DEFAULT NULL COMMENT '处理时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    PRIMARY KEY (id),
                    KEY idx_lab_bill_unmatched_orders_bill_id (bill_id),
                    KEY idx_lab_bill_unmatched_orders_order_id (lab_order_id),
                    KEY idx_lab_bill_unmatched_orders_resolution_status (resolution_status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='义齿账单仅系统有订单'
                """);
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
