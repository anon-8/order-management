package com.company.manufacturingorder.application.command;

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
@DisplayName("Complete Manufacturing Order Handler Tests")
class CompleteManufacturingOrderHandlerTest {

    @Mock
    private ManufacturingOrderRepository repository;

    private CompleteManufacturingOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CompleteManufacturingOrderHandler(repository);
    }

    @Test
    @DisplayName("Should complete manufacturing order successfully")
    void shouldCompleteManufacturingOrderSuccessfully() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId, OrderStatus.IN_PROGRESS);
        CompleteManufacturingOrderCommand command = new CompleteManufacturingOrderCommand(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        handler.handle(command);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getTimeline().getActualCompletionDate());
        verify(repository).save(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        OrderId orderId = OrderId.generate();
        CompleteManufacturingOrderCommand command = new CompleteManufacturingOrderCommand(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should complete already completed order without error")
    void shouldCompleteAlreadyCompletedOrderWithoutError() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId, OrderStatus.IN_PROGRESS);
        order.changeStatus(OrderStatus.COMPLETED); // Transition properly through valid states
        CompleteManufacturingOrderCommand command = new CompleteManufacturingOrderCommand(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> handler.handle(command));
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(repository).save(order);
    }

    @Test
    @DisplayName("Should not complete cancelled order")
    void shouldNotCompleteCancelledOrder() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId, OrderStatus.IN_PROGRESS);
        order.changeStatus(OrderStatus.CANCELLED); // Transition properly
        CompleteManufacturingOrderCommand command = new CompleteManufacturingOrderCommand(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> handler.handle(command));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(repository, never()).save(any());
    }

    private ManufacturingOrder createTestOrder(OrderId orderId, OrderStatus initialStatus) {
        ProductSpecification productSpec = ProductSpecification.of(
            "PROD-001", "Test Product", 10, "Standard specifications"
        );
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );

        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
        if (initialStatus != OrderStatus.PENDING) {
            order.changeStatus(initialStatus);
        }
        return order;
    }
}