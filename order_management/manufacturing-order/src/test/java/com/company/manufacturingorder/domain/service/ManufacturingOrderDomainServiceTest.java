package com.company.manufacturingorder.domain.service;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Manufacturing Order Domain Service Tests")
class ManufacturingOrderDomainServiceTest {

    @Mock
    private ManufacturingOrderRepository repository;

    private ManufacturingOrderDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new ManufacturingOrderDomainService(repository);
    }

    @Test
    @DisplayName("Should allow scheduling new order when under capacity")
    void shouldAllowSchedulingNewOrderWhenUnderCapacity() {
        when(repository.findByStatus(OrderStatus.IN_PROGRESS)).thenReturn(List.of());

        boolean canSchedule = domainService.canScheduleNewOrder();

        assertTrue(canSchedule);
        verify(repository).findByStatus(OrderStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should prevent scheduling when at capacity")
    void shouldPreventSchedulingWhenAtCapacity() {
        List<ManufacturingOrder> maxOrders = List.of(
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS)
        );
        when(repository.findByStatus(OrderStatus.IN_PROGRESS)).thenReturn(maxOrders);

        boolean canSchedule = domainService.canScheduleNewOrder();

        assertFalse(canSchedule);
    }

    @Test
    @DisplayName("Should find orders requiring attention")
    void shouldFindOrdersRequiringAttention() {
        ManufacturingOrder overdueOrder = createOverdueOrder();
        when(repository.findOverdueOrders()).thenReturn(List.of(overdueOrder));

        List<ManufacturingOrder> ordersRequiringAttention = domainService.findOrdersRequiringAttention();

        assertEquals(1, ordersRequiringAttention.size());
        assertEquals(overdueOrder.getId(), ordersRequiringAttention.get(0).getId());
        verify(repository).findOverdueOrders();
    }

    @Test
    @DisplayName("Should validate order scheduling successfully")
    void shouldValidateOrderSchedulingSuccessfully() {
        ManufacturingOrder order = createTestOrderWithFutureStart();
        when(repository.findByStatus(OrderStatus.IN_PROGRESS)).thenReturn(List.of());

        assertDoesNotThrow(() -> domainService.validateOrderScheduling(order));
    }

    @Test
    @DisplayName("Should throw exception when scheduling order with past start date")
    void shouldThrowExceptionWhenSchedulingOrderWithPastStartDate() {
        ManufacturingOrder order = createTestOrderWithPastStart();
        when(repository.findByStatus(OrderStatus.IN_PROGRESS)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> domainService.validateOrderScheduling(order));
    }

    @Test
    @DisplayName("Should throw exception when scheduling at capacity")
    void shouldThrowExceptionWhenSchedulingAtCapacity() {
        ManufacturingOrder order = createTestOrderWithFutureStart();
        List<ManufacturingOrder> maxOrders = createMaxCapacityOrders();
        when(repository.findByStatus(OrderStatus.IN_PROGRESS)).thenReturn(maxOrders);

        assertThrows(IllegalStateException.class, () -> domainService.validateOrderScheduling(order));
    }

    private ManufacturingOrder createTestOrder(OrderStatus status) {
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of(
            "PROD-001", "Test Product", 10, "Standard specifications"
        );
        Timeline timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );

        ManufacturingOrder order = ManufacturingOrder.create(orderId, productSpec, timeline);
        if (status != OrderStatus.PENDING) {
            order.changeStatus(status);
        }
        return order;
    }

    private ManufacturingOrder createOverdueOrder() {
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of(
            "PROD-002", "Overdue Product", 5, "Specifications"
        );
        Timeline pastTimeline = Timeline.create(
            Instant.now().minus(10, ChronoUnit.DAYS),
            Instant.now().minus(1, ChronoUnit.DAYS)
        );

        return ManufacturingOrder.create(orderId, productSpec, pastTimeline);
    }

    private ManufacturingOrder createTestOrderWithFutureStart() {
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of(
            "PROD-003", "Future Product", 5, "Future specifications"
        );
        Timeline timeline = Timeline.create(
            Instant.now().plus(2, ChronoUnit.DAYS),
            Instant.now().plus(10, ChronoUnit.DAYS)
        );

        return ManufacturingOrder.create(orderId, productSpec, timeline);
    }

    private ManufacturingOrder createTestOrderWithPastStart() {
        OrderId orderId = OrderId.generate();
        ProductSpecification productSpec = ProductSpecification.of(
            "PROD-004", "Past Product", 5, "Past specifications"
        );
        Timeline timeline = Timeline.create(
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(5, ChronoUnit.DAYS)
        );

        return ManufacturingOrder.create(orderId, productSpec, timeline);
    }

    private List<ManufacturingOrder> createMaxCapacityOrders() {
        return List.of(
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS),
            createTestOrder(OrderStatus.IN_PROGRESS)
        );
    }
}