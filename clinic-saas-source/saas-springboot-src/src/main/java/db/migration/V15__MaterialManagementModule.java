package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V15__MaterialManagementModule extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createTables(connection);
        seedRootCategories(connection);
    }

    private void createTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS material_categories (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    name VARCHAR(100) NOT NULL COMMENT '分类名称',
                    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
                    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
                    status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用/已删除',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_material_categories_parent_id (parent_id),
                    KEY idx_material_categories_status (status),
                    KEY idx_material_categories_sort_order (sort_order)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材分类'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS materials (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    name VARCHAR(100) NOT NULL COMMENT '耗材名称',
                    spec VARCHAR(100) DEFAULT NULL COMMENT '规格',
                    brand VARCHAR(100) DEFAULT NULL COMMENT '品牌',
                    category_id BIGINT NOT NULL COMMENT '分类ID',
                    category_name VARCHAR(100) DEFAULT NULL COMMENT '分类名称冗余',
                    unit VARCHAR(20) DEFAULT NULL COMMENT '计量单位',
                    min_stock_alert INT NOT NULL DEFAULT 0 COMMENT '最低库存预警值',
                    current_stock INT NOT NULL DEFAULT 0 COMMENT '当前库存',
                    status VARCHAR(20) NOT NULL DEFAULT '在用' COMMENT '状态：在用/停用',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_materials_category_id (category_id),
                    KEY idx_materials_status (status),
                    KEY idx_materials_name (name),
                    KEY idx_materials_brand (brand)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材档案'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS material_purchases (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    supplier_name VARCHAR(100) DEFAULT NULL COMMENT '供应商名称',
                    purchase_date DATE NOT NULL COMMENT '采购日期',
                    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总金额',
                    payment_method VARCHAR(20) DEFAULT NULL COMMENT '付款方式',
                    invoice_image_url VARCHAR(255) DEFAULT NULL COMMENT '发票/采购单图片',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    finance_record_id BIGINT DEFAULT NULL COMMENT '关联财务记录ID',
                    status VARCHAR(20) NOT NULL DEFAULT '有效' COMMENT '状态：有效/已作废',
                    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
                    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
                    voided_by BIGINT DEFAULT NULL COMMENT '作废人ID',
                    voided_by_name VARCHAR(50) DEFAULT NULL COMMENT '作废人姓名',
                    voided_at DATETIME DEFAULT NULL COMMENT '作废时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    KEY idx_material_purchases_purchase_date (purchase_date),
                    KEY idx_material_purchases_supplier_name (supplier_name),
                    KEY idx_material_purchases_status (status),
                    KEY idx_material_purchases_finance_record_id (finance_record_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材采购单'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS material_purchase_items (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    purchase_id BIGINT NOT NULL COMMENT '采购单ID',
                    material_id BIGINT NOT NULL COMMENT '耗材ID',
                    material_name VARCHAR(100) DEFAULT NULL COMMENT '耗材名称冗余',
                    material_spec VARCHAR(100) DEFAULT NULL COMMENT '耗材规格冗余',
                    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '单价',
                    quantity INT NOT NULL DEFAULT 0 COMMENT '数量',
                    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '小计',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    PRIMARY KEY (id),
                    KEY idx_material_purchase_items_purchase_id (purchase_id),
                    KEY idx_material_purchase_items_material_id (material_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材采购单明细'
                """);
    }

    private void seedRootCategories(Connection connection) throws SQLException {
        insertRootCategory(connection, "种植类", 10);
        insertRootCategory(connection, "正畸类", 20);
        insertRootCategory(connection, "修复类", 30);
        insertRootCategory(connection, "基础耗材", 40);
        insertRootCategory(connection, "其他", 50);
    }

    private void insertRootCategory(Connection connection, String name, int sortOrder) throws SQLException {
        execute(connection, """
                INSERT INTO material_categories(name, parent_id, sort_order, status)
                SELECT ?, 0, ?, '启用'
                FROM DUAL
                WHERE NOT EXISTS (
                    SELECT 1 FROM material_categories
                    WHERE parent_id = 0 AND name = ? AND status <> '已删除'
                )
                """, name, sortOrder, name);
    }

    private void execute(Connection connection, String sql, Object... args) throws SQLException {
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            statement.execute();
        }
    }
}
