package com.company.manufacturingorder.domain.model;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.events.ManufacturingOrderCreated;
import com.company.sharedkernel.events.ManufacturingOrderStatusChanged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Manufacturing Order Aggregate Tests")
class ManufacturingOrderTest {

    private OrderId orderId;
    private ProductSpecification productSpec;
    private Timeline timeline;

    @BeforeEach
    void setUp() {
        orderId = OrderId.generate();
        productSpec = ProductSpecification.of(
            "PROD-001",
            "Test Product",
            10,
            "Standard specifications"
        );
        timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("Should create manufacturing order with pending status")
    void shouldCreateManufacturingOrderWithPendingStatus() {
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);

        assertEquals(orderId, order.getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(productSpec, order.getProductSpecification());
        assertEquals(timeline, order.getTimeline());
        assertNotNull(order.getCreatedAt());

        // Check domain event
        assertEquals(1, order.getDomainEvents().size());
        assertTrue(order.getDomainEvents().get(0) instanceof ManufacturingOrderCreated);
    }

    @Test
    @DisplayName("Should transition from PENDING to IN_PROGRESS")
    void shouldTransitionFromPendingToInProgress() {
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
        order.clearDomainEvents(); // Clear creation event

        order.changeStatus(OrderStatus.IN_PROGRESS);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertTrue(order.isInProgress());

        // Check domain event
        assertEquals(1, order.getDomainEvents().size());
        assertTrue(order.getDomainEvents().get(0) instanceof ManufacturingOrderStatusChanged);
    }

    @Test
    @DisplayName("Should complete order and generate completion event")
    void shouldCompleteOrderAndGenerateCompletionEvent() {
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
        order.changeStatus(OrderStatus.IN_PROGRESS);
        order.clearDomainEvents();

        order.complete();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.isCompleted());
        assertNotNull(order.getTimeline().getActualCompletionDate());

        // Should have both status change and completion events
        assertEquals(2, order.getDomainEvents().size());
    }

    @Test
    @DisplayName("Should cancel order from any non-terminal status")
    void shouldCancelOrderFromAnyNonTerminalStatus() {
        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);

        order.cancel();


        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isCancelled());
    }

    @Test
    @DisplayName("Should identify overdue orders")
    void shouldIdentifyOverdueOrders() {
        Timeline pastTimeline = Timeline.create(
            Instant.now().minus(7, ChronoUnit.DAYS),
            Instant.now().minus(1, ChronoUnit.DAYS)
        );

        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, pastTimeline);

        assertTrue(order.isOverdue());
    }
}