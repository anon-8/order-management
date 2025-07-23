package com.company.customerorder.adapter.in.rest;

import com.company.sharedkernel.OrderId;
import com.company.customerorder.application.command.*;
import com.company.customerorder.application.query.CustomerOrderDto;
import com.company.customerorder.application.service.CustomerOrderApplicationService;
import com.company.customerorder.domain.model.CustomerId;
import com.company.customerorder.domain.model.CustomerOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer-orders")
@RequiredArgsConstructor
public class CustomerOrderController {
    private final CustomerOrderApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        var command = new PlaceCustomerOrderCommand(
            OrderId.of(request.orderId()),
            CustomerId.of(request.customerId()),
            request.customerName(),
            request.customerEmail(),
            request.customerAddress(),
            request.items().stream()
                .map(item -> new PlaceCustomerOrderCommand.OrderItemCommand(
                    item.productCode(),
                    item.description(),
                    item.quantity(),
                    item.unitPrice(),
                    item.currency()
                ))
                .toList()
        );
        
        var orderId = applicationService.placeOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new PlaceOrderResponse(orderId.getValue()));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<CustomerOrderDto> getOrder(@PathVariable UUID orderId) {
        return applicationService.findOrder(OrderId.of(orderId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody UpdateStatusRequest request
    ) {
        var command = new UpdateCustomerOrderStatusCommand(
            OrderId.of(orderId),
            request.newStatus()
        );
        
        applicationService.updateOrderStatus(command);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable UUID orderId) {
        var command = new UpdateCustomerOrderStatusCommand(
            OrderId.of(orderId),
            CustomerOrderStatus.CONFIRMED
        );
        
        applicationService.updateOrderStatus(command);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
        @PathVariable UUID orderId,
        @Valid @RequestBody CancelOrderRequest request
    ) {
        var command = new CancelCustomerOrderCommand(
            OrderId.of(orderId),
            request.reason()
        );
        
        applicationService.cancelOrder(command);
        return ResponseEntity.ok().build();
    }
    
    public record PlaceOrderRequest(
        UUID orderId,
        UUID customerId,
        String customerName,
        String customerEmail,
        String customerAddress,
        List<OrderItemRequest> items
    ) {}
    
    public record OrderItemRequest(
        String productCode,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        Currency currency
    ) {}
    
    public record PlaceOrderResponse(
        UUID orderId
    ) {}
    
    public record UpdateStatusRequest(
        CustomerOrderStatus newStatus
    ) {}
    
    public record CancelOrderRequest(
        String reason
    ) {}
}