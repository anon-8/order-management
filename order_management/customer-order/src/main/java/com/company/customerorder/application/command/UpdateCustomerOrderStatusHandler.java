package com.company.customerorder.application.command;

import com.company.customerorder.domain.port.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateCustomerOrderStatusHandler {
    private final CustomerOrderRepository repository;
    
    @Transactional
    public void handle(UpdateCustomerOrderStatusCommand command) {
        var order = repository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Customer order not found: " + command.orderId()
            ));
        
        order.updateStatus(command.newStatus());
        
        repository.save(order);
    }
}