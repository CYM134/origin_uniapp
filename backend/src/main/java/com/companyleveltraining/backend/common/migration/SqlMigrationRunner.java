package com.companyleveltraining.backend.common.migration;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/**
 * Lightweight SQL migration runner for project-owned idempotent scripts.
 * Docker still initializes fresh databases; this runner keeps existing databases current.
 */
@Component
public class SqlMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SqlMigrationRunner.class);
    private static final String MIGRATION_LOCATION = "classpath*:/db/migration/*.sql";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;

    public SqlMigrationRunner(DataSource dataSource, JdbcTemplate jdbcTemplate,
                              @Value("${app.sql-migration.enabled:true}") boolean enabled) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            log.info("SQL migration runner is disabled");
            return;
        }

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS app_schema_migrations (
              script_name VARCHAR(255) NOT NULL PRIMARY KEY,
              executed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """);

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MIGRATION_LOCATION);
        Arrays.sort(resources, Comparator.comparing(this::resourceName));

        for (Resource resource : resources) {
            String scriptName = resourceName(resource);
            if (hasExecuted(scriptName)) {
                continue;
            }
            log.info("Executing SQL migration {}", scriptName);
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new EncodedResource(resource, StandardCharsets.UTF_8));
            }
            jdbcTemplate.update("INSERT INTO app_schema_migrations (script_name) VALUES (?)", scriptName);
        }
    }

    private boolean hasExecuted(String scriptName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM app_schema_migrations WHERE script_name = ?",
            Integer.class,
            scriptName
        );
        return count != null && count > 0;
    }

    private String resourceName(Resource resource) {
        return resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
    }
}
