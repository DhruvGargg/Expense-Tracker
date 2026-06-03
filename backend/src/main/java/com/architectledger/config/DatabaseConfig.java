package com.architectledger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null) {
            dbUrl = System.getenv("DB_URL");
        }

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

        if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
            try {
                // Standard URI parser to extract connection parameters from postgres:// URL
                URI dbUri = new URI(dbUrl);
                String[] userInfo = dbUri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo.length > 1 ? userInfo[1] : "";
                
                // Build JDBC URL
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String path = dbUri.getPath();
                
                String jdbcUrl = "jdbc:postgresql://" + host + (port != -1 ? ":" + port : "") + path;
                
                // Append SSL parameter if not present (Neon and Render require SSL)
                if (!jdbcUrl.contains("?")) {
                    jdbcUrl += "?sslmode=require";
                } else if (!jdbcUrl.contains("sslmode")) {
                    jdbcUrl += "&sslmode=require";
                }

                dataSourceBuilder.url(jdbcUrl);
                dataSourceBuilder.username(username);
                dataSourceBuilder.password(password);
                dataSourceBuilder.driverClassName("org.postgresql.Driver");
                
                return dataSourceBuilder.build();
            } catch (URISyntaxException | NullPointerException e) {
                // Fail-safe: fallback to H2
                return defaultH2DataSource();
            }
        }

        // Fallback to default H2 database
        return defaultH2DataSource();
    }

    private DataSource defaultH2DataSource() {
        String dbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:h2:mem:architectledger;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        String dbUser = System.getenv().getOrDefault("DB_USER", "sa");
        String dbPass = System.getenv().getOrDefault("DB_PASS", "");

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .driverClassName("org.h2.Driver")
                .build();
    }
}
