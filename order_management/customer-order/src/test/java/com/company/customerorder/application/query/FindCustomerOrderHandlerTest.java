package com.company.customerorder.application.query;

import com.company.customerorder.domain.model.*;
import com.company.customerorder.domain.port.CustomerOrderRepository;
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
@DisplayName("Find Customer Order Handler Tests")
class FindCustomerOrderHandlerTest {

    @Mock
    private CustomerOrderRepository repository;

    private FindCustomerOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FindCustomerOrderHandler(repository);
    }

    @Test
    @DisplayName("Should return order DTO when order exists")
    void shouldReturnOrderDtoWhenOrderExists() {
        OrderId orderId = OrderId.generate();
        CustomerOrder order = createTestOrder(orderId);
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<CustomerOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        CustomerOrderDto dto = result.get();
        assertEquals(orderId, dto.orderId());
        assertEquals(CustomerOrderStatus.PLACED, dto.status());
        assertEquals("John Doe", dto.customerName());
        assertEquals(new BigDecimal("110.00"), dto.totalAmount().getAmount());
    }

    @Test
    @DisplayName("Should return empty when order does not exist")
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        OrderId orderId = OrderId.generate();
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.empty());

        Optional<CustomerOrderDto> result = handler.handle(query);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should map all order properties correctly")
    void shouldMapAllOrderPropertiesCorrectly() {
        OrderId orderId = OrderId.generate();
        CustomerOrder order = createTestOrder(orderId);
        order.confirm();
        
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<CustomerOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        CustomerOrderDto dto = result.get();
        assertEquals(orderId, dto.orderId());
        assertEquals(CustomerOrderStatus.CONFIRMED, dto.status());
        assertEquals("John Doe", dto.customerName());
        assertEquals("john.doe@example.com", dto.customerEmail());
        assertEquals(2, dto.items().size());
        assertNotNull(dto.placedAt());
    }

    @Test
    @DisplayName("Should include manufacturing order ID when linked")
    void shouldIncludeManufacturingOrderIdWhenLinked() {
        OrderId orderId = OrderId.generate();
        OrderId manufacturingOrderId = OrderId.generate();
        CustomerOrder order = createTestOrder(orderId);
        order.confirm();
        order.linkManufacturingOrder(manufacturingOrderId);
        order.notifyManufacturingStarted();
        
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<CustomerOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        CustomerOrderDto dto = result.get();
        assertEquals(manufacturingOrderId, dto.manufacturingOrderId());
        assertEquals(CustomerOrderStatus.MANUFACTURING_IN_PROGRESS, dto.status());
    }

    @Test
    @DisplayName("Should map order items correctly")
    void shouldMapOrderItemsCorrectly() {
        OrderId orderId = OrderId.generate();
        CustomerOrder order = createTestOrder(orderId);
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<CustomerOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        CustomerOrderDto dto = result.get();
        assertEquals(2, dto.items().size());
        
        var firstItem = dto.items().get(0);
        assertEquals("PROD-001", firstItem.productCode());
        assertEquals("Product 1", firstItem.description());
        assertEquals(1, firstItem.quantity());
        assertEquals(new BigDecimal("50.00"), firstItem.unitPrice().getAmount());
    }

    @Test
    @DisplayName("Should handle delivered orders")
    void shouldHandleDeliveredOrders() {
        OrderId orderId = OrderId.generate();
        CustomerOrder order = createTestOrder(orderId);
        order.confirm();
        order.linkManufacturingOrder(OrderId.generate());
        order.notifyManufacturingStarted();
        order.notifyManufacturingCompleted();
        order.markAsShipped();
        order.markAsDelivered();
        
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        Optional<CustomerOrderDto> result = handler.handle(query);

        assertTrue(result.isPresent());
        CustomerOrderDto dto = result.get();
        assertEquals(CustomerOrderStatus.DELIVERED, dto.status());
    }

    @Test
    @DisplayName("Should handle repository exceptions")
    void shouldHandleRepositoryExceptions() {
        OrderId orderId = OrderId.generate();
        FindCustomerOrderQuery query = new FindCustomerOrderQuery(orderId);

        when(repository.findById(orderId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> handler.handle(query));
    }

    private CustomerOrder createTestOrder(OrderId orderId) {
        CustomerInfo customerInfo = CustomerInfo.of(
            CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St, City, State"
        );
        
        List<OrderItem> orderItems = List.of(
            OrderItem.of("PROD-001", "Product 1", 1, Money.usd(new BigDecimal("50.00"))),
            OrderItem.of("PROD-002", "Product 2", 2, Money.usd(new BigDecimal("30.00")))
        );

        return CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
    }
}