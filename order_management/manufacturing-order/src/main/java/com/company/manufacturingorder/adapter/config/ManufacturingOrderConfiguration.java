package com.company.manufacturingorder.adapter.config;

import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.manufacturingorder.domain.service.ManufacturingOrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManufacturingOrderConfiguration {
    
    @Bean
    public ManufacturingOrderDomainService manufacturingOrderDomainService(
        ManufacturingOrderRepository repository
    ) {
        return new ManufacturingOrderDomainService(repository);
    }
}