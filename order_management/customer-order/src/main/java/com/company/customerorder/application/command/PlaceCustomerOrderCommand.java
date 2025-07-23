package com.company.customerorder.application.command;

import com.company.sharedkernel.OrderId;
import com.company.customerorder.domain.model.CustomerId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Currency;

public record PlaceCustomerOrderCommand(
    OrderId orderId,
    CustomerId customerId,
    String customerName,
    String customerEmail,
    String customerAddress,
    List<OrderItemCommand> items
) {
    public PlaceCustomerOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }
    
    public record OrderItemCommand(
        String productCode,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        Currency currency
    ) {
        public OrderItemCommand {
            if (productCode == null || productCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Product code cannot be null or empty");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
        }
    }
}