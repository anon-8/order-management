package com.company.manufacturingorder.adapter.in.rest;

import com.company.manufacturingorder.application.query.ManufacturingOrderDto;
import com.company.manufacturingorder.application.service.ManufacturingOrderApplicationService;
import com.company.manufacturingorder.domain.model.OrderStatus;
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

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManufacturingOrderController.class)
@ContextConfiguration(classes = {ManufacturingOrderControllerTest.TestConfig.class, ManufacturingOrderController.class})
@DisplayName("Manufacturing Order Controller Tests")
class ManufacturingOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManufacturingOrderApplicationService applicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create manufacturing order successfully")
    void shouldCreateManufacturingOrderSuccessfully() throws Exception {
        OrderId orderId = OrderId.generate();
        when(applicationService.createOrder(any()))
            .thenReturn(orderId);

        String requestBody = """
            {
                "orderId": "123e4567-e89b-12d3-a456-426614174000",
                "productCode": "PROD-001",
                "description": "Test Product",
                "quantity": 10,
                "specifications": "Standard specifications",
                "expectedStartDate": "2024-01-15T10:00:00Z",
                "expectedCompletionDate": "2024-01-30T18:00:00Z"
            }
            """;

        mockMvc.perform(post("/api/manufacturing-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());

        verify(applicationService).createOrder(any());
    }


    @Test
    @DisplayName("Should get manufacturing order successfully")
    void shouldGetManufacturingOrderSuccessfully() throws Exception {
        OrderId orderId = OrderId.generate();
        ManufacturingOrderDto orderDto = createTestOrderDto(orderId);
        
        when(applicationService.findOrder(orderId))
            .thenReturn(Optional.of(orderDto));

        mockMvc.perform(get("/api/manufacturing-orders/{orderId}", orderId.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId.value").value(orderId.getValue().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.productCode").value("PROD-001"));

        verify(applicationService).findOrder(orderId);
    }

    @Test
    @DisplayName("Should return not found when order does not exist")
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        OrderId orderId = OrderId.generate();
        
        when(applicationService.findOrder(orderId))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/manufacturing-orders/{orderId}", orderId.getValue()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should change order status successfully")
    void shouldChangeOrderStatusSuccessfully() throws Exception {
        OrderId orderId = OrderId.generate();

        String requestBody = """
            {
                "newStatus": "IN_PROGRESS"
            }
            """;

        mockMvc.perform(put("/api/manufacturing-orders/{orderId}/status", orderId.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        verify(applicationService).changeOrderStatus(any());
    }



    @Test
    @DisplayName("Should complete manufacturing order successfully")
    void shouldCompleteManufacturingOrderSuccessfully() throws Exception {
        OrderId orderId = OrderId.generate();

        mockMvc.perform(post("/api/manufacturing-orders/{orderId}/complete", orderId.getValue()))
                .andExpect(status().isOk());

        verify(applicationService).completeOrder(any());
    }

    @Test
    @DisplayName("Should handle invalid order ID format")
    void shouldHandleInvalidOrderIdFormat() throws Exception {
        mockMvc.perform(get("/api/manufacturing-orders/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    private ManufacturingOrderDto createTestOrderDto(OrderId orderId) {
        return new ManufacturingOrderDto(
            orderId,
            "PROD-001",
            "Test Product",
            10,
            "Standard specifications",
            OrderStatus.PENDING,
            Instant.now(),
            Instant.now().plusSeconds(86400),
            null,
            null,
            Instant.now(),
            Instant.now(),
            false
        );
    }

    @SpringBootConfiguration
    static class TestConfig {
        @Bean
        public ManufacturingOrderApplicationService manufacturingOrderApplicationService() {
            return mock(ManufacturingOrderApplicationService.class);
        }
    }
}