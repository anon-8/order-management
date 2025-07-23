package com.company.customerorder.adapter.in.rest;

import com.company.customerorder.application.command.PlaceCustomerOrderCommand;
import com.company.customerorder.application.query.CustomerOrderDto;
import com.company.customerorder.application.service.CustomerOrderApplicationService;
import com.company.customerorder.domain.model.CustomerOrderStatus;
import com.company.sharedkernel.OrderId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerOrderController.class)
@ContextConfiguration(classes = {CustomerOrderControllerTest.TestConfig.class, CustomerOrderController.class})
@DisplayName("Customer Order Controller Tests")
class CustomerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerOrderApplicationService applicationService;

    @Test
    @DisplayName("Should place customer order successfully")
    void shouldPlaceCustomerOrderSuccessfully() throws Exception {
        OrderId orderId = OrderId.generate();
        when(applicationService.placeOrder(any(PlaceCustomerOrderCommand.class)))
            .thenReturn(orderId);

        String requestBody = """
            {
                "orderId": "123e4567-e89b-12d3-a456-426614174000",
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "customerName": "John Doe",
                "customerEmail": "john.doe@example.com",
                "customerAddress": "123 Main St, City, State",
                "items": [
                    {
                        "productCode": "PROD-001",
                        "description": "Product 1",
                        "quantity": 1,
                        "unitPrice": 50.00,
                        "currency": "USD"
                    },
                    {
                        "productCode": "PROD-002",
                        "description": "Product 2",
                        "quantity": 2,
                        "unitPrice": 30.00,
                        "currency": "USD"
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/customer-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());

        verify(applicationService).placeOrder(any());
    }


    @Test
    @DisplayName("Should return not found when order does not exist")
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        OrderId orderId = OrderId.generate();
        
        when(applicationService.findOrder(any()))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customer-orders/{orderId}", orderId.getValue()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should cancel customer order successfully")
    void shouldCancelCustomerOrderSuccessfully() throws Exception {
        OrderId orderId = OrderId.generate();

        String requestBody = """
            {
                "reason": "Customer requested cancellation"
            }
            """;

        mockMvc.perform(post("/api/customer-orders/{orderId}/cancel", orderId.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        verify(applicationService).cancelOrder(any());
    }



    @Test
    @DisplayName("Should handle invalid order ID format")
    void shouldHandleInvalidOrderIdFormat() throws Exception {
        mockMvc.perform(get("/api/customer-orders/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return order items in response")
    void shouldReturnOrderItemsInResponse() throws Exception {
        OrderId orderId = OrderId.generate();
        CustomerOrderDto orderDto = createTestOrderDto(orderId);
        
        when(applicationService.findOrder(any()))
            .thenReturn(Optional.of(orderDto));

        mockMvc.perform(get("/api/customer-orders/{orderId}", orderId.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].productCode").value("PROD-001"))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[1].productCode").value("PROD-002"))
                .andExpect(jsonPath("$.items[1].quantity").value(2));
    }


    private CustomerOrderDto createTestOrderDto(OrderId orderId) {
        return new CustomerOrderDto(
            orderId,
            com.company.customerorder.domain.model.CustomerId.generate(),
            "John Doe",
            "john.doe@example.com",
            "123 Main St, City, State",
            List.of(
                new CustomerOrderDto.OrderItemDto("PROD-001", "Product 1", 1, 
                    com.company.sharedkernel.Money.usd(new BigDecimal("50.00")),
                    com.company.sharedkernel.Money.usd(new BigDecimal("50.00"))),
                new CustomerOrderDto.OrderItemDto("PROD-002", "Product 2", 2, 
                    com.company.sharedkernel.Money.usd(new BigDecimal("30.00")),
                    com.company.sharedkernel.Money.usd(new BigDecimal("60.00")))
            ),
            com.company.sharedkernel.Money.usd(new BigDecimal("110.00")),
            CustomerOrderStatus.PLACED,
            Instant.now(),
            Instant.now(),
            null,
            true
        );
    }

    @SpringBootConfiguration
    static class TestConfig {
        @Bean
        public CustomerOrderApplicationService customerOrderApplicationService() {
            return mock(CustomerOrderApplicationService.class);
        }
    }
}