package com.company.customerorder.adapter.in.event;

import com.company.sharedkernel.events.ManufacturingOrderCompleted;
import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.customerorder.domain.model.*;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManufacturingOrderEventHandlerTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @InjectMocks
    private ManufacturingOrderEventHandler eventHandler;

    private OrderId manufacturingOrderId;
    private OrderId customerOrderId;
    private CustomerOrder customerOrder;

    @BeforeEach
    void setUp() {
        manufacturingOrderId = OrderId.of(UUID.randomUUID());
        customerOrderId = OrderId.of(UUID.randomUUID());
        
        var customerInfo = CustomerInfo.of(
            CustomerId.of(UUID.randomUUID()),
            "John Doe",
            "john.doe@example.com",
            "123 Main St"
        );
        
        var items = List.of(
            OrderItem.of("PROD-001", "Test Product", 2, 
                Money.of(new BigDecimal("100.00"), Currency.getInstance("USD")))
        );
        
        customerOrder = CustomerOrder.placeOrder(customerOrderId, customerInfo, items);
        customerOrder.confirm();
        customerOrder.linkManufacturingOrder(manufacturingOrderId);
        customerOrder.notifyManufacturingStarted();
    }

    @Test
    void shouldHandleManufacturingOrderCompletedEvent() {
        // Given
        var completedEvent = ManufacturingOrderCompleted.of(manufacturingOrderId, Instant.now());
        
        when(customerOrderRepository.findByManufacturingOrderId(manufacturingOrderId)).thenReturn(List.of(customerOrder));
        
        // When
        eventHandler.handleManufacturingOrderCompleted(completedEvent);
        
        // Then
        verify(customerOrderRepository, times(1)).findByManufacturingOrderId(manufacturingOrderId);
        verify(customerOrderRepository, times(1)).save(argThat(order -> 
            order.getStatus() == CustomerOrderStatus.MANUFACTURING_COMPLETED
        ));
    }

    @Test
    void shouldIgnoreEventWhenNoMatchingCustomerOrders() {
        // Given
        var unmatchedManufacturingOrderId = OrderId.of(UUID.randomUUID());
        var completedEvent = ManufacturingOrderCompleted.of(unmatchedManufacturingOrderId, Instant.now());
        
        when(customerOrderRepository.findByManufacturingOrderId(unmatchedManufacturingOrderId)).thenReturn(List.of());
        
        // When
        eventHandler.handleManufacturingOrderCompleted(completedEvent);
        
        // Then
        verify(customerOrderRepository, times(1)).findByManufacturingOrderId(unmatchedManufacturingOrderId);
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    void shouldHandleMultipleMatchingCustomerOrders() {
        // Given
        var secondCustomerOrderId = OrderId.of(UUID.randomUUID());
        var customerInfo = CustomerInfo.of(
            CustomerId.of(UUID.randomUUID()),
            "Jane Doe",
            "jane.doe@example.com",
            "456 Oak Ave"
        );
        
        var items = List.of(
            OrderItem.of("PROD-002", "Another Product", 1, 
                Money.of(new BigDecimal("200.00"), Currency.getInstance("USD")))
        );
        
        var secondCustomerOrder = CustomerOrder.placeOrder(secondCustomerOrderId, customerInfo, items);
        secondCustomerOrder.confirm();
        secondCustomerOrder.linkManufacturingOrder(manufacturingOrderId); // Same manufacturing order
        secondCustomerOrder.notifyManufacturingStarted();
        
        var completedEvent = ManufacturingOrderCompleted.of(manufacturingOrderId, Instant.now());
        
        when(customerOrderRepository.findByManufacturingOrderId(manufacturingOrderId)).thenReturn(List.of(customerOrder, secondCustomerOrder));
        
        // When
        eventHandler.handleManufacturingOrderCompleted(completedEvent);
        
        // Then
        verify(customerOrderRepository, times(1)).findByManufacturingOrderId(manufacturingOrderId);
        verify(customerOrderRepository, times(2)).save(argThat(order -> 
            order.getStatus() == CustomerOrderStatus.MANUFACTURING_COMPLETED
        ));
    }

    @Test
    void shouldHandleEventWhenCustomerOrderHasNoLinkedManufacturingOrder() {
        // Given
        var customerOrderWithoutLink = CustomerOrder.placeOrder(
            OrderId.of(UUID.randomUUID()),
            customerOrder.getCustomerInfo(),
            customerOrder.getItems().stream().toList()
        );
        customerOrderWithoutLink.confirm();
        // No manufacturing order linked
        
        var completedEvent = ManufacturingOrderCompleted.of(manufacturingOrderId, Instant.now());
        
        when(customerOrderRepository.findByManufacturingOrderId(manufacturingOrderId)).thenReturn(
            List.of(customerOrder)
        );
        
        // When
        eventHandler.handleManufacturingOrderCompleted(completedEvent);
        
        // Then
        verify(customerOrderRepository, times(1)).findByManufacturingOrderId(manufacturingOrderId);
        verify(customerOrderRepository, times(1)).save(argThat(order -> 
            order.getId().equals(customerOrderId) &&
            order.getStatus() == CustomerOrderStatus.MANUFACTURING_COMPLETED
        ));
    }

    @Test
    void shouldHandleEventWhenRepositoryIsEmpty() {
        // Given
        var completedEvent = ManufacturingOrderCompleted.of(manufacturingOrderId, Instant.now());
        
        when(customerOrderRepository.findByManufacturingOrderId(manufacturingOrderId)).thenReturn(List.of());
        
        // When
        eventHandler.handleManufacturingOrderCompleted(completedEvent);
        
        // Then
        verify(customerOrderRepository, times(1)).findByManufacturingOrderId(manufacturingOrderId);
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    void shouldHandleEventWhenCustomerOrderNotInCorrectStatus() {
        // Given
        var newCustomerOrder = CustomerOrder.placeOrder(customerOrderId, 
            customerOrder.getCustomerInfo(), customerOrder.getItems().stream().toList());
        // Order is in PLACED status, not MANUFACTURING_IN_PROGRESS
        newCustomerOrder.linkManufacturingOrder(manufacturingOrderId);
        
        var completedEvent = ManufacturingOrderCompleted.of(manufacturingOrderId, Instant.now());
        
        when(customerOrderRepository.findByManufacturingOrderId(manufacturingOrderId)).thenReturn(List.of(newCustomerOrder));
        
        // When
        eventHandler.handleManufacturingOrderCompleted(completedEvent);
        
        // Then
        verify(customerOrderRepository, times(1)).findByManufacturingOrderId(manufacturingOrderId);
        verify(customerOrderRepository, times(1)).save(any(CustomerOrder.class));
        
        // The handler should still call notifyManufacturingCompleted, but the domain logic
        // will determine if the status transition is valid
    }
}