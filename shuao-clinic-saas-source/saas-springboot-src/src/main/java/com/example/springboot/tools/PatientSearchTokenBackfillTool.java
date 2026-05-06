package com.example.springboot.tools;

import com.example.springboot.util.PatientSearchUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PatientSearchTokenBackfillTool {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/clinic_system_new?autoReconnect=true&useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    public static void main(String[] args) throws Exception {
        String jdbcUrl = args.length > 0 && args[0] != null && !args[0].isBlank() ? args[0].trim() : DEFAULT_URL;
        String username = args.length > 1 && args[1] != null && !args[1].isBlank() ? args[1].trim() : DEFAULT_USER;
        String password = args.length > 2 ? args[2] : DEFAULT_PASSWORD;

        Class.forName("com.mysql.cj.jdbc.Driver");

        int updatedCount = 0;
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement select = connection.prepareStatement("""
                     SELECT id, name
                     FROM patients
                     WHERE COALESCE(name_pinyin, '') = ''
                        OR COALESCE(name_initials, '') = ''
                     """);
             ResultSet resultSet = select.executeQuery();
             PreparedStatement update = connection.prepareStatement("""
                     UPDATE patients
                     SET name_pinyin = ?, name_initials = ?
                     WHERE id = ?
                     """)) {

            connection.setAutoCommit(false);
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                update.setString(1, normalizeToken(PatientSearchUtils.toPinyin(name)));
                update.setString(2, normalizeToken(PatientSearchUtils.toInitials(name)));
                update.setLong(3, id);
                update.addBatch();
                updatedCount += 1;
            }
            update.executeBatch();
            connection.commit();
        }

        System.out.println("Updated patient search tokens: " + updatedCount);
    }

    private static String normalizeToken(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }
}
