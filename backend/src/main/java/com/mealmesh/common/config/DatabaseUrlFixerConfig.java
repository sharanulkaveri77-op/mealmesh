package com.mealmesh.common.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cloud providers like Render, Heroku, Railway pass database URLs formatted as
 * postgresql://... while Spring Boot's JDBC driver requires jdbc:postgresql://...
 * This BeanPostProcessor automatically prepends "jdbc:" if missing.
 */
@Configuration
public class DatabaseUrlFixerConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties props) {
            String url = props.getUrl();
            if (url != null && url.startsWith("postgresql://")) {
                props.setUrl("jdbc:" + url);
            }
        }
        return bean;
    }
}
