package com.company.ordermanagement.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.event.TransactionPhase;

@TestConfiguration
public class TestEventConfiguration {

    @Bean(name = "applicationEventMulticaster") 
    @Primary
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setErrorHandler(throwable -> {
            System.err.println("Event processing error: " + throwable.getMessage());
            throwable.printStackTrace();
        });
        return eventMulticaster;
    }
}