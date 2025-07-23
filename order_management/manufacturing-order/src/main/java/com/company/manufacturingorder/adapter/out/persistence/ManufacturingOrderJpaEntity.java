package com.company.manufacturingorder.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manufacturing_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturingOrderJpaEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "product_code", nullable = false)
    private String productCode;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "specifications", nullable = false, columnDefinition = "TEXT")
    private String specifications;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatusEntity status;
    
    @Column(name = "expected_start_date", nullable = false)
    private Instant expectedStartDate;
    
    @Column(name = "expected_completion_date", nullable = false)
    private Instant expectedCompletionDate;
    
    @Column(name = "actual_start_date")
    private Instant actualStartDate;
    
    @Column(name = "actual_completion_date")
    private Instant actualCompletionDate;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    public enum OrderStatusEntity {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}