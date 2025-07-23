package com.company.manufacturingorder.adapter.out.persistence;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ManufacturingOrderMapper {
    
    @Mapping(target = "id", source = "id", qualifiedByName = "orderIdToUuid")
    @Mapping(target = "productCode", source = "productSpecification.productCode")
    @Mapping(target = "description", source = "productSpecification.description")
    @Mapping(target = "quantity", source = "productSpecification.quantity")
    @Mapping(target = "specifications", source = "productSpecification.specifications")
    @Mapping(target = "status", source = "status", qualifiedByName = "orderStatusToEntity")
    @Mapping(target = "expectedStartDate", source = "timeline.expectedStartDate")
    @Mapping(target = "expectedCompletionDate", source = "timeline.expectedCompletionDate")
    @Mapping(target = "actualStartDate", source = "timeline.actualStartDate")
    @Mapping(target = "actualCompletionDate", source = "timeline.actualCompletionDate")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ManufacturingOrderJpaEntity toEntity(ManufacturingOrder order);
    
    default ManufacturingOrder toDomain(ManufacturingOrderJpaEntity entity) {
        if (entity == null) return null;
        
        return ManufacturingOrder.reconstitute(
            uuidToOrderId(entity.getId()),
            entityToProductSpec(entity),
            entityToOrderStatus(entity.getStatus()),
            entityToTimeline(entity),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
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
    
    @Named("orderStatusToEntity")
    default ManufacturingOrderJpaEntity.OrderStatusEntity orderStatusToEntity(OrderStatus status) {
        return status != null ? ManufacturingOrderJpaEntity.OrderStatusEntity.valueOf(status.name()) : null;
    }
    
    @Named("entityToOrderStatus")
    default OrderStatus entityToOrderStatus(ManufacturingOrderJpaEntity.OrderStatusEntity status) {
        return status != null ? OrderStatus.valueOf(status.name()) : null;
    }
    
    @Named("entityToProductSpec")
    default ProductSpecification entityToProductSpec(ManufacturingOrderJpaEntity entity) {
        return ProductSpecification.of(
            entity.getProductCode(),
            entity.getDescription(),
            entity.getQuantity(),
            entity.getSpecifications()
        );
    }
    
    @Named("entityToTimeline")
    default Timeline entityToTimeline(ManufacturingOrderJpaEntity entity) {
        var timeline = Timeline.create(
            entity.getExpectedStartDate(),
            entity.getExpectedCompletionDate()
        );
        
        if (entity.getActualStartDate() != null) {
            timeline = timeline.withActualStartDate(entity.getActualStartDate());
        }
        
        if (entity.getActualCompletionDate() != null) {
            timeline = timeline.withActualCompletionDate(entity.getActualCompletionDate());
        }
        
        return timeline;
    }
}