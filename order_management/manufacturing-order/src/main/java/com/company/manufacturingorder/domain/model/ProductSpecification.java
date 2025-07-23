package com.company.manufacturingorder.domain.model;

import com.company.sharedkernel.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class ProductSpecification extends ValueObject {
    @NotBlank
    private final String productCode;
    
    @NotBlank
    private final String description;
    
    @NotNull
    @Positive
    private final Integer quantity;
    
    @NotBlank
    private final String specifications;
    
    public static ProductSpecification of(String productCode, String description, Integer quantity, String specifications) {
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (specifications == null || specifications.trim().isEmpty()) {
            throw new IllegalArgumentException("Specifications cannot be null or empty");
        }
        
        return new ProductSpecification(productCode.trim(), description.trim(), quantity, specifications.trim());
    }
}