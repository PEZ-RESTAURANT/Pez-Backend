package com.pezbackend.shared.infrastructure.persistence.jpa.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class FlywayDataSourcePostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            DataSource dataSource = (DataSource) bean;
            System.out.println("=== EJECUTANDO MIGRACIONES FLYWAY DESDE POSTPROCESSOR ===");
            try {
                Flyway.configure()
                        .dataSource(dataSource)
                        .baselineOnMigrate(true)
                        .baselineVersion("1")
                        .load()
                        .migrate();
                System.out.println("=== MIGRACIONES FLYWAY COMPLETADAS CON ÉXITO ===");
            } catch (Exception e) {
                System.err.println("=== ERROR EN MIGRACIONES FLYWAY ===");
                e.printStackTrace();
                throw new RuntimeException("Flyway migration failed", e);
            }
        }
        return bean;
    }
}
