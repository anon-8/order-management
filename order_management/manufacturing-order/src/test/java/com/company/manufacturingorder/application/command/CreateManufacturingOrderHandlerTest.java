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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Create Manufacturing Order Handler Tests")
class CreateManufacturingOrderHandlerTest {

    @Mock
    private ManufacturingOrderRepository repository;

    @Mock
    private ManufacturingOrderDomainService domainService;

    private CreateManufacturingOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateManufacturingOrderHandler(repository, domainService);
    }

    @Test
    @DisplayName("Should create manufacturing order successfully")
    void shouldCreateManufacturingOrderSuccessfully() {
        OrderId orderId = OrderId.generate();
        Instant expectedStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant expectedCompletion = expectedStart.plus(7, ChronoUnit.DAYS);
        
        CreateManufacturingOrderCommand command = new CreateManufacturingOrderCommand(
            orderId, "PROD-001", "Test Product", 10, "Standard specifications",
            expectedStart, expectedCompletion
        );

        OrderId result = handler.handle(command);

        assertNotNull(result);
        verify(repository).save(any(ManufacturingOrder.class));
    }

    @Test
    @DisplayName("Should handle repository save failure")
    void shouldHandleRepositorySaveFailure() {
        OrderId orderId = OrderId.generate();
        Instant expectedStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant expectedCompletion = expectedStart.plus(7, ChronoUnit.DAYS);
        
        CreateManufacturingOrderCommand command = new CreateManufacturingOrderCommand(
            orderId, "PROD-001", "Test Product", 10, "Standard specifications",
            expectedStart, expectedCompletion
        );

        doThrow(new RuntimeException("Database error")).when(repository).save(any());

        assertThrows(RuntimeException.class, () -> handler.handle(command));
    }

    @Test
    @DisplayName("Should create order with correct initial state")
    void shouldCreateOrderWithCorrectInitialState() {
        OrderId orderId = OrderId.generate();
        Instant expectedStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant expectedCompletion = expectedStart.plus(7, ChronoUnit.DAYS);
        
        CreateManufacturingOrderCommand command = new CreateManufacturingOrderCommand(
            orderId, "PROD-001", "Test Product", 10, "Standard specifications",
            expectedStart, expectedCompletion
        );

        handler.handle(command);

        verify(repository).save(argThat(order -> 
            order.getStatus() == OrderStatus.PENDING &&
            order.getProductSpecification().getProductCode().equals("PROD-001")
        ));
    }
}