package com.company.customerorder.domain.model;

import com.company.sharedkernel.Money;
import com.company.sharedkernel.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class OrderItem extends ValueObject {
    private final String productCode;
    private final String description;
    private final Integer quantity;
    private final Money unitPrice;
    
    public static OrderItem of(String productCode, String description, Integer quantity, Money unitPrice) {
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || !unitPrice.isPositive()) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        
        return new OrderItem(productCode.trim(), description.trim(), quantity, unitPrice);
    }
    
    public Money getTotalPrice() {
        return unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
    }
}