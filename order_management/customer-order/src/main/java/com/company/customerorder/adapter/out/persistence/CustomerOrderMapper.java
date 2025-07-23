package com.company.customerorder.adapter.out.persistence;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.customerorder.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerOrderMapper {
    
    @Mapping(target = "id", source = "id", qualifiedByName = "orderIdToUuid")
    @Mapping(target = "customerId", source = "customerInfo.customerId", qualifiedByName = "customerIdToUuid")
    @Mapping(target = "customerName", source = "customerInfo.name")
    @Mapping(target = "customerEmail", source = "customerInfo.email")
    @Mapping(target = "customerAddress", source = "customerInfo.address")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency", qualifiedByName = "currencyToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToEntity")
    @Mapping(target = "manufacturingOrderId", source = "manufacturingOrderId", qualifiedByName = "orderIdToUuid")
    @Mapping(target = "items", source = "items", qualifiedByName = "itemsToEntities")
    @Mapping(target = "placedAt", source = "placedAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CustomerOrderJpaEntity toEntity(CustomerOrder order);
    
    default CustomerOrder toDomain(CustomerOrderJpaEntity entity) {
        if (entity == null) return null;
        
        return CustomerOrder.reconstitute(
            uuidToOrderId(entity.getId()),
            entityToCustomerInfo(entity),
            entitiesToItems(entity.getItems()),
            entityToMoney(entity),
            entityToStatus(entity.getStatus()),
            entity.getPlacedAt(),
            entity.getUpdatedAt(),
            uuidToOrderId(entity.getManufacturingOrderId())
        );
    }
    
    @Named("orderIdToUuid")
    default UUID orderIdToUuid(OrderId orderId) {
        return orderId != null ? orderId.getValue() : null;
    }
    
    @Named("uuidToOrderId")
    default OrderId uuidToOrderId(UUID uuid) {
        return uuid != null ? OrderId.of(uuid) : null;
    }
    
    @Named("customerIdToUuid")
    default UUID customerIdToUuid(CustomerId customerId) {
        return customerId != null ? customerId.getValue() : null;
    }
    
    @Named("uuidToCustomerId")
    default CustomerId uuidToCustomerId(UUID uuid) {
        return uuid != null ? CustomerId.of(uuid) : null;
    }
    
    @Named("currencyToString")
    default String currencyToString(Currency currency) {
        return currency != null ? currency.getCurrencyCode() : null;
    }
    
    @Named("stringToCurrency")
    default Currency stringToCurrency(String currencyCode) {
        return currencyCode != null ? Currency.getInstance(currencyCode) : null;
    }
    
    @Named("statusToEntity")
    default CustomerOrderJpaEntity.CustomerOrderStatusEntity statusToEntity(CustomerOrderStatus status) {
        return status != null ? CustomerOrderJpaEntity.CustomerOrderStatusEntity.valueOf(status.name()) : null;
    }
    
    @Named("entityToStatus")
    default CustomerOrderStatus entityToStatus(CustomerOrderJpaEntity.CustomerOrderStatusEntity status) {
        return status != null ? CustomerOrderStatus.valueOf(status.name()) : null;
    }
    
    @Named("entityToCustomerInfo")
    default CustomerInfo entityToCustomerInfo(CustomerOrderJpaEntity entity) {
        return CustomerInfo.of(
            CustomerId.of(entity.getCustomerId()),
            entity.getCustomerName(),
            entity.getCustomerEmail(),
            entity.getCustomerAddress()
        );
    }
    
    @Named("entityToMoney")
    default Money entityToMoney(CustomerOrderJpaEntity entity) {
        return Money.of(entity.getTotalAmount(), Currency.getInstance(entity.getCurrency()));
    }
    
    @Named("itemsToEntities")
    default List<OrderItemJpaEntity> itemsToEntities(List<OrderItem> items) {
        return items.stream()
            .map(item -> new OrderItemJpaEntity(
                null,
                null,
                item.getProductCode(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice().getAmount(),
                item.getUnitPrice().getCurrency().getCurrencyCode()
            ))
            .toList();
    }
    
    default List<OrderItemJpaEntity> mapItemsPreservingIds(List<OrderItem> items, List<OrderItemJpaEntity> existingItems) {
        if (existingItems == null || existingItems.isEmpty()) {
            return itemsToEntities(items);
        }
        
        var result = new java.util.ArrayList<OrderItemJpaEntity>();
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            OrderItemJpaEntity entity;
            
            if (i < existingItems.size()) {
                entity = existingItems.get(i);
                entity.setProductCode(item.getProductCode());
                entity.setDescription(item.getDescription());
                entity.setQuantity(item.getQuantity());
                entity.setUnitPrice(item.getUnitPrice().getAmount());
                entity.setCurrency(item.getUnitPrice().getCurrency().getCurrencyCode());
            } else {
                entity = new OrderItemJpaEntity(null, null, item.getProductCode(),
                    item.getDescription(), item.getQuantity(), item.getUnitPrice().getAmount(),
                    item.getUnitPrice().getCurrency().getCurrencyCode());
            }
            result.add(entity);
        }
        return result;
    }
    
    @Named("entitiesToItems")
    default List<OrderItem> entitiesToItems(List<OrderItemJpaEntity> entities) {
        return entities.stream()
            .map(entity -> OrderItem.of(
                entity.getProductCode(),
                entity.getDescription(),
                entity.getQuantity(),
                Money.of(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency()))
            ))
            .toList();
    }
}