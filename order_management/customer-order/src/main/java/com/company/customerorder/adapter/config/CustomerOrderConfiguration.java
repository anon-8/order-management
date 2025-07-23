package com.company.customerorder.adapter.config;

import com.company.customerorder.domain.port.ManufacturingOrderQueryPort;
import com.company.customerorder.domain.service.CustomerOrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerOrderConfiguration {
    
    @Bean
    public CustomerOrderDomainService customerOrderDomainService(
        ManufacturingOrderQueryPort manufacturingOrderQueryPort
    ) {
        return new CustomerOrderDomainService(manufacturingOrderQueryPort);
    }
}