package com.company.customerorder.adapter.out.persistence;

import com.company.sharedkernel.DomainEvent;
import com.company.sharedkernel.DomainEventPublisher;
import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.customerorder.config.TestConfiguration;
import com.company.customerorder.domain.model.*;
import com.company.customerorder.adapter.out.persistence.CustomerOrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({RepositoryTestConfig.class, CustomerOrderRepositoryAdapter.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
class CustomerOrderRepositoryAdapterIntegrationTest {

    @Autowired
    private CustomerOrderRepositoryAdapter repository;

    @Autowired
    private CustomerOrderJpaRepository jpaRepository;

    @Autowired
    private DomainEventPublisher eventPublisher;

    private OrderId testOrderId;
    private CustomerInfo testCustomerInfo;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        reset(eventPublisher);
        testOrderId = OrderId.of(UUID.randomUUID());
        testCustomerInfo = CustomerInfo.of(
            CustomerId.of(UUID.randomUUID()),
            "John Doe",
            "john.doe@example.com",
            "123 Main St, City, Country"
        );
        
        var item1 = OrderItem.of(
            "PROD-001",
            "Test Product 1",
            5,
            Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"))
        );
        
        var item2 = OrderItem.of(
            "PROD-002",
            "Test Product 2",
            3,
            Money.of(new BigDecimal("150.00"), Currency.getInstance("USD"))
        );
        
        testItems = List.of(item1, item2);
    }

    @Test
    void shouldSaveCustomerOrderAndPublishDomainEvents() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        
        // When
        var savedOrder = repository.save(customerOrder);
        
        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(testOrderId);
        assertThat(savedOrder.getCustomerInfo().getName()).isEqualTo("John Doe");
        assertThat(savedOrder.getStatus()).isEqualTo(CustomerOrderStatus.PLACED);
        assertThat(savedOrder.getItems()).hasSize(2);
        assertThat(savedOrder.getTotalAmount().getAmount()).isEqualTo(new BigDecimal("950.00"));
        
        // Verify domain events were published
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        DomainEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.eventType()).isEqualTo("CustomerOrderPlaced");
    }

    @Test
    void shouldFindOrderById() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        repository.save(customerOrder);
        
        // When
        var foundOrder = repository.findById(testOrderId);
        
        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(testOrderId);
        assertThat(foundOrder.get().getCustomerInfo().getName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindOrdersByCustomerId() {
        // Given
        var order1 = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var order2 = CustomerOrder.placeOrder(
            OrderId.of(UUID.randomUUID()), testCustomerInfo, testItems);
        
        repository.save(order1);
        repository.save(order2);
        
        // When
        var customerOrders = repository.findByCustomerId(testCustomerInfo.getCustomerId());
        
        // Then
        assertThat(customerOrders).hasSize(2);
        assertThat(customerOrders)
            .extracting(order -> order.getCustomerInfo().getCustomerId())
            .containsOnly(testCustomerInfo.getCustomerId());
    }

    @Test
    void shouldFindOrdersByStatus() {
        // Given
        var order1 = CustomerOrder.placeOrder(
            OrderId.of(UUID.randomUUID()), testCustomerInfo, testItems);
        var order2 = CustomerOrder.placeOrder(
            OrderId.of(UUID.randomUUID()), testCustomerInfo, testItems);
        
        repository.save(order1);
        repository.save(order2);
        
        // When
        var placedOrders = repository.findByStatus(CustomerOrderStatus.PLACED);
        
        // Then
        assertThat(placedOrders).hasSize(2);
        assertThat(placedOrders)
            .extracting(CustomerOrder::getStatus)
            .containsOnly(CustomerOrderStatus.PLACED);
    }

    @Test
    void shouldUpdateOrderStatusAndPublishEvents() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var savedOrder = repository.save(customerOrder);
        
        // When
        savedOrder.confirm();
        repository.save(savedOrder);
        
        // Then
        var updatedOrder = repository.findById(testOrderId);
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getStatus()).isEqualTo(CustomerOrderStatus.CONFIRMED);
        
        // Verify events were published (place + confirm)
        verify(eventPublisher, times(2)).publishEvent(any(DomainEvent.class));
    }

    @Test
    void shouldLinkManufacturingOrderAndUpdateStatus() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var savedOrder = repository.save(customerOrder);
        savedOrder.confirm();
        
        var manufacturingOrderId = OrderId.of(UUID.randomUUID());
        
        // When
        savedOrder.linkManufacturingOrder(manufacturingOrderId);
        savedOrder.notifyManufacturingStarted();
        repository.save(savedOrder);
        
        // Then
        var updatedOrder = repository.findById(testOrderId);
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getManufacturingOrderId()).isEqualTo(manufacturingOrderId);
        assertThat(updatedOrder.get().getStatus()).isEqualTo(CustomerOrderStatus.MANUFACTURING_IN_PROGRESS);
    }

    @Test
    void shouldNotifyManufacturingCompletedAndUpdateStatus() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var savedOrder = repository.save(customerOrder);
        savedOrder.confirm();
        savedOrder.linkManufacturingOrder(OrderId.of(UUID.randomUUID()));
        savedOrder.notifyManufacturingStarted();
        
        // When
        savedOrder.notifyManufacturingCompleted();
        repository.save(savedOrder);
        
        // Then
        var updatedOrder = repository.findById(testOrderId);
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getStatus()).isEqualTo(CustomerOrderStatus.MANUFACTURING_COMPLETED);
    }

    @Test
    void shouldHandleOrderCancellation() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var savedOrder = repository.save(customerOrder);
        
        // When
        savedOrder.cancel("Customer requested cancellation");
        repository.save(savedOrder);
        
        // Then
        var cancelledOrder = repository.findById(testOrderId);
        assertThat(cancelledOrder).isPresent();
        assertThat(cancelledOrder.get().getStatus()).isEqualTo(CustomerOrderStatus.CANCELLED);
        assertThat(cancelledOrder.get().isCancelled()).isTrue();
        
        // Verify cancellation event was published
        verify(eventPublisher, times(2)).publishEvent(any(DomainEvent.class)); // place + cancel
    }

    @Test
    void shouldFindActiveOrders() {
        // Given
        var activeOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var cancelledOrderId = OrderId.of(UUID.randomUUID());
        var cancelledOrder = CustomerOrder.placeOrder(cancelledOrderId, testCustomerInfo, testItems);
        
        repository.save(activeOrder);
        repository.save(cancelledOrder);
        
        var savedCancelledOrder = repository.findById(cancelledOrderId).orElseThrow();
        savedCancelledOrder.cancel("Test cancellation");
        repository.save(savedCancelledOrder);
        
        // When
        var activeOrders = repository.findActiveOrders();
        
        // Then
        assertThat(activeOrders).hasSize(1);
        assertThat(activeOrders.get(0).getId()).isEqualTo(testOrderId);
        assertThat(activeOrders.get(0).isActive()).isTrue();
    }

    @Test
    void shouldCompleteOrderLifecycle() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        var savedOrder = repository.save(customerOrder);
        
        // When - simulate full order lifecycle
        savedOrder.confirm();
        savedOrder.linkManufacturingOrder(OrderId.of(UUID.randomUUID()));
        savedOrder.notifyManufacturingStarted();
        savedOrder.notifyManufacturingCompleted();
        savedOrder.markAsShipped();
        savedOrder.markAsDelivered();
        repository.save(savedOrder);
        
        // Then
        var completedOrder = repository.findById(testOrderId);
        assertThat(completedOrder).isPresent();
        assertThat(completedOrder.get().getStatus()).isEqualTo(CustomerOrderStatus.DELIVERED);
        assertThat(completedOrder.get().isDelivered()).isTrue();
        assertThat(completedOrder.get().isActive()).isFalse();
        
        // Verify all status change events were published (place + confirm + manufacturing_started + manufacturing_completed + shipped + delivered)
        verify(eventPublisher, times(6)).publishEvent(any(DomainEvent.class));
    }

    @Test
    void shouldDeleteOrder() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        repository.save(customerOrder);
        
        // When
        repository.delete(testOrderId);
        
        // Then
        var deletedOrder = repository.findById(testOrderId);
        assertThat(deletedOrder).isEmpty();
    }

    @Test
    void shouldCheckOrderExistence() {
        // Given
        var customerOrder = CustomerOrder.placeOrder(testOrderId, testCustomerInfo, testItems);
        repository.save(customerOrder);
        
        // When & Then
        assertThat(repository.existsById(testOrderId)).isTrue();
        assertThat(repository.existsById(OrderId.of(UUID.randomUUID()))).isFalse();
    }

    @Test
    void shouldFindAllOrders() {
        // Given
        var order1 = CustomerOrder.placeOrder(
            OrderId.of(UUID.randomUUID()), testCustomerInfo, testItems);
        var order2 = CustomerOrder.placeOrder(
            OrderId.of(UUID.randomUUID()), testCustomerInfo, testItems);
        
        repository.save(order1);
        repository.save(order2);
        
        // When
        var allOrders = repository.findAll();
        
        // Then
        assertThat(allOrders).hasSize(2);
    }
}