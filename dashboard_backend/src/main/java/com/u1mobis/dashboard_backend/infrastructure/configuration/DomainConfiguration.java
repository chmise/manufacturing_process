package com.u1mobis.dashboard_backend.infrastructure.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.u1mobis.dashboard_backend.infrastructure.persistence"
})
@ComponentScan(basePackages = {
    "com.u1mobis.dashboard_backend.domain",
    "com.u1mobis.dashboard_backend.application", 
    "com.u1mobis.dashboard_backend.infrastructure"
})
public class DomainConfiguration {
    
    // All domain configurations are handled through component scanning
    // and individual domain configuration classes
}