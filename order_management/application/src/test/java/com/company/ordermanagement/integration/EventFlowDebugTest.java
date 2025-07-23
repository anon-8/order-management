package com.company.ordermanagement.integration;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderCommand;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderHandler;
import com.company.manufacturingorder.application.command.CompleteManufacturingOrderCommand;
import com.company.manufacturingorder.application.command.CompleteManufacturingOrderHandler;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerInfo;
import com.company.customerorder.domain.model.OrderItem;
import com.company.customerorder.domain.model.CustomerOrder;
import com.company.sharedkernel.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

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
    "spring.datasource.url=jdbc:h2:mem:debugdb", 
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "order.management.auto-create-manufacturing-orders=false",
    "spring.main.allow-bean-definition-overriding=true"
})
class EventFlowDebugTest {

    @Autowired
    private CreateManufacturingOrderHandler createManufacturingOrderHandler;
    
    @Autowired
    private CompleteManufacturingOrderHandler completeManufacturingOrderHandler;

    @Autowired
    private ManufacturingOrderRepository manufacturingOrderRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    void debugEventFlow() {
        System.out.println("=== DEBUG: Starting event flow test ===");
        
        // Step 1: Create a customer order and manufacturing order with SAME ID for correlation
        var orderId = OrderId.of(UUID.randomUUID());
        System.out.println("=== DEBUG: Using order ID: " + orderId.getValue() + " ===");
        
        // Create customer order first
        var customerInfo = CustomerInfo.of(
            CustomerId.of(UUID.randomUUID()),
            "Debug Customer",
            "debug@test.com",
            "123 Debug St"
        );
        
        var orderItems = List.of(
            OrderItem.of("DEBUG-PROD", "Debug Product", 1,
                Money.of(new BigDecimal("100.00"), Currency.getInstance("USD")))
        );
        
        var customerOrder = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        customerOrder.confirm(); // Move to CONFIRMED status
        customerOrder.linkManufacturingOrder(orderId); // Link to manufacturing order (same ID)
        customerOrder.notifyManufacturingStarted(); // Start manufacturing to transition to IN_PROGRESS
        
        System.out.println("=== DEBUG: Customer order status before save: " + customerOrder.getStatus() + " ===");
        System.out.println("=== DEBUG: Customer order manufacturing ID: " + customerOrder.getManufacturingOrderId() + " ===");
        
        customerOrderRepository.save(customerOrder);
        
        // Create manufacturing order with same ID
        var createCommand = new CreateManufacturingOrderCommand(
            orderId,
            "DEBUG-PROD",
            "Debug Manufacturing Product", 
            1,
            "Debug specifications",
            Instant.now().plus(1, ChronoUnit.HOURS),
            Instant.now().plus(1, ChronoUnit.DAYS)
        );
        
        System.out.println("=== DEBUG: Creating manufacturing order ===");
        createManufacturingOrderHandler.handle(createCommand);
        
        // Verify manufacturing order was created
        var manufacturingOrder = manufacturingOrderRepository.findById(orderId).orElseThrow();
        System.out.println("=== DEBUG: Manufacturing order status: " + manufacturingOrder.getStatus() + " ===");
        
        // Step 2: Complete the manufacturing order - this should trigger the event
        System.out.println("=== DEBUG: Completing manufacturing order ===");
        var completeCommand = new CompleteManufacturingOrderCommand(orderId);
        completeManufacturingOrderHandler.handle(completeCommand);
        
        // Step 3: Check if manufacturing order was completed
        var completedManufacturingOrder = manufacturingOrderRepository.findById(orderId).orElseThrow();
        System.out.println("=== DEBUG: Manufacturing order status after completion: " + completedManufacturingOrder.getStatus() + " ===");
        assertThat(completedManufacturingOrder.isCompleted()).isTrue();
        
        // Step 4: Wait for async event processing with proper await
        System.out.println("=== DEBUG: Waiting for async event processing ===");
        
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                var updatedCustomerOrder = customerOrderRepository.findById(orderId).orElseThrow();
                System.out.println("=== DEBUG: Checking customer order status: " + updatedCustomerOrder.getStatus() + " ===");
                assertThat(updatedCustomerOrder.getStatus().name()).isEqualTo("MANUFACTURING_COMPLETED");
            });
        
        System.out.println("=== DEBUG: Event processing completed successfully ===");
        
        // Final verification - check if the repository can find the customer order by manufacturing order ID
        var foundOrders = customerOrderRepository.findByManufacturingOrderId(orderId);
        System.out.println("=== DEBUG: Found " + foundOrders.size() + " customer orders by manufacturing order ID ===");
        foundOrders.forEach(order -> {
            System.out.println("=== DEBUG: Found customer order ID: " + order.getId().getValue() + ", status: " + order.getStatus() + " ===");
        });
        
        System.out.println("=== DEBUG: Test completed successfully ===");
    }
}