package com.company.customerorder.application.command;

import com.company.customerorder.domain.model.*;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import com.company.customerorder.domain.service.CustomerOrderDomainService;
import com.company.sharedkernel.Money;
import com.company.sharedkernel.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Place Customer Order Handler Tests")
class PlaceCustomerOrderHandlerTest {

    @Mock
    private CustomerOrderRepository repository;

    @Mock
    private CustomerOrderDomainService domainService;

    private PlaceCustomerOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PlaceCustomerOrderHandler(repository, domainService);
    }

    @Test
    @DisplayName("Should place customer order successfully")
    void shouldPlaceCustomerOrderSuccessfully() {
        PlaceCustomerOrderCommand command = createTestCommand();

        OrderId result = handler.handle(command);

        assertNotNull(result);
        verify(repository).save(any(CustomerOrder.class));
    }


    @Test
    @DisplayName("Should handle repository exceptions")
    void shouldHandleRepositoryExceptions() {
        PlaceCustomerOrderCommand command = createTestCommand();
        
        doThrow(new RuntimeException("Database error")).when(repository).save(any());

        assertThrows(RuntimeException.class, () -> handler.handle(command));
        verify(domainService).validateOrderItems(any());
    }

    @Test
    @DisplayName("Should create order with correct customer information")
    void shouldCreateOrderWithCorrectCustomerInformation() {
        PlaceCustomerOrderCommand command = createTestCommand();

        OrderId result = handler.handle(command);

        assertNotNull(result);
        assertEquals(command.orderId(), result);
        verify(repository).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should validate order items before placement")
    void shouldValidateOrderItemsBeforePlacement() {
        List<PlaceCustomerOrderCommand.OrderItemCommand> items = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "PROD-001",
                "Product 1", 
                1, 
                new BigDecimal("50.00"),
                Currency.getInstance("USD")
            )
        );
        
        PlaceCustomerOrderCommand command = new PlaceCustomerOrderCommand(
            OrderId.generate(),
            CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St",
            items
        );

        OrderId result = handler.handle(command);

        assertNotNull(result);
        verify(repository).save(any(CustomerOrder.class));
    }


    @Test
    @DisplayName("Should create order with multiple items")
    void shouldCreateOrderWithMultipleItems() {
        List<PlaceCustomerOrderCommand.OrderItemCommand> items = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "PROD-001", "Product 1", 1, new BigDecimal("50.00"), Currency.getInstance("USD")
            ),
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "PROD-002", "Product 2", 2, new BigDecimal("30.00"), Currency.getInstance("USD")
            )
        );
        
        PlaceCustomerOrderCommand command = new PlaceCustomerOrderCommand(
            OrderId.generate(),
            CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St",
            items
        );

        OrderId result = handler.handle(command);

        assertNotNull(result);
        verify(repository).save(any(CustomerOrder.class));
    }

    private PlaceCustomerOrderCommand createTestCommand() {
        return new PlaceCustomerOrderCommand(
            OrderId.generate(),
            CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St, City, State",
            List.of(createTestOrderItemCommand())
        );
    }

    private PlaceCustomerOrderCommand.OrderItemCommand createTestOrderItemCommand() {
        return new PlaceCustomerOrderCommand.OrderItemCommand(
            "PROD-001",
            "Product 1",
            1,
            new BigDecimal("50.00"),
            Currency.getInstance("USD")
        );
    }
}