package com.company.manufacturingorder.adapter.out.external;

import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.application.port.ManufacturingOrderQueryPort;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ManufacturingOrderQueryAdapter implements ManufacturingOrderQueryPort {
    private final ManufacturingOrderRepository repository;
    
    @Override
    public Optional<ManufacturingOrderStatus> getOrderStatus(OrderId orderId) {
        return repository.findById(orderId)
            .map(order -> new ManufacturingOrderStatus(
                order.getId(),
                order.getStatus(),
                order.getProductSpecification().getProductCode(),
                order.getProductSpecification().getQuantity()
            ));
    }
    
    @Override
    public boolean isOrderCompleted(OrderId orderId) {
        return repository.findById(orderId)
            .map(ManufacturingOrder::isCompleted)
            .orElse(false);
    }
}