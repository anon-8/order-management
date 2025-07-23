package com.company.manufacturingorder.application.command;

import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CompleteManufacturingOrderHandler {
    private final ManufacturingOrderRepository repository;
    
    @Transactional
    public void handle(CompleteManufacturingOrderCommand command) {
        var order = repository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Manufacturing order not found: " + command.orderId()
            ));
        
        order.complete();
        
        repository.save(order);
    }
}