package com.company.manufacturingorder.adapter.out.persistence;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.DomainEventPublisher;
import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.config.TestConfiguration;
import com.company.manufacturingorder.domain.model.ManufacturingOrder;
import com.company.manufacturingorder.adapter.out.persistence.ManufacturingOrderJpaRepository;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({RepositoryTestConfig.class, ManufacturingOrderRepositoryAdapter.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
class ManufacturingOrderRepositoryAdapterIntegrationTest {

    @Autowired
    private ManufacturingOrderRepositoryAdapter repository;

    @Autowired
    private ManufacturingOrderJpaRepository jpaRepository;

    @Autowired
    private DomainEventPublisher eventPublisher;

    private OrderId testOrderId;
    private ProductSpecification testProductSpec;
    private Timeline testTimeline;

    @BeforeEach
    void setUp() {
        reset(eventPublisher);
        testOrderId = OrderId.of(UUID.randomUUID());
        testProductSpec = ProductSpecification.of(
            "PROD-001",
            "Test Product",
            10,
            "Standard specifications"
        );
        testTimeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );
    }

    @Test
    void shouldSaveManufacturingOrderAndPublishDomainEvents() {
        // Given
        var manufacturingOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        
        // When
        var savedOrder = repository.save(manufacturingOrder);
        
        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(testOrderId);
        assertThat(savedOrder.getProductSpecification().getProductCode()).isEqualTo("PROD-001");
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        
        // Verify domain events were published
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        DomainEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.eventType()).isEqualTo("ManufacturingOrderCreated");
    }

    @Test
    void shouldFindOrderById() {
        // Given
        var manufacturingOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        repository.save(manufacturingOrder);
        
        // When
        var foundOrder = repository.findById(testOrderId);
        
        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(testOrderId);
        assertThat(foundOrder.get().getProductSpecification().getProductCode()).isEqualTo("PROD-001");
    }

    @Test
    void shouldFindOrdersByStatus() {
        // Given
        var order1 = ManufacturingOrder.create(
            OrderId.of(UUID.randomUUID()), testProductSpec, testTimeline);
        var order2 = ManufacturingOrder.create(
            OrderId.of(UUID.randomUUID()), testProductSpec, testTimeline);
        
        repository.save(order1);
        repository.save(order2);
        
        // When
        var pendingOrders = repository.findByStatus(OrderStatus.PENDING);
        
        // Then
        assertThat(pendingOrders).hasSize(2);
        assertThat(pendingOrders)
            .extracting(ManufacturingOrder::getStatus)
            .containsOnly(OrderStatus.PENDING);
    }

    @Test
    void shouldUpdateOrderStatusAndPublishEvents() {
        // Given
        var manufacturingOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        var savedOrder = repository.save(manufacturingOrder);
        
        // When
        savedOrder.changeStatus(OrderStatus.IN_PROGRESS);
        repository.save(savedOrder);
        
        // Then
        var updatedOrder = repository.findById(testOrderId);
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        
        // Verify events were published (create + status change)
        verify(eventPublisher, times(2)).publishEvent(any(DomainEvent.class));
    }

    @Test
    void shouldFindOverdueOrders() {
        // Given - create order with past completion date
        var pastTimeline = Timeline.create(
            Instant.now().minus(10, ChronoUnit.DAYS),
            Instant.now().minus(1, ChronoUnit.DAYS) // Past due
        );
        var overdueOrder = ManufacturingOrder.create(
            OrderId.of(UUID.randomUUID()), testProductSpec, pastTimeline);
        
        var normalOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        
        repository.save(overdueOrder);
        repository.save(normalOrder);
        
        // When
        var overdueOrders = repository.findOverdueOrders();
        
        // Then
        assertThat(overdueOrders).hasSize(1);
        assertThat(overdueOrders.get(0).isOverdue()).isTrue();
    }

    @Test
    void shouldDeleteOrder() {
        // Given
        var manufacturingOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        repository.save(manufacturingOrder);
        
        // When
        repository.delete(testOrderId);
        
        // Then
        var deletedOrder = repository.findById(testOrderId);
        assertThat(deletedOrder).isEmpty();
    }

    @Test
    void shouldCheckOrderExistence() {
        // Given
        var manufacturingOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        repository.save(manufacturingOrder);
        
        // When & Then
        assertThat(repository.existsById(testOrderId)).isTrue();
        assertThat(repository.existsById(OrderId.of(UUID.randomUUID()))).isFalse();
    }

    @Test
    void shouldFindAllOrders() {
        // Given
        var order1 = ManufacturingOrder.create(
            OrderId.of(UUID.randomUUID()), testProductSpec, testTimeline);
        var order2 = ManufacturingOrder.create(
            OrderId.of(UUID.randomUUID()), testProductSpec, testTimeline);
        
        repository.save(order1);
        repository.save(order2);
        
        // When
        var allOrders = repository.findAll();
        
        // Then
        assertThat(allOrders).hasSize(2);
    }

    @Test
    void shouldHandleCompleteOrderWithEvents() {
        // Given
        var manufacturingOrder = ManufacturingOrder.create(testOrderId, testProductSpec, testTimeline);
        var savedOrder = repository.save(manufacturingOrder);
        
        // When
        savedOrder.changeStatus(OrderStatus.IN_PROGRESS);
        savedOrder.complete();
        repository.save(savedOrder);
        
        // Then
        var completedOrder = repository.findById(testOrderId);
        assertThat(completedOrder).isPresent();
        assertThat(completedOrder.get().getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.get().isCompleted()).isTrue();
        
        // Verify all events were published (create + in_progress + status_change_to_completed + completed)
        verify(eventPublisher, times(4)).publishEvent(any(DomainEvent.class));
    }
}