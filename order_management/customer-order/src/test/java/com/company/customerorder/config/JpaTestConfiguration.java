package com.company.customerorder.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.company.customerorder.adapter.out.persistence")
@EnableJpaRepositories("com.company.customerorder.adapter.out.persistence")
public class JpaTestConfiguration {
}