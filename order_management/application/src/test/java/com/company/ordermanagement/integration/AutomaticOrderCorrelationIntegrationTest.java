package com.company.ordermanagement.integration;

import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.customerorder.application.command.PlaceCustomerOrderCommand;
import com.company.customerorder.application.command.PlaceCustomerOrderHandler;
import com.company.customerorder.application.command.CancelCustomerOrderCommand;
import com.company.customerorder.application.command.CancelCustomerOrderHandler;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerOrderStatus;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.manufacturingorder.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.main.allow-bean-definition-overriding=true"
})
class AutomaticOrderCorrelationIntegrationTest {

    @Autowired
    private PlaceCustomerOrderHandler placeCustomerOrderHandler;
    
    @Autowired
    private CancelCustomerOrderHandler cancelCustomerOrderHandler;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private ManufacturingOrderRepository manufacturingOrderRepository;

    private OrderId testOrderId;
    private CustomerId testCustomerId;

    @BeforeEach
    void setUp() {
        testOrderId = OrderId.of(UUID.randomUUID());
        testCustomerId = CustomerId.of(UUID.randomUUID());
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

    @Test
    void shouldAutomaticallyCreateManufacturingOrderWhenCustomerOrderConfirmed() {
        // Given - Customer order placement command
        var orderItems = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "WIDGET-AUTO",
                "Automatically correlated widget",
                5,
                new BigDecimal("150.00"),
                Currency.getInstance("USD")
            )
        );
        
        var placeOrderCommand = new PlaceCustomerOrderCommand(
            testOrderId,
            testCustomerId,
            "Auto Correlation Customer",
            "auto@correlation.test",
            "123 Auto Correlation St",
            orderItems
        );

        // When - Customer order is placed and confirmed
        var placedOrderId = placeCustomerOrderHandler.handle(placeOrderCommand);
        
        var customerOrder = customerOrderRepository.findById(placedOrderId).orElseThrow();
        customerOrder.confirm();
        customerOrderRepository.save(customerOrder);

        // Then - Manufacturing order should be automatically created when confirmed
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                var updatedCustomerOrder = customerOrderRepository.findById(placedOrderId).orElseThrow();
                assertThat(updatedCustomerOrder.getId()).isEqualTo(testOrderId);
                assertThat(updatedCustomerOrder.getStatus()).isEqualTo(CustomerOrderStatus.CONFIRMED);
                
                var manufacturingOrder = manufacturingOrderRepository.findById(testOrderId).orElseThrow();
                assertThat(manufacturingOrder.getId()).isEqualTo(testOrderId);
                assertThat(manufacturingOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
                
                assertThat(updatedCustomerOrder.getManufacturingOrderId()).isEqualTo(testOrderId);
            });
    }

    @Test
    void shouldAutomaticallyCancelManufacturingOrderWhenCustomerOrderCancelled() {
        // Given - Customer order with linked manufacturing order
        var orderItems = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "WIDGET-CANCEL",
                "Widget to be cancelled",
                3,
                new BigDecimal("100.00"),
                Currency.getInstance("USD")
            )
        );
        
        var placeOrderCommand = new PlaceCustomerOrderCommand(
            testOrderId,
            testCustomerId,
            "Cancellation Test Customer", 
            "cancel@test.com",
            "456 Cancel St",
            orderItems
        );

        var placedOrderId = placeCustomerOrderHandler.handle(placeOrderCommand);
        
        var customerOrderToConfirm = customerOrderRepository.findById(placedOrderId).orElseThrow();
        customerOrderToConfirm.confirm();
        customerOrderRepository.save(customerOrderToConfirm);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(manufacturingOrderRepository.existsById(testOrderId)).isTrue();
                var updatedCustomerOrder = customerOrderRepository.findById(placedOrderId).orElseThrow();
                assertThat(updatedCustomerOrder.getManufacturingOrderId()).isEqualTo(testOrderId);
            });

        // When - Customer order is cancelled
        var cancelCommand = new CancelCustomerOrderCommand(
            testOrderId,
            "Customer requested cancellation for testing"
        );
        
        cancelCustomerOrderHandler.handle(cancelCommand);

        // Then - Manufacturing order should be automatically cancelled
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                var manufacturingOrder = manufacturingOrderRepository.findById(testOrderId).orElseThrow();
                assertThat(manufacturingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                
                var customerOrder = customerOrderRepository.findById(placedOrderId).orElseThrow();
                assertThat(customerOrder.isCancelled()).isTrue();
            });
    }

    @Test
    void shouldHandleMultipleCustomerOrdersWithSeparateManufacturingOrders() {
        // Given - Multiple customer orders
        var order1Id = OrderId.of(UUID.randomUUID());
        var order2Id = OrderId.of(UUID.randomUUID());
        
        var customer1Id = CustomerId.of(UUID.randomUUID());
        var customer2Id = CustomerId.of(UUID.randomUUID());
        
        var orderItems1 = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "WIDGET-MULTI-1", "Multi test widget 1", 2,
                new BigDecimal("75.00"), Currency.getInstance("USD")
            )
        );
        
        var orderItems2 = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "WIDGET-MULTI-2", "Multi test widget 2", 4,
                new BigDecimal("200.00"), Currency.getInstance("USD")
            )
        );

        var command1 = new PlaceCustomerOrderCommand(
            order1Id, customer1Id, "Customer One", "one@multi.test",
            "111 Multi St", orderItems1
        );
        
        var command2 = new PlaceCustomerOrderCommand(
            order2Id, customer2Id, "Customer Two", "two@multi.test", 
            "222 Multi Ave", orderItems2
        );

        // When - Both orders are placed and confirmed
        placeCustomerOrderHandler.handle(command1);
        placeCustomerOrderHandler.handle(command2);
        
        var customerOrder1ToConfirm = customerOrderRepository.findById(order1Id).orElseThrow();
        var customerOrder2ToConfirm = customerOrderRepository.findById(order2Id).orElseThrow();
        customerOrder1ToConfirm.confirm();
        customerOrder2ToConfirm.confirm();
        customerOrderRepository.save(customerOrder1ToConfirm);
        customerOrderRepository.save(customerOrder2ToConfirm);

        // Then - Both should have separate manufacturing orders
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                var customerOrder1 = customerOrderRepository.findById(order1Id).orElseThrow();
                var manufacturingOrder1 = manufacturingOrderRepository.findById(order1Id).orElseThrow();
                assertThat(customerOrder1.getManufacturingOrderId()).isEqualTo(order1Id);
                assertThat(manufacturingOrder1.getId()).isEqualTo(order1Id);
                
                var customerOrder2 = customerOrderRepository.findById(order2Id).orElseThrow();
                var manufacturingOrder2 = manufacturingOrderRepository.findById(order2Id).orElseThrow();
                assertThat(customerOrder2.getManufacturingOrderId()).isEqualTo(order2Id);
                assertThat(manufacturingOrder2.getId()).isEqualTo(order2Id);
                
                assertThat(order1Id).isNotEqualTo(order2Id);
                assertThat(manufacturingOrder1.getId()).isNotEqualTo(manufacturingOrder2.getId());
            });
    }

    @Test
    void shouldNotCreateDuplicateManufacturingOrderForSameCustomerOrder() {
        // Given - Customer order placement command
        var orderItems = List.of(
            new PlaceCustomerOrderCommand.OrderItemCommand(
                "WIDGET-DUPLICATE",
                "Duplicate prevention test widget",
                1,
                new BigDecimal("50.00"),
                Currency.getInstance("USD")
            )
        );
        
        var placeOrderCommand = new PlaceCustomerOrderCommand(
            testOrderId,
            testCustomerId,
            "Duplicate Prevention Customer",
            "duplicate@prevention.test",
            "789 Duplicate Ave",
            orderItems
        );

        // When - Customer order is placed and confirmed
        placeCustomerOrderHandler.handle(placeOrderCommand);
        
        var customerOrder = customerOrderRepository.findById(testOrderId).orElseThrow();
        customerOrder.confirm();
        customerOrderRepository.save(customerOrder);
        
        await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(manufacturingOrderRepository.existsById(testOrderId)).isTrue();
            });
            
        placeCustomerOrderHandler.handle(placeOrderCommand);

        // Then - Only one manufacturing order should exist
        await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                var allManufacturingOrders = manufacturingOrderRepository.findAll();
                var ordersWithTestId = allManufacturingOrders.stream()
                    .filter(order -> order.getId().equals(testOrderId))
                    .toList();
                    
                assertThat(ordersWithTestId).hasSize(1);
            });
    }
}