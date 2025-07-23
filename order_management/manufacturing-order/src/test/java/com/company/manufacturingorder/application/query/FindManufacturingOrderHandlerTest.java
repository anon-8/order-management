package com.company.manufacturingorder.application.query;

import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.sharedkernel.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Find Manufacturing Order Handler Tests")
class FindManufacturingOrderHandlerTest {

    @Mock
    private ManufacturingOrderRepository repository;

    private FindManufacturingOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FindManufacturingOrderHandler(repository);
    }

    @Test
    @DisplayName("Should return order DTO when order exists")
    void shouldReturnOrderDtoWhenOrderExists() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId);
        FindManufacturingOrderQuery query = new FindManufacturingOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<ManufacturingOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        ManufacturingOrderDto dto = result.get();
        assertEquals(orderId, dto.orderId());
        assertEquals(OrderStatus.PENDING, dto.status());
        assertEquals("PROD-001", dto.productCode());
        assertNotNull(dto.createdAt());
    }

    @Test
    @DisplayName("Should return empty when order does not exist")
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        OrderId orderId = OrderId.generate();
        FindManufacturingOrderQuery query = new FindManufacturingOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.empty());

        Optional<ManufacturingOrderDto> result = handler.handle(query);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should map all order properties correctly")
    void shouldMapAllOrderPropertiesCorrectly() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId);
        order.changeStatus(OrderStatus.IN_PROGRESS);
        
        FindManufacturingOrderQuery query = new FindManufacturingOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<ManufacturingOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        ManufacturingOrderDto dto = result.get();
        assertEquals(orderId, dto.orderId());
        assertEquals(OrderStatus.IN_PROGRESS, dto.status());
        assertEquals("Test Product", dto.description());
        assertEquals(10, dto.quantity());
        assertNotNull(dto.expectedStartDate());
        assertNotNull(dto.expectedCompletionDate());
    }

    @Test
    @DisplayName("Should handle completed orders with actual completion date")
    void shouldHandleCompletedOrdersWithActualCompletionDate() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId);
        order.changeStatus(OrderStatus.IN_PROGRESS);
        order.complete();
        
        FindManufacturingOrderQuery query = new FindManufacturingOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<ManufacturingOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        ManufacturingOrderDto dto = result.get();
        assertEquals(OrderStatus.COMPLETED, dto.status());
        assertNotNull(dto.actualCompletionDate());
    }

    @Test
    @DisplayName("Should handle repository exceptions")
    void shouldHandleRepositoryExceptions() {
        OrderId orderId = OrderId.generate();
        FindManufacturingOrderQuery query = new FindManufacturingOrderQuery(orderId);

        when(repository.findById(orderId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> handler.handle(query));
    }

    private ManufacturingOrder createTestOrder(OrderId orderId) {
        ProductSpecification productSpec = ProductSpecification.of(
            "PROD-001", "Test Product", 10, "Standard specifications"
        );
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );

        return ManufacturingOrder.create(orderId, productSpec, timeline);
    }
}