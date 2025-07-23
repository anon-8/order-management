package com.company.manufacturingorder.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ManufacturingOrderJpaRepository extends JpaRepository<ManufacturingOrderJpaEntity, UUID> {
    
    List<ManufacturingOrderJpaEntity> findByStatus(ManufacturingOrderJpaEntity.OrderStatusEntity status);
    
    @Query("SELECT o FROM ManufacturingOrderJpaEntity o WHERE o.expectedCompletionDate < :now " +
           "AND o.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<ManufacturingOrderJpaEntity> findOverdueOrders(Instant now);
    
    long countByStatus(ManufacturingOrderJpaEntity.OrderStatusEntity status);
}