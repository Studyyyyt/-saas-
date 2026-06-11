package com.example.springboot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnBean(JdbcTemplate.class)
@ConditionalOnProperty(name = "legacy.schema-initializer.enabled", havingValue = "true")
public class SchemaInitializer implements ApplicationRunner {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (jdbcTemplate == null) {
            return;
        }
        ClassPathResource resource = new ClassPathResource("schema-extra.sql");
        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        String[] statements = sql.split(";");
        for (String stmt : statements) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty()) {
                try {
                    jdbcTemplate.execute(trimmed);
                } catch (Exception e) {
                    // table may already exist, ignore
                }
            }
        }
    }
}
