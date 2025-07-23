package com.company.customerorder.application.query;

import com.company.customerorder.domain.port.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FindCustomerOrderHandler {
    private final CustomerOrderRepository repository;
    
    @Transactional(readOnly = true)
    public Optional<CustomerOrderDto> handle(FindCustomerOrderQuery query) {
        return repository.findById(query.orderId())
            .map(order -> new CustomerOrderDto(
                order.getId(),
                order.getCustomerInfo().getCustomerId(),
                order.getCustomerInfo().getName(),
                order.getCustomerInfo().getEmail(),
                order.getCustomerInfo().getAddress(),
                order.getItems().stream()
                    .map(item -> new CustomerOrderDto.OrderItemDto(
                        item.getProductCode(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                    ))
                    .toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPlacedAt(),
                order.getUpdatedAt(),
                order.getManufacturingOrderId(),
                order.isActive()
            ));
    }
}