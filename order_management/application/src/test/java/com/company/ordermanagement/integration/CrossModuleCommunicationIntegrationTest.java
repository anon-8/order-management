package com.company.ordermanagement.integration;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderCommand;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderHandler;
import com.company.manufacturingorder.application.command.CompleteManufacturingOrderHandler;
import com.company.manufacturingorder.application.command.CompleteManufacturingOrderCommand;
import com.company.manufacturingorder.application.command.ChangeOrderStatusCommand;
import com.company.manufacturingorder.application.command.ChangeOrderStatusHandler;
import com.company.manufacturingorder.domain.model.ProductSpecification;
import com.company.manufacturingorder.domain.model.Timeline;
import com.company.manufacturingorder.domain.model.OrderStatus;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.customerorder.application.command.PlaceCustomerOrderCommand;
import com.company.customerorder.application.command.PlaceCustomerOrderHandler;
import com.company.customerorder.domain.model.CustomerInfo;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.OrderItem;
import com.company.customerorder.domain.model.CustomerOrderStatus;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestEventConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "order.management.auto-create-manufacturing-orders=false",
    "spring.main.allow-bean-definition-overriding=true"
})
class CrossModuleCommunicationIntegrationTest {

    @Autowired
    private CreateManufacturingOrderHandler createManufacturingOrderHandler;

    @Autowired
    private CompleteManufacturingOrderHandler completeManufacturingOrderHandler;

    @Autowired
    private ChangeOrderStatusHandler changeOrderStatusHandler;

    @Autowired
    private PlaceCustomerOrderHandler placeCustomerOrderHandler;

    @Autowired
    private ManufacturingOrderRepository manufacturingOrderRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    private OrderId manufacturingOrderId;
    private OrderId customerOrderId;

    @BeforeEach
    void setUp() {
        manufacturingOrderId = OrderId.of(UUID.randomUUID());
        customerOrderId = OrderId.of(UUID.randomUUID());
    }
    
    @AfterEach 
    void tearDown() {
        try {
            customerOrderRepository.findAll().forEach(order -> 
                customerOrderRepository.delete(order.getId()));
            manufacturingOrderRepository.findAll().forEach(order ->
                manufacturingOrderRepository.delete(order.getId()));
        } catch (Exception e) {
        }
    }
    
    private PlaceCustomerOrderCommand createPlaceCustomerOrderCommand(OrderId orderId, CustomerInfo customerInfo, List<OrderItem> orderItems) {
        var orderItemCommands = orderItems.stream()
            .map(item -> new PlaceCustomerOrderCommand.OrderItemCommand(
                item.getProductCode(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice().getAmount(),
                item.getUnitPrice().getCurrency()
            ))
            .toList();
            
        return new PlaceCustomerOrderCommand(
            orderId,
            customerInfo.getCustomerId(),
            customerInfo.getName(),
            customerInfo.getEmail(),
            customerInfo.getAddress(),
            orderItemCommands
        );
    }



    @Test
    void shouldNotAffectUnlinkedCustomerOrders() {
        // Given - Create manufacturing order
        var productSpec = ProductSpecification.of("PROD-UNLINKED", "Unlinked Product", 10, "Unlinked specs");
        var timeline = Timeline.create(
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );
        
        createManufacturingOrderHandler.handle(
            new CreateManufacturingOrderCommand(
                manufacturingOrderId,
                productSpec.getProductCode(),
                productSpec.getDescription(),
                productSpec.getQuantity(),
                productSpec.getSpecifications(),
                timeline.getExpectedStartDate(),
                timeline.getExpectedCompletionDate()
            )
        );

        // And - Create customer order that is NOT linked to manufacturing order
        var unlinkedCustomerId = OrderId.of(UUID.randomUUID());
        var customerInfo = CustomerInfo.of(
            CustomerId.of(UUID.randomUUID()),
            "Unlinked Customer",
            "unlinked@test.com",
            "999 Unlinked Ave"
        );
        
        var orderItems = List.of(
            OrderItem.of("PROD-OTHER", "Other Product", 3,
                Money.of(new BigDecimal("75.00"), Currency.getInstance("USD")))
        );
        
        var placedUnlinkedId = placeCustomerOrderHandler.handle(
            createPlaceCustomerOrderCommand(unlinkedCustomerId, customerInfo, orderItems)
        );
        
        var unlinkedOrder = customerOrderRepository.findById(placedUnlinkedId).orElseThrow();
        unlinkedOrder.confirm();
        customerOrderRepository.save(unlinkedOrder); // No manufacturing order linked

        // When - Start and complete manufacturing order
        // First transition to IN_PROGRESS
        changeOrderStatusHandler.handle(new ChangeOrderStatusCommand(manufacturingOrderId, OrderStatus.IN_PROGRESS));
        
        // Then complete the order
        completeManufacturingOrderHandler.handle(
            new CompleteManufacturingOrderCommand(manufacturingOrderId)
        );

        // Then - Unlinked customer order should remain unchanged
        var unchangedOrder = customerOrderRepository.findById(placedUnlinkedId).orElseThrow();
        assertThat(unchangedOrder.getStatus()).isEqualTo(CustomerOrderStatus.CONFIRMED);
        assertThat(unchangedOrder.getManufacturingOrderId()).isNull();
    }

}