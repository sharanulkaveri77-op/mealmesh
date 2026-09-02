package com.mealmesh.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * Cloud providers like Render, Heroku, Railway pass database URLs formatted as:
 * postgresql://username:password@host:port/dbname
 *
 * Spring Boot requires:
 * 1. A jdbc: URL format (jdbc:postgresql://host:port/dbname)
 * 2. Separate username and password credentials.
 *
 * This BeanPostProcessor automatically extracts username/password from the URI
 * and constructs the clean JDBC URL so HikariCP connects smoothly.
 */
@Configuration
@Slf4j
public class DatabaseUrlFixerConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties props) {
            String rawUrl = props.getUrl();
            if (rawUrl != null && (rawUrl.startsWith("postgresql://") || rawUrl.startsWith("postgres://"))) {
                try {
                    URI uri = new URI(rawUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        props.setUsername(parts[0]);
                        props.setPassword(parts[1]);
                    }
                    int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                    String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";
                    String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query;
                    props.setUrl(jdbcUrl);
                    log.info("Configured JDBC URL for database: jdbc:postgresql://{}:{}{}", uri.getHost(), port, uri.getPath());
                } catch (Exception e) {
                    log.warn("Failed to parse cloud database URL as URI, prepending jdbc: prefix: {}", e.getMessage());
                    props.setUrl("jdbc:" + rawUrl);
                }
            }
        }
        return bean;
    }
}
