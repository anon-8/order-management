package com.company.ordermanagement.integration;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderCommand;
import com.company.manufacturingorder.application.command.CreateManufacturingOrderHandler;
import com.company.manufacturingorder.application.command.CompleteManufacturingOrderHandler;
import com.company.manufacturingorder.application.command.CompleteManufacturingOrderCommand;
import com.company.manufacturingorder.domain.port.ManufacturingOrderRepository;
import com.company.customerorder.domain.port.CustomerOrderRepository;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerInfo;
import com.company.customerorder.domain.model.OrderItem;
import com.company.customerorder.domain.model.CustomerOrder;
import com.company.customerorder.domain.model.CustomerOrderStatus;
import com.company.sharedkernel.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

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
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:simpletest",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "order.management.auto-create-manufacturing-orders=false",
    "spring.main.allow-bean-definition-overriding=true"
})
class SimpleEventFlowTest {

    @Autowired
    private CreateManufacturingOrderHandler createManufacturingOrderHandler;
    
    @Autowired
    private CompleteManufacturingOrderHandler completeManufacturingOrderHandler;

    @Autowired
    private ManufacturingOrderRepository manufacturingOrderRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    void simpleEventFlowTest() {
        System.out.println("=== SIMPLE TEST: Testing basic event flow ===");
        
        // Use same ID for both orders to ensure correlation
        var orderId = OrderId.of(UUID.randomUUID());
        
        // Create customer order
        var customerInfo = CustomerInfo.of(
            CustomerId.of(UUID.randomUUID()),
            "Simple Test Customer",
            "simple@test.com",
            "123 Simple St"
        );
        
        var orderItems = List.of(
            OrderItem.of("SIMPLE-PROD", "Simple Product", 1,
                Money.of(new BigDecimal("100.00"), Currency.getInstance("USD")))
        );
        
        var customerOrder = CustomerOrder.placeOrder(orderId, customerInfo, orderItems);
        customerOrder.confirm(); // Move to CONFIRMED status
        customerOrder.linkManufacturingOrder(orderId); // Link to manufacturing order (same ID)
        customerOrder.notifyManufacturingStarted(); // Start manufacturing to transition to IN_PROGRESS
        customerOrderRepository.save(customerOrder);
        
        // Verify customer order status
        var savedCustomerOrder = customerOrderRepository.findById(orderId).orElseThrow();
        assertThat(savedCustomerOrder.getStatus()).isEqualTo(CustomerOrderStatus.MANUFACTURING_IN_PROGRESS);
        
        // Create manufacturing order
        var createCommand = new CreateManufacturingOrderCommand(
            orderId,
            "SIMPLE-PROD",
            "Simple Manufacturing Product", 
            1,
            "Simple specifications",
            Instant.now().plus(1, ChronoUnit.HOURS),
            Instant.now().plus(1, ChronoUnit.DAYS)
        );
        
        createManufacturingOrderHandler.handle(createCommand);
        
        // Complete the manufacturing order - this should trigger the event
        var completeCommand = new CompleteManufacturingOrderCommand(orderId);
        completeManufacturingOrderHandler.handle(completeCommand);
        
        // Verify manufacturing order was completed
        var manufacturingOrder = manufacturingOrderRepository.findById(orderId).orElseThrow();
        assertThat(manufacturingOrder.isCompleted()).isTrue();
        
        // Wait for async event processing to update customer order
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                var updatedCustomerOrder = customerOrderRepository.findById(orderId).orElseThrow();
                System.out.println("=== Customer order status: " + updatedCustomerOrder.getStatus() + " ===");
                assertThat(updatedCustomerOrder.getStatus()).isEqualTo(CustomerOrderStatus.MANUFACTURING_COMPLETED);
            });
        
        System.out.println("=== SIMPLE TEST: Completed successfully ===");
    }
}