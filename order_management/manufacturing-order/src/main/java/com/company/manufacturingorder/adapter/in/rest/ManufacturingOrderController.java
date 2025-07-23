package com.company.manufacturingorder.adapter.in.rest;

import com.company.sharedkernel.OrderId;
import com.company.manufacturingorder.application.command.*;
import com.company.manufacturingorder.application.query.ManufacturingOrderDto;
import com.company.manufacturingorder.application.service.ManufacturingOrderApplicationService;
import com.company.manufacturingorder.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/manufacturing-orders")
@RequiredArgsConstructor
public class ManufacturingOrderController {
    private final ManufacturingOrderApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var command = new CreateManufacturingOrderCommand(
            OrderId.of(request.orderId()),
            request.productCode(),
            request.description(),
            request.quantity(),
            request.specifications(),
            request.expectedStartDate(),
            request.expectedCompletionDate()
        );
        
        var orderId = applicationService.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateOrderResponse(orderId.getValue()));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ManufacturingOrderDto> getOrder(@PathVariable UUID orderId) {
        return applicationService.findOrder(OrderId.of(orderId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> changeOrderStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody ChangeStatusRequest request
    ) {
        var command = new ChangeOrderStatusCommand(
            OrderId.of(orderId),
            request.newStatus()
        );
        
        applicationService.changeOrderStatus(command);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/start")
    public ResponseEntity<Void> startOrder(@PathVariable UUID orderId) {
        var command = new ChangeOrderStatusCommand(
            OrderId.of(orderId),
            OrderStatus.IN_PROGRESS
        );
        
        applicationService.changeOrderStatus(command);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable UUID orderId) {
        var command = new CompleteManufacturingOrderCommand(OrderId.of(orderId));
        applicationService.completeOrder(command);
        return ResponseEntity.ok().build();
    }
    
    public record CreateOrderRequest(
        UUID orderId,
        String productCode,
        String description,
        Integer quantity,
        String specifications,
        Instant expectedStartDate,
        Instant expectedCompletionDate
    ) {}
    
    public record CreateOrderResponse(
        UUID orderId
    ) {}
    
    public record ChangeStatusRequest(
        OrderStatus newStatus
    ) {}
}