package com.company.manufacturingorder.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.company.manufacturingorder.adapter.out.persistence")
@EnableJpaRepositories("com.company.manufacturingorder.adapter.out.persistence")
public class JpaTestConfiguration {
}