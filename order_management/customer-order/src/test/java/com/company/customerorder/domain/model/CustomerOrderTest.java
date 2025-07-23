package com.company.customerorder.domain.model;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.sharedkernel.events.CustomerOrderPlaced;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer Order Aggregate Tests")
class CustomerOrderTest {
    
    private OrderId orderId;
    private CustomerInfo customerInfo;
    private List<OrderItem> orderItems;
    private final Currency USD = Currency.getInstance("USD");
    
    @BeforeEach
    void setUp() {
        orderId = OrderId.generate();
        customerInfo = CustomerInfo.of(
            CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St, City, State"
        );
        orderItems = List.of(
            OrderItem.of("PROD-001", "Product 1", 2, Money.usd(new BigDecimal("50.00"))),
            OrderItem.of("PROD-002", "Product 2", 1, Money.usd(new BigDecimal("100.00")))
        );
    }
    
    @Test
    @DisplayName("Should place order with correct total amount")
    void shouldPlaceOrderWithCorrectTotalAmount() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        
        assertEquals(orderId, order.getId());
        assertEquals(CustomerOrderStatus.PLACED, order.getStatus());
        assertEquals(customerInfo, order.getCustomerInfo());
        assertEquals(2, order.getItems().size());

        assertEquals(new BigDecimal("200.00"), order.getTotalAmount().getAmount());
        assertEquals(USD, order.getTotalAmount().getCurrency());
        

        assertEquals(1, order.getDomainEvents().size());
        assertInstanceOf(CustomerOrderPlaced.class, order.getDomainEvents().getFirst());
    }
    
    @Test
    @DisplayName("Should confirm order")
    void shouldConfirmOrder() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        order.clearDomainEvents();
        
        order.confirm();
        
        assertEquals(CustomerOrderStatus.CONFIRMED, order.getStatus());
    }
    
    @Test
    @DisplayName("Should update status to manufacturing in progress when manufacturing starts")
    void shouldUpdateStatusToManufacturingInProgressWhenLinked() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        order.confirm();
        order.clearDomainEvents();
        
        OrderId manufacturingOrderId = OrderId.generate();
        order.linkManufacturingOrder(manufacturingOrderId);
        order.notifyManufacturingStarted();
        
        assertEquals(CustomerOrderStatus.MANUFACTURING_IN_PROGRESS, order.getStatus());
        assertEquals(manufacturingOrderId, order.getManufacturingOrderId());
    }
    
    @Test
    @DisplayName("Should handle manufacturing completion notification")
    void shouldHandleManufacturingCompletionNotification() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        order.confirm();
        order.linkManufacturingOrder(OrderId.generate());
        order.notifyManufacturingStarted();
        order.clearDomainEvents();
        
        order.notifyManufacturingCompleted();
        
        assertEquals(CustomerOrderStatus.MANUFACTURING_COMPLETED, order.getStatus());
    }
    
    @Test
    @DisplayName("Should mark order as shipped and delivered")
    void shouldMarkOrderAsShippedAndDelivered() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        order.confirm();
        order.linkManufacturingOrder(OrderId.generate());
        order.notifyManufacturingStarted();
        order.notifyManufacturingCompleted();
        
        order.markAsShipped();
        assertEquals(CustomerOrderStatus.SHIPPED, order.getStatus());
        
        order.markAsDelivered();
        assertEquals(CustomerOrderStatus.DELIVERED, order.getStatus());
        assertTrue(order.isDelivered());
        assertFalse(order.isActive());
    }
    
    @Test
    @DisplayName("Should cancel order with reason")
    void shouldCancelOrderWithReason() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        order.clearDomainEvents();
        
        order.cancel("Customer requested cancellation");
        
        assertEquals(CustomerOrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.isCancelled());
        assertFalse(order.isActive());
    }
    
    @Test
    @DisplayName("Should throw exception when placing order with empty items")
    void shouldThrowExceptionWhenPlacingOrderWithEmptyItems() {
        assertThrows(IllegalArgumentException.class, 
            () -> CustomerOrder.placeOrder(orderId, customerInfo, List.of()));
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling terminal order")
    void shouldThrowExceptionWhenCancellingTerminalOrder() {
        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        order.cancel("Test cancellation");
        
        assertThrows(IllegalStateException.class, 
            () -> order.cancel("Another cancellation"));
    }
}