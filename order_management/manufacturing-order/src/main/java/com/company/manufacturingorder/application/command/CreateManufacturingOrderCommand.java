package com.company.manufacturingorder.application.command;

import com.company.sharedkernel.OrderId;

import java.time.Instant;

public record CreateManufacturingOrderCommand(
    OrderId orderId,
    String productCode,
    String description,
    Integer quantity,
    String specifications,
    Instant expectedStartDate,
    Instant expectedCompletionDate
) {
    public CreateManufacturingOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (expectedStartDate == null) {
            throw new IllegalArgumentException("Expected start date cannot be null");
        }
        if (expectedCompletionDate == null) {
            throw new IllegalArgumentException("Expected completion date cannot be null");
        }
    }
}