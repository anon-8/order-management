package com.company.manufacturingorder.application.query;

import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FindManufacturingOrderHandler {
    private final ManufacturingOrderRepository repository;
    
    @Transactional(readOnly = true)
    public Optional<ManufacturingOrderDto> handle(FindManufacturingOrderQuery query) {
        return repository.findById(query.orderId())
            .map(order -> new ManufacturingOrderDto(
                order.getId(),
                order.getProductSpecification().getProductCode(),
                order.getProductSpecification().getDescription(),
                order.getProductSpecification().getQuantity(),
                order.getProductSpecification().getSpecifications(),
                order.getStatus(),
                order.getTimeline().getExpectedStartDate(),
                order.getTimeline().getExpectedCompletionDate(),
                order.getTimeline().getActualStartDate(),
                order.getTimeline().getActualCompletionDate(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.isOverdue()
            ));
    }
}