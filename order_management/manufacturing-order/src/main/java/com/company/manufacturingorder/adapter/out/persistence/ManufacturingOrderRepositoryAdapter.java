package com.company.manufacturingorder.adapter.out.persistence;

import com.company.sharedkernel.DomainEventPublisher;
import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ManufacturingOrderRepositoryAdapter implements ManufacturingOrderRepository {
    private final ManufacturingOrderJpaRepository jpaRepository;
    private final ManufacturingOrderMapper mapper;
    private final DomainEventPublisher eventPublisher;

    public ManufacturingOrderRepositoryAdapter(
        ManufacturingOrderJpaRepository jpaRepository,
        @Qualifier("manufacturingOrderMapperImpl") ManufacturingOrderMapper mapper,
        DomainEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Optional<ManufacturingOrder> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<ManufacturingOrder> findByStatus(OrderStatus status) {
        var entityStatus = ManufacturingOrderJpaEntity.OrderStatusEntity.valueOf(status.name());
        return jpaRepository.findByStatus(entityStatus)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<ManufacturingOrder> findOverdueOrders() {
        return jpaRepository.findOverdueOrders(Instant.now())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<ManufacturingOrder> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public ManufacturingOrder save(ManufacturingOrder order) {
        var entity = mapper.toEntity(order);
        var savedEntity = jpaRepository.save(entity);
        var savedOrder = mapper.toDomain(savedEntity);
        
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
        
        return savedOrder;
    }
    
    @Override
    public void delete(OrderId orderId) {
        jpaRepository.deleteById(orderId.getValue());
    }
    
    @Override
    public boolean existsById(OrderId orderId) {
        return jpaRepository.existsById(orderId.getValue());
    }
}