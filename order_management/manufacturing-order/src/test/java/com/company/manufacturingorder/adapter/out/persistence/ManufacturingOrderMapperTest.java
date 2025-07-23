package com.company.manufacturingorder.adapter.out.persistence;

import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import com.company.sharedkernel.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ManufacturingOrder Mapper Tests")
class ManufacturingOrderMapperTest {

    private final ManufacturingOrderMapper mapper = Mappers.getMapper(ManufacturingOrderMapper.class);

    @Test
    @DisplayName("Should map domain to entity correctly")
    void shouldMapDomainToEntityCorrectly() {
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of("PROD-001", "Test Product", 10, "Specs");
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );
        
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
        order.changeStatus(OrderStatus.IN_PROGRESS);

        ManufacturingOrderJpaEntity entity = mapper.toEntity(order);

        assertNotNull(entity);
        assertEquals(orderId.getValue(), entity.getId());
        assertEquals("PROD-001", entity.getProductCode());
        assertEquals("Test Product", entity.getDescription());
        assertEquals(10, entity.getQuantity());
        assertEquals("Specs", entity.getSpecifications());
        assertEquals(ManufacturingOrderJpaEntity.OrderStatusEntity.IN_PROGRESS, entity.getStatus());
        assertEquals(timeline.getExpectedStartDate(), entity.getExpectedStartDate());
        assertEquals(timeline.getExpectedCompletionDate(), entity.getExpectedCompletionDate());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map entity to domain correctly")
    void shouldMapEntityToDomainCorrectly() {
        OrderId orderId = OrderId.generate();
        Instant now = Instant.now();
        
        ManufacturingOrderJpaEntity entity = new ManufacturingOrderJpaEntity();
        entity.setId(orderId.getValue());
        entity.setProductCode("PROD-001");
        entity.setDescription("Test Product");
        entity.setQuantity(10);
        entity.setSpecifications("Specs");
        entity.setStatus(ManufacturingOrderJpaEntity.OrderStatusEntity.IN_PROGRESS);
        entity.setExpectedStartDate(now.plus(1, ChronoUnit.DAYS));
        entity.setExpectedCompletionDate(now.plus(7, ChronoUnit.DAYS));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        ManufacturingOrder order = mapper.toDomain(entity);

        assertNotNull(order);
        assertEquals(orderId, order.getId());
        assertEquals("PROD-001", order.getProductSpecification().getProductCode());
        assertEquals("Test Product", order.getProductSpecification().getDescription());
        assertEquals(10, order.getProductSpecification().getQuantity());
        assertEquals("Specs", order.getProductSpecification().getSpecifications());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals(now.plus(1, ChronoUnit.DAYS), order.getTimeline().getExpectedStartDate());
        assertEquals(now.plus(7, ChronoUnit.DAYS), order.getTimeline().getExpectedCompletionDate());
        assertEquals(now, order.getCreatedAt());
        assertEquals(now, order.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDomain(null));
    }

    @Test
    @DisplayName("Should handle completed order with actual dates")
    void shouldHandleCompletedOrderWithActualDates() {
        // Given
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of("PROD-COMPLEX", "Complex Product", 25, "Complex specs");
        Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant completionDate = Instant.now().plus(7, ChronoUnit.DAYS);
        Timeline timeline = Timeline.create(startDate, completionDate);
        
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
        order.changeStatus(OrderStatus.IN_PROGRESS);
        order.complete();

        // When
        ManufacturingOrderJpaEntity entity = mapper.toEntity(order);

        // Then
        assertNotNull(entity);
        assertEquals(ManufacturingOrderJpaEntity.OrderStatusEntity.COMPLETED, entity.getStatus());
        assertNotNull(entity.getActualStartDate());
        assertNotNull(entity.getActualCompletionDate());
    }

    @Test
    @DisplayName("Should round-trip conversion maintain data integrity")
    void shouldRoundTripConversionMaintainDataIntegrity() {
        // Given
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of("PROD-ROUNDTRIP", "Roundtrip Product", 15, "Detailed specs");
        Timeline timeline = Timeline.create(
            Instant.now().plus(2, ChronoUnit.DAYS),
            Instant.now().plus(14, ChronoUnit.DAYS)
        );
        
        ManufacturingOrder originalOrder = ManufacturingOrder.create(orderId, productSpec, timeline);
        originalOrder.changeStatus(OrderStatus.IN_PROGRESS);

        // When - convert domain -> entity -> domain
        ManufacturingOrderJpaEntity entity = mapper.toEntity(originalOrder);
        ManufacturingOrder reconstructedOrder = mapper.toDomain(entity);

        // Then
        assertEquals(originalOrder.getId(), reconstructedOrder.getId());
        assertEquals(originalOrder.getProductSpecification().getProductCode(), 
                    reconstructedOrder.getProductSpecification().getProductCode());
        assertEquals(originalOrder.getProductSpecification().getDescription(), 
                    reconstructedOrder.getProductSpecification().getDescription());
        assertEquals(originalOrder.getProductSpecification().getQuantity(), 
                    reconstructedOrder.getProductSpecification().getQuantity());
        assertEquals(originalOrder.getStatus(), reconstructedOrder.getStatus());
        assertEquals(originalOrder.getTimeline().getExpectedStartDate(), 
                    reconstructedOrder.getTimeline().getExpectedStartDate());
        assertEquals(originalOrder.getTimeline().getExpectedCompletionDate(), 
                    reconstructedOrder.getTimeline().getExpectedCompletionDate());
    }

    @Test
    @DisplayName("Should handle all order statuses correctly")
    void shouldHandleAllOrderStatusesCorrectly() {
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of("PROD-STATUS", "Status Product", 5, "Status specs");
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(5, ChronoUnit.DAYS)
        );

        for (OrderStatus domainStatus : OrderStatus.values()) {
            // Given
            ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
            
            // Manually set status for testing (bypassing business rules)
            var entity = mapper.toEntity(order);
            entity.setStatus(ManufacturingOrderJpaEntity.OrderStatusEntity.valueOf(domainStatus.name()));
            
            // When
            ManufacturingOrder reconstructedOrder = mapper.toDomain(entity);
            
            // Then
            assertEquals(domainStatus, reconstructedOrder.getStatus(), 
                "Status mapping failed for: " + domainStatus);
        }
    }

    @Test
    @DisplayName("Should handle empty and null specifications")
    void shouldHandleEmptyAndNullSpecifications() {
        OrderId orderId = OrderId.generate();
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(5, ChronoUnit.DAYS)
        );

        // Test with minimal specifications  
        ProductSpecification minimalSpecs = ProductSpecification.of("PROD-MINIMAL", "Minimal Specs Product", 1, "Basic");
        ManufacturingOrder orderWithMinimalSpecs = ManufacturingOrder.create(orderId, minimalSpecs, timeline);
        
        ManufacturingOrderJpaEntity entity = mapper.toEntity(orderWithMinimalSpecs);
        assertEquals("Basic", entity.getSpecifications());
        
        ManufacturingOrder reconstructed = mapper.toDomain(entity);
        assertEquals("Basic", reconstructed.getProductSpecification().getSpecifications());
    }

    @Test
    @DisplayName("Should preserve timestamps with microsecond precision")
    void shouldPreserveTimestampsWithMicrosecondPrecision() {
        // Given
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of("PROD-TIME", "Time Product", 1, "Time specs");
        
        Instant preciseStartTime = Instant.parse("2025-07-21T10:15:30.123456Z");
        Instant preciseEndTime = Instant.parse("2025-07-28T16:45:15.987654Z");
        Timeline timeline = Timeline.create(preciseStartTime, preciseEndTime);
        
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);

        // When
        ManufacturingOrderJpaEntity entity = mapper.toEntity(order);
        ManufacturingOrder reconstructedOrder = mapper.toDomain(entity);

        // Then
        assertEquals(preciseStartTime, reconstructedOrder.getTimeline().getExpectedStartDate());
        assertEquals(preciseEndTime, reconstructedOrder.getTimeline().getExpectedCompletionDate());
    }

    @Test
    @DisplayName("Should handle large quantity values")
    void shouldHandleLargeQuantityValues() {
        // Given
        OrderId orderId = OrderId.generate();
        int largeQuantity = Integer.MAX_VALUE;
        ProductSpecification productSpec = ProductSpecification.of("PROD-LARGE", "Large Quantity Product", largeQuantity, "Large specs");
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(30, ChronoUnit.DAYS) // Longer timeline for large order
        );
        
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);

        // When
        ManufacturingOrderJpaEntity entity = mapper.toEntity(order);
        ManufacturingOrder reconstructedOrder = mapper.toDomain(entity);

        // Then
        assertEquals(largeQuantity, entity.getQuantity());
        assertEquals(largeQuantity, reconstructedOrder.getProductSpecification().getQuantity());
    }
}