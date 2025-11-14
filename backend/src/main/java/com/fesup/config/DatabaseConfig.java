package com.fesup.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration de la base de données pour Render.com
 * Render fournit DATABASE_URL au format: postgresql://user:pass@host:5432/db
 * JDBC attend: jdbc:postgresql://host:5432/db
 */
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    /**
     * Bean DataSource personnalisé qui convertit l'URL Render en URL JDBC
     * Activé uniquement si DATABASE_URL est définie (environnement Render)
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource renderDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Convertir postgresql:// en jdbc:postgresql://
        String jdbcUrl = databaseUrl.startsWith("jdbc:") 
            ? databaseUrl 
            : "jdbc:" + databaseUrl;
        
        config.setJdbcUrl(jdbcUrl);
        
        // Configuration optimisée pour Render (512MB RAM)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Configuration PostgreSQL
        config.setDriverClassName("org.postgresql.Driver");
        
        System.out.println("✅ DataSource configuré pour Render: " + jdbcUrl.replaceAll(":[^:@]+@", ":****@"));
        
        return new HikariDataSource(config);
    }
}
