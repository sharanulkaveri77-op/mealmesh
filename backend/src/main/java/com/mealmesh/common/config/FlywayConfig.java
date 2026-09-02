package com.mealmesh.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures Flyway automatically repairs any previously failed migration state
 * (e.g. from initial cloud deployment attempts) before executing migrations.
 */
@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                log.info("Executing Flyway repair to clear any failed migration attempts...");
                flyway.repair();
            } catch (Exception e) {
                log.warn("Flyway repair encountered non-critical error: {}", e.getMessage());
            }
            log.info("Executing Flyway migrate...");
            flyway.migrate();
            log.info("Flyway migration completed successfully.");
        };
    }
}
