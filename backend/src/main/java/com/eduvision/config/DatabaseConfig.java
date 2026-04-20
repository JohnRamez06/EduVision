package com.eduvision.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class DatabaseConfig {
    // Uses application.properties datasource settings.
    // Auditing can be used later with @CreatedDate/@LastModifiedDate if you want.
}