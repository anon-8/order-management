package com.company.customerorder.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderJpaEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;
    
    @Column(name = "customer_address", nullable = false, columnDefinition = "TEXT")
    private String customerAddress;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", nullable = false)
    private String currency;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerOrderStatusEntity status;
    
    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "manufacturing_order_id")
    private UUID manufacturingOrderId;
    
    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItemJpaEntity> items = new ArrayList<>();
    
    public enum CustomerOrderStatusEntity {
        PLACED, CONFIRMED, MANUFACTURING_IN_PROGRESS, 
        MANUFACTURING_COMPLETED, SHIPPED, DELIVERED, CANCELLED
    }
}