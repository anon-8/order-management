package com.company.customerorder.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerOrderJpaRepository extends JpaRepository<CustomerOrderJpaEntity, UUID> {
    
    List<CustomerOrderJpaEntity> findByCustomerId(UUID customerId);
    
    List<CustomerOrderJpaEntity> findByStatus(CustomerOrderJpaEntity.CustomerOrderStatusEntity status);
    
    @Query("SELECT o FROM CustomerOrderJpaEntity o WHERE o.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<CustomerOrderJpaEntity> findActiveOrders();
    
    List<CustomerOrderJpaEntity> findByManufacturingOrderId(UUID manufacturingOrderId);
}