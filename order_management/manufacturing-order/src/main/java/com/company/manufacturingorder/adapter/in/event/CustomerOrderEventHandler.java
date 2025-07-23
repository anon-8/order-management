package com.company.manufacturingorder.adapter.in.event;

import com.company.sharedkernel.events.CustomerOrderStatusUpdated;
import com.company.sharedkernel.events.CustomerOrderCancelled;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderCommand;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderHandler;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.manufacturingorder.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@ConditionalOnProperty(name = "order.management.auto-create-manufacturing-orders", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderEventHandler {
    private final CreateManufacturingOrderHandler createManufacturingOrderHandler;
    private final ManufacturingOrderRepository manufacturingOrderRepository;
    
    @EventListener
    @Transactional
    public void handleCustomerOrderStatusUpdated(CustomerOrderStatusUpdated event) {
        if (!"CONFIRMED".equals(event.newStatus())) {
            return;
        }
        
        if (manufacturingOrderRepository.existsById(event.orderId())) {
            return;
        }
        
        var expectedStartDate = Instant.now().plus(1, ChronoUnit.DAYS);
        var expectedCompletionDate = Instant.now().plus(7, ChronoUnit.DAYS);
        
        var createCommand = new CreateManufacturingOrderCommand(
            event.orderId(),
            "AUTO-" + System.currentTimeMillis(),
            "Auto-created manufacturing order for confirmed customer order: " + event.orderId(),
            1,
            "Auto-generated specifications based on confirmed customer order",
            expectedStartDate,
            expectedCompletionDate
        );
        
        createManufacturingOrderHandler.handle(createCommand);
    }
    
    @EventListener
    @Transactional
    public void handleCustomerOrderCancelled(CustomerOrderCancelled event) {
        manufacturingOrderRepository.findById(event.orderId())
            .ifPresent(manufacturingOrder -> {
                if (manufacturingOrder.getStatus() == OrderStatus.PENDING || 
                    manufacturingOrder.getStatus() == OrderStatus.IN_PROGRESS) {
                    manufacturingOrder.cancel();
                    manufacturingOrderRepository.save(manufacturingOrder);
                }
            });
    }
    
}