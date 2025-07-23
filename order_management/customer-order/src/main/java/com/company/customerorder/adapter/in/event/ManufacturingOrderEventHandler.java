package com.company.customerorder.adapter.in.event;

import com.company.sharedkernel.events.ManufacturingOrderCompleted;
import com.company.sharedkernel.events.ManufacturingOrderCreated;
import com.company.sharedkernel.events.ManufacturingOrderStatusChanged;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManufacturingOrderEventHandler {
    private final CustomerOrderRepository customerOrderRepository;
    
    @EventListener
    @Transactional
    public void handleManufacturingOrderCompleted(ManufacturingOrderCompleted event) {
        customerOrderRepository.findByManufacturingOrderId(event.orderId())
            .forEach(customerOrder -> {
                customerOrder.notifyManufacturingCompleted();
                customerOrderRepository.save(customerOrder);
            });
    }
    
    @EventListener
    @Transactional
    public void handleManufacturingOrderCreated(ManufacturingOrderCreated event) {
        customerOrderRepository.findById(event.orderId())
            .ifPresent(customerOrder -> {
                if (customerOrder.getManufacturingOrderId() == null) {
                    customerOrder.linkManufacturingOrder(event.orderId());
                    customerOrderRepository.save(customerOrder);
                }
            });
    }
    
    @EventListener
    @Transactional
    public void handleManufacturingOrderStatusChanged(ManufacturingOrderStatusChanged event) {
        customerOrderRepository.findByManufacturingOrderId(event.orderId())
            .forEach(customerOrder -> {
                switch (event.newStatus()) {
                    case "IN_PROGRESS" -> {
                        customerOrder.notifyManufacturingStarted();
                        customerOrderRepository.save(customerOrder);
                    }
                    case "CANCELLED" -> {
                        if (!customerOrder.getStatus().name().equals("CANCELLED") && 
                            !customerOrder.getStatus().name().equals("DELIVERED")) {
                            customerOrder.cancel("Manufacturing order was cancelled");
                            customerOrderRepository.save(customerOrder);
                        }
                    }
                }
            });
    }
}