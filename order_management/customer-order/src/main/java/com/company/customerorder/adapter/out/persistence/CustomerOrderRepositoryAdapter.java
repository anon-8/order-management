package com.company.customerorder.adapter.out.persistence;

import com.company.sharedkernel.DomainEventPublisher;
import com.company.sharedkernel.OrderId;
import com.company.customerorder.domain.model.CustomerOrder;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerOrderStatus;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerOrderRepositoryAdapter implements CustomerOrderRepository {
    private final CustomerOrderJpaRepository jpaRepository;
    private final CustomerOrderMapper mapper;
    private final DomainEventPublisher eventPublisher;

    public CustomerOrderRepositoryAdapter(
        CustomerOrderJpaRepository jpaRepository,
        @Qualifier("customerOrderMapperImpl") CustomerOrderMapper mapper,
        DomainEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Optional<CustomerOrder> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<CustomerOrder> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.getValue())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<CustomerOrder> findByStatus(CustomerOrderStatus status) {
        var entityStatus = CustomerOrderJpaEntity.CustomerOrderStatusEntity.valueOf(status.name());
        return jpaRepository.findByStatus(entityStatus)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<CustomerOrder> findActiveOrders() {
        return jpaRepository.findActiveOrders()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<CustomerOrder> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<CustomerOrder> findByManufacturingOrderId(OrderId manufacturingOrderId) {
        return jpaRepository.findByManufacturingOrderId(manufacturingOrderId.getValue())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public CustomerOrder save(CustomerOrder order) {
        var existingEntity = jpaRepository.findById(order.getId().getValue()).orElse(null);
        var entity = mapper.toEntity(order);
        
        if (existingEntity != null) {
            var preservedItems = mapper.mapItemsPreservingIds(order.getItems(), existingEntity.getItems());
            entity.setItems(preservedItems);
        }
        
        entity.getItems().forEach(item -> item.setCustomerOrder(entity));
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