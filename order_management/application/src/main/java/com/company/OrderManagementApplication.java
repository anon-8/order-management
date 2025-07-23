package com.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.modulith.Modulith;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Modulith(
    systemName = "Order Management System",
    sharedModules = "sharedkernel"
)
@ComponentScan(basePackages = {
    "com.company.manufacturingorder",
    "com.company.customerorder", 
    "com.company.ordermanagement",
    "com.company.sharedkernel"
})
@EntityScan(basePackages = {
    "com.company.manufacturingorder.adapter.out.persistence",
    "com.company.customerorder.adapter.out.persistence"
})
@EnableJpaRepositories(basePackages = {
    "com.company.manufacturingorder.adapter.out.persistence",
    "com.company.customerorder.adapter.out.persistence"
})
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class OrderManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementApplication.class, args);
    }
}