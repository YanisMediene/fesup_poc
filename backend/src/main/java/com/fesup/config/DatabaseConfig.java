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
        try {
            // Render fournit: postgresql://user:password@host:port/database
            // Parser et reconstruire l'URL JDBC correctement
            
            String urlWithoutPrefix = databaseUrl.replaceFirst("^postgres(ql)?://", "");
            
            // Parser: user:password@host:port/database
            String[] userAndRest = urlWithoutPrefix.split("@", 2);
            String[] userPassword = userAndRest[0].split(":", 2);
            String username = userPassword[0];
            String password = userPassword.length > 1 ? userPassword[1] : "";
            
            // Parser: host:port/database
            String[] hostAndDb = userAndRest[1].split("/", 2);
            String hostPort = hostAndDb[0];
            String database = hostAndDb.length > 1 ? hostAndDb[1] : "";
            
            // URL JDBC sans credentials
            String jdbcUrl = "jdbc:postgresql://" + hostPort + "/" + database;
            
            System.out.println("✅ DataSource Render - URL: " + jdbcUrl + ", User: " + username);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            
            // Configuration optimisée pour Render (512MB RAM)
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            return new HikariDataSource(config);
            
        } catch (Exception e) {
            throw new RuntimeException("❌ Erreur parsing DATABASE_URL: " + databaseUrl, e);
        }
    }
}
