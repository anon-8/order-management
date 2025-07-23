package com.company.manufacturingorder.application.query;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.OrderStatus;

import java.time.Instant;

public record ManufacturingOrderDto(
    OrderId orderId,
    String productCode,
    String description,
    Integer quantity,
    String specifications,
    OrderStatus status,
    Instant expectedStartDate,
    Instant expectedCompletionDate,
    Instant actualStartDate,
    Instant actualCompletionDate,
    Instant createdAt,
    Instant updatedAt,
    boolean isOverdue
) {}