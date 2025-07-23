package com.company.customerorder.domain.service;

import com.company.customerorder.domain.model.*;
import com.company.customerorder.domain.port.ManufacturingOrderQueryPort;
import com.company.sharedkernel.Money;
import com.company.sharedkernel.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Order Domain Service Tests")
class CustomerOrderDomainServiceTest {

    @Mock
    private ManufacturingOrderQueryPort manufacturingOrderQueryPort;

    private CustomerOrderDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new CustomerOrderDomainService(manufacturingOrderQueryPort);
    }

    @Test
    @DisplayName("Should validate order items successfully")
    void shouldValidateOrderItemsSuccessfully() {
        List<OrderItem> orderItems = createTestOrderItems();

        assertDoesNotThrow(() -> domainService.validateOrderItems(orderItems));
    }

    @Test
    @DisplayName("Should reject empty order items")
    void shouldRejectEmptyOrderItems() {
        List<OrderItem> emptyItems = List.of();

        assertThrows(IllegalArgumentException.class, () -> domainService.validateOrderItems(emptyItems));
    }

    @Test
    @DisplayName("Should reject null order items")
    void shouldRejectNullOrderItems() {
        assertThrows(IllegalArgumentException.class, () -> domainService.validateOrderItems(null));
    }


    @Test
    @DisplayName("Should calculate 10% discount for orders over 1000")
    void shouldCalculate10PercentDiscountForOrdersOver1000() {
        CustomerOrder order = createTestOrderWithAmount(new BigDecimal("1000.00"));

        Money discount = domainService.calculateOrderDiscount(order);

        assertEquals(0, new BigDecimal("100.00").compareTo(discount.getAmount()));
    }

    @Test
    @DisplayName("Should calculate 5% discount for orders over 500")
    void shouldCalculate5PercentDiscountForOrdersOver500() {
        CustomerOrder order = createTestOrderWithAmount(new BigDecimal("500.00"));

        Money discount = domainService.calculateOrderDiscount(order);

        assertEquals(0, new BigDecimal("25.00").compareTo(discount.getAmount()));
    }

    @Test
    @DisplayName("Should not apply discount for orders under 500")
    void shouldNotApplyDiscountForOrdersUnder500() {
        CustomerOrder order = createTestOrderWithAmount(new BigDecimal("400.00"));

        Money discount = domainService.calculateOrderDiscount(order);

        assertEquals(new BigDecimal("0.00"), discount.getAmount());
    }

    @Test
    @DisplayName("Should allow cancellation of placed orders")
    void shouldAllowCancellationOfPlacedOrders() {
        CustomerOrder order = createTestOrder(CustomerOrderStatus.PLACED);

        boolean canCancel = domainService.canCancelOrder(order);

        assertTrue(canCancel);
    }

    @Test
    @DisplayName("Should prevent cancellation of delivered orders")
    void shouldPreventCancellationOfDeliveredOrders() {
        CustomerOrder order = createTestOrder(CustomerOrderStatus.DELIVERED);

        boolean canCancel = domainService.canCancelOrder(order);

        assertFalse(canCancel);
    }

    @Test
    @DisplayName("Should prevent cancellation of already cancelled orders")
    void shouldPreventCancellationOfAlreadyCancelledOrders() {
        CustomerOrder order = createTestOrder(CustomerOrderStatus.CANCELLED);

        boolean canCancel = domainService.canCancelOrder(order);

        assertFalse(canCancel);
    }

    @Test
    @DisplayName("Should prevent cancellation when manufacturing is in progress")
    void shouldPreventCancellationWhenManufacturingIsInProgress() {
        CustomerOrder order = createTestOrderWithLinkedManufacturing();
        OrderId manufacturingOrderId = order.getManufacturingOrderId();
        
        when(manufacturingOrderQueryPort.getOrderStatus(manufacturingOrderId))
            .thenReturn(Optional.of(new ManufacturingOrderQueryPort.ManufacturingOrderStatus(
                manufacturingOrderId, "IN_PROGRESS", "PROD-001", 1)));

        boolean canCancel = domainService.canCancelOrder(order);

        assertFalse(canCancel);
    }

    @Test
    @DisplayName("Should allow cancellation when manufacturing is pending")
    void shouldAllowCancellationWhenManufacturingIsPending() {
        CustomerOrder order = createTestOrderWithLinkedManufacturing();
        OrderId manufacturingOrderId = order.getManufacturingOrderId();
        
        when(manufacturingOrderQueryPort.getOrderStatus(manufacturingOrderId))
            .thenReturn(Optional.of(new ManufacturingOrderQueryPort.ManufacturingOrderStatus(
                manufacturingOrderId, "PENDING", "PROD-001", 1)));

        boolean canCancel = domainService.canCancelOrder(order);

        assertTrue(canCancel);
    }


    private CustomerInfo createTestCustomerInfo() {
        return CustomerInfo.of(
            CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St, City, State"
        );
    }

    private List<OrderItem> createTestOrderItems() {
        return List.of(
            OrderItem.of("PROD-001", "Product 1", 1, Money.usd(new BigDecimal("50.00"))),
            OrderItem.of("PROD-002", "Product 2", 2, Money.usd(new BigDecimal("30.00")))
        );
    }

    private CustomerOrder createTestOrder(CustomerOrderStatus status) {
        OrderId orderId = OrderId.generate();
        CustomerInfo customerInfo = createTestCustomerInfo();
        List<OrderItem> orderItems = createTestOrderItems();

        CustomerOrder order = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        
        switch (status) {
            case CONFIRMED -> order.confirm();
            case MANUFACTURING_IN_PROGRESS -> {
                order.confirm();
                order.linkManufacturingOrder(OrderId.generate());
                order.notifyManufacturingStarted();
            }
            case MANUFACTURING_COMPLETED -> {
                order.confirm();
                order.linkManufacturingOrder(OrderId.generate());
                order.notifyManufacturingStarted();
                order.notifyManufacturingCompleted();
            }
            case SHIPPED -> {
                order.confirm();
                order.linkManufacturingOrder(OrderId.generate());
                order.notifyManufacturingStarted();
                order.notifyManufacturingCompleted();
                order.markAsShipped();
            }
            case DELIVERED -> {
                order.confirm();
                order.linkManufacturingOrder(OrderId.generate());
                order.notifyManufacturingStarted();
                order.notifyManufacturingCompleted();
                order.markAsShipped();
                order.markAsDelivered();
            }
            case CANCELLED -> order.cancel("Test cancellation");
        }
        
        return order;
    }

    private CustomerOrder createTestOrderWithAmount(BigDecimal amount) {
        OrderId orderId = OrderId.generate();
        CustomerInfo customerInfo = createTestCustomerInfo();
        List<OrderItem> orderItems = List.of(
            OrderItem.of("PROD-001", "High Value Product", 1, Money.usd(amount))
        );

        return CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
    }

    private CustomerOrder createTestOrderWithLinkedManufacturing() {
        CustomerOrder order = createTestOrder(CustomerOrderStatus.PLACED);
        order.confirm();
        order.linkManufacturingOrder(OrderId.generate());
        return order;
    }
}