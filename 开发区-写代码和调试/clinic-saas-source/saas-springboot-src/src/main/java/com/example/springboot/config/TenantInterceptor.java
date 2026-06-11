package com.example.springboot.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * MyBatis 多租户拦截器
 * 自动为 SELECT/UPDATE/DELETE 语句注入 clinic_id 条件
 * INSERT 语句由 Service 层手动控制 clinic_id
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TenantInterceptor implements Interceptor {

    /**
     * 白名单表：这些表不受 clinic_id 拦截（全局配置类表）
     */
    private static final Set<String> GLOBAL_TABLES = new HashSet<>(Arrays.asList(
        "flyway_schema_history",
        "ai_global_config",
        "ai_function_config",
        "ai_function_mapping",
        "ai_model_provider",
        "ai_prompt_template",
        "ai_few_shot_example",
        "ai_operation_log",
        "ai_function_call_log",
        "external_agent_config",
        "system_config",
        "role_menu_permissions",
        "clinic_info",
        "clinic",
        "user_clinic",
        "users",
        "roles",
        "api_key"
    ));

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String clinicId = ClinicContext.get();
        if (clinicId == null || clinicId.isEmpty()) {
            // 没有诊所上下文，不拦截（如系统初始化、开放接口）
            return invocation.proceed();
        }

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        String originalSql = (String) metaObject.getValue("delegate.boundSql.sql");

        if (originalSql == null || originalSql.trim().isEmpty()) {
            return invocation.proceed();
        }

        String modifiedSql = injectClinicId(originalSql, clinicId);
        if (!modifiedSql.equals(originalSql)) {
            metaObject.setValue("delegate.boundSql.sql", modifiedSql);
        }

        return invocation.proceed();
    }

    /**
     * 为 SQL 注入 clinic_id 条件
     */
    private String injectClinicId(String sql, String clinicId) {
        String lowerSql = sql.trim().toLowerCase();

        // 如果 SQL 已经包含当前诊所的 clinic_id 条件，不重复处理
        String escapedId = escapeSqlString(clinicId);
        if (lowerSql.contains("clinic_id = '" + escapedId + "'") ||
            lowerSql.contains("clinic_id='" + escapedId + "'")) {
            return sql;
        }

        // 检查是否操作白名单表
        String tableName = extractTableName(lowerSql);
        if (tableName != null && GLOBAL_TABLES.contains(tableName.toLowerCase())) {
            return sql;
        }

        if (lowerSql.startsWith("select")) {
            return injectToSelect(sql, clinicId);
        } else if (lowerSql.startsWith("update")) {
            return injectToUpdate(sql, clinicId);
        } else if (lowerSql.startsWith("delete")) {
            return injectToDelete(sql, clinicId);
        } else if (lowerSql.startsWith("insert")) {
            return injectToInsert(sql, clinicId);
        }

        return sql;
    }

    /**
     * 从 SQL 中提取表名（简化版，适用于本项目常见 SQL 格式）
     */
    private String extractTableName(String lowerSql) {
        // SELECT ... FROM table_name
        int fromIdx = findKeywordPosition(lowerSql, "from");
        if (fromIdx != -1) {
            String afterFrom = lowerSql.substring(fromIdx + 4).trim();
            // 按任意空白字符分割取第一个词
            String firstToken = afterFrom.split("\\s+")[0];
            return firstToken.isEmpty() ? null : firstToken;
        }

        // UPDATE table_name SET ...
        if (lowerSql.startsWith("update ")) {
            String afterUpdate = lowerSql.substring(7).trim();
            String firstToken = afterUpdate.split("\\s+")[0];
            return firstToken.isEmpty() ? null : firstToken;
        }

        // DELETE FROM table_name
        if (lowerSql.startsWith("delete from ")) {
            String afterDelete = lowerSql.substring(12).trim();
            String firstToken = afterDelete.split("\\s+")[0];
            return firstToken.isEmpty() ? null : firstToken;
        }

        // INSERT INTO table_name
        if (lowerSql.startsWith("insert into ")) {
            String afterInsert = lowerSql.substring(12).trim();
            String firstToken = afterInsert.split("\\s+")[0];
            // 去掉尾部可能的括号（如 table_name(col1,...）
            int parenIdx = firstToken.indexOf('(');
            if (parenIdx != -1) {
                firstToken = firstToken.substring(0, parenIdx);
            }
            return firstToken.isEmpty() ? null : firstToken;
        }

        return null;
    }

    /**
     * 从 SELECT SQL 中提取主表别名（如 FROM table_name alias）
     * 返回别名，如果没有别名则返回表名
     */
    private String extractMainTableAlias(String lowerSql) {
        int fromIdx = findKeywordPosition(lowerSql, "from");
        if (fromIdx == -1) {
            return null;
        }
        String afterFrom = lowerSql.substring(fromIdx + 4).trim();

        // 如果 afterFrom 以 '(' 开头，说明主表是派生表，不提取别名
        // 派生表内部的真实表已各自被拦截器注入 clinic_id，外层无需重复注入
        if (afterFrom.startsWith("(")) {
            return null;
        }

        // 找到下一个关键字（join, where, group, order, limit, union, comma）
        String[] stopWords = {" join", " where", " group", " order", " limit", " union", ","};
        int stopPos = afterFrom.length();
        for (String sw : stopWords) {
            int pos = findKeywordPosition(afterFrom, sw.trim());
            if (pos != -1 && pos < stopPos) {
                stopPos = pos;
            }
        }
        String tablePart = afterFrom.substring(0, stopPos).trim();
        // tablePart 可能是 "table_name alias" 或 "table_name as alias"
        String[] parts = tablePart.split("\\s+");
        if (parts.length >= 3 && "as".equals(parts[1])) {
            return parts[2];
        }
        if (parts.length >= 2) {
            return parts[1];
        }
        return parts.length >= 1 ? parts[0] : null;
    }

    /**
     * 为 SELECT 语句注入 clinic_id
     */
    private String injectToSelect(String sql, String clinicId) {
        String lower = sql.toLowerCase();

        // 如果 FROM 后面紧跟 '('，说明主表是派生表，不注入外层 clinic_id
        // 派生表内部的真实表已各自被拦截器注入 clinic_id，外层无需重复注入
        int fromIdx = findKeywordPosition(lower, "from");
        if (fromIdx != -1) {
            String afterFrom = lower.substring(fromIdx + 4).trim();
            if (afterFrom.startsWith("(")) {
                return sql;
            }
        }

        boolean hasJoin = lower.contains(" join ");
        String alias = hasJoin ? extractMainTableAlias(lower) : null;
        // 验证别名有效性（必须是合法 SQL 标识符）
        if (alias != null && !alias.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            alias = null;
        }
        String columnRef = (alias != null && !alias.isEmpty()) ? alias + ".clinic_id" : "clinic_id";
        String condition = " " + columnRef + " = '" + escapeSqlString(clinicId) + "' ";

        // 找到 ORDER BY / GROUP BY / LIMIT / HAVING / UNION 的位置
        int insertPos = sql.length();
        String[] keywords = {"order by", "group by", "having", "limit", "union"};
        for (String kw : keywords) {
            int pos = findKeywordPosition(lower, kw);
            if (pos != -1 && pos < insertPos) {
                insertPos = pos;
            }
        }

        int wherePos = findKeywordPosition(lower, "where");

        StringBuilder sb = new StringBuilder(sql);
        if (wherePos == -1) {
            // 没有 WHERE，在 insertPos 前插入 WHERE clinic_id = 'xxx'
            sb.insert(insertPos, " WHERE" + condition);
        } else {
            // 有 WHERE，在 WHERE 后紧跟 clinic_id = 'xxx' AND
            int afterWhere = wherePos + 5;
            sb.insert(afterWhere, condition + "AND ");
        }

        return sb.toString();
    }

    /**
     * 为 UPDATE 语句注入 clinic_id
     */
    private String injectToUpdate(String sql, String clinicId) {
        String lower = sql.toLowerCase();
        String condition = " clinic_id = '" + escapeSqlString(clinicId) + "' ";

        // 找到 ORDER BY / LIMIT
        int insertPos = sql.length();
        String[] keywords = {"order by", "limit"};
        for (String kw : keywords) {
            int pos = findKeywordPosition(lower, kw);
            if (pos != -1 && pos < insertPos) {
                insertPos = pos;
            }
        }

        int wherePos = findKeywordPosition(lower, "where");

        StringBuilder sb = new StringBuilder(sql);
        if (wherePos == -1) {
            sb.insert(insertPos, " WHERE" + condition);
        } else {
            int afterWhere = wherePos + 5;
            sb.insert(afterWhere, condition + "AND ");
        }

        return sb.toString();
    }

    /**
     * 为 DELETE 语句注入 clinic_id
     */
    private String injectToDelete(String sql, String clinicId) {
        String lower = sql.toLowerCase();
        String condition = " clinic_id = '" + escapeSqlString(clinicId) + "' ";

        int insertPos = sql.length();
        String[] keywords = {"order by", "limit"};
        for (String kw : keywords) {
            int pos = findKeywordPosition(lower, kw);
            if (pos != -1 && pos < insertPos) {
                insertPos = pos;
            }
        }

        int wherePos = findKeywordPosition(lower, "where");

        StringBuilder sb = new StringBuilder(sql);
        if (wherePos == -1) {
            sb.insert(insertPos, " WHERE" + condition);
        } else {
            int afterWhere = wherePos + 5;
            sb.insert(afterWhere, condition + "AND ");
        }

        return sb.toString();
    }

    /**
     * 为 INSERT 语句注入 clinic_id
     * 支持格式：INSERT INTO table (cols) VALUES (vals) 和 INSERT INTO table SET col=val
     */
    private String injectToInsert(String sql, String clinicId) {
        String lower = sql.toLowerCase();
        String escaped = escapeSqlString(clinicId);

        // 格式1: INSERT INTO table (cols) VALUES (vals)
        if (lower.contains("values")) {
            int valuesIdx = findKeywordPosition(lower, "values");
            if (valuesIdx == -1) {
                return sql;
            }

            // 检查 values 之前是否有列列表（即是否有 ')'）
            int colsEnd = sql.lastIndexOf(')', valuesIdx);
            boolean hasColumnList = colsEnd != -1;

            if (hasColumnList) {
                // 在列列表的 ')' 前插入 clinic_id
                sql = sql.substring(0, colsEnd) + ", clinic_id" + sql.substring(colsEnd);
                lower = sql.toLowerCase();
                valuesIdx = findKeywordPosition(lower, "values");
            }

            // 在 values 之后的值列表中注入 clinic_id
            // 找到 values 后第一个 '('
            int valuesParen = sql.indexOf('(', valuesIdx);
            if (valuesParen != -1) {
                // 从 valuesParen 开始，找到所有顶层 ')' 并在每个前注入 clinic_id
                sql = injectClinicIdToValueGroups(sql, valuesParen, escaped);
            }
            return sql;
        }

        // 格式2: INSERT INTO table SET col1=val1, col2=val2
        if (lower.contains("set")) {
            int setIdx = findKeywordPosition(lower, "set");
            if (setIdx != -1) {
                // 找到 SET 后面内容的末尾（ON DUPLICATE KEY 或 RETURNING 或结尾）
                int insertPos = sql.length();
                String[] endKeywords = {"on duplicate", "returning"};
                for (String kw : endKeywords) {
                    int pos = findKeywordPosition(lower, kw);
                    if (pos != -1 && pos < insertPos) {
                        insertPos = pos;
                    }
                }
                sql = sql.substring(0, insertPos) + ", clinic_id = '" + escaped + "'" + sql.substring(insertPos);
                return sql;
            }
        }

        return sql;
    }

    /**
     * 在 INSERT 的 VALUES 值组中注入 clinic_id
     * 支持多值组：VALUES (...), (...), ...
     */
    private String injectClinicIdToValueGroups(String sql, int startParen, String clinicId) {
        StringBuilder sb = new StringBuilder(sql);
        int offset = 0;
        int i = startParen + 1;
        int depth = 1;

        while (i < sb.length() && depth > 0) {
            char c = sb.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    // 找到值组的结束 ')'，在前面插入 clinic_id
                    int insertPos = i + offset;
                    String suffix = sb.substring(insertPos);
                    // 检查 ')' 前面是否已经有内容（不是空的值组）
                    String before = sb.substring(startParen + 1 + offset, insertPos).trim();
                    if (!before.isEmpty()) {
                        sb.insert(insertPos, ", '" + clinicId + "'");
                        offset += (", '" + clinicId + "'").length();
                        i += (", '" + clinicId + "'").length();
                    }

                    // 继续检查后面是否还有更多的值组（逗号后跟 '('）
                    int j = i + 1;
                    while (j < sb.length() && Character.isWhitespace(sb.charAt(j))) {
                        j++;
                    }
                    if (j < sb.length() && sb.charAt(j) == ',') {
                        // 跳过后面的空白和 '('
                        j++;
                        while (j < sb.length() && Character.isWhitespace(sb.charAt(j))) {
                            j++;
                        }
                        if (j < sb.length() && sb.charAt(j) == '(') {
                            startParen = j;
                            i = j + 1;
                            depth = 1;
                            continue;
                        }
                    }
                    break;
                }
            }
            i++;
        }

        return sb.toString();
    }

    /**
     * 简单转义 SQL 字符串中的单引号（防止注入）
     */
    private String escapeSqlString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * 找到关键字在 SQL 中的位置（不在字符串内，且不在括号/子查询内的位置）
     */
    private int findKeywordPosition(String lowerSql, String keyword) {
        int len = lowerSql.length();
        int kwLen = keyword.length();
        boolean inString = false;
        int parenDepth = 0;

        for (int i = 0; i <= len - kwLen; i++) {
            char c = lowerSql.charAt(i);

            if (c == '\'') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }

            if (c == '(') {
                parenDepth++;
                continue;
            }
            if (c == ')') {
                if (parenDepth > 0) {
                    parenDepth--;
                }
                continue;
            }

            if (parenDepth > 0) {
                continue;
            }

            // 检查是否匹配关键字
            boolean match = true;
            for (int j = 0; j < kwLen; j++) {
                if (lowerSql.charAt(i + j) != keyword.charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (!match) {
                continue;
            }

            // 检查前后字符
            boolean beforeOk = i == 0 || !Character.isLetterOrDigit(lowerSql.charAt(i - 1));
            int afterPos = i + kwLen;
            boolean afterOk = afterPos >= len || !Character.isLetterOrDigit(lowerSql.charAt(afterPos));

            if (beforeOk && afterOk) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
        // 无需配置
    }
}
