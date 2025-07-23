package com.company.customerorder.adapter.out.persistence;

import com.company.sharedkernel.DomainEventPublisher;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import static org.mockito.Mockito.mock;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.company.customerorder.adapter.out.persistence")
public class RepositoryTestConfig {
    
    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return mock(DomainEventPublisher.class);
    }
}