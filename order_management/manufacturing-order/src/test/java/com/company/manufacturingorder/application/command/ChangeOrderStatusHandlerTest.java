package com.company.manufacturingorder.application.command;

import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.manufacturingorder.domain.service.ManufacturingOrderDomainService;
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
@DisplayName("Change Order Status Handler Tests")
class ChangeOrderStatusHandlerTest {

    @Mock
    private ManufacturingOrderRepository repository;

    @Mock
    private ManufacturingOrderDomainService domainService;

    private ChangeOrderStatusHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChangeOrderStatusHandler(repository);
    }

    @Test
    @DisplayName("Should change order status when order exists")
    void shouldChangeOrderStatusWhenOrderExists() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId, OrderStatus.PENDING);
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(orderId, OrderStatus.IN_PROGRESS);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        handler.handle(command);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        verify(repository).save(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        OrderId orderId = OrderId.generate();
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(orderId, OrderStatus.IN_PROGRESS);

        when(repository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle transition to completed status")
    void shouldHandleTransitionToCompletedStatus() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId, OrderStatus.IN_PROGRESS);
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(orderId, OrderStatus.COMPLETED);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        handler.handle(command);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(repository).save(order);
    }

    @Test
    @DisplayName("Should handle transition from pending to cancelled")
    void shouldHandleTransitionFromPendingToCancelled() {
        OrderId orderId = OrderId.generate();
        ManufacturingOrder order = createTestOrder(orderId, OrderStatus.PENDING);
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(orderId, OrderStatus.CANCELLED);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        handler.handle(command);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(repository).save(order);
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