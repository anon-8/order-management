package com.company.customerorder.domain.model;

import com.company.sharedkernel.AggregateRoot;
import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.Money;
import com.company.sharedkernel.events.CustomerOrderPlaced;
import com.company.sharedkernel.events.CustomerOrderStatusUpdated;
import com.company.sharedkernel.events.CustomerOrderCancelled;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Getter
@NoArgsConstructor
public class CustomerOrder extends AggregateRoot<OrderId> {
    private static final Logger log = LoggerFactory.getLogger(CustomerOrder.class);

    private OrderId id;
    private CustomerInfo customerInfo;
    private List<OrderItem> items;
    private Money totalAmount;
    private CustomerOrderStatus status;
    private Instant placedAt;
    private Instant updatedAt;
    private OrderId manufacturingOrderId;
    
    public static CustomerOrder placeOrder(
        OrderId orderId,
        CustomerInfo customerInfo,
        List<OrderItem> items
    ) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        var order = new CustomerOrder();
        order.id = orderId;
        order.customerInfo = customerInfo;
        order.items = new ArrayList<>(items);
        order.totalAmount = calculateTotalAmount(items);
        order.status = CustomerOrderStatus.PLACED;
        order.placedAt = Instant.now();
        order.updatedAt = Instant.now();
        
        order.addDomainEvent(CustomerOrderPlaced.of(
            orderId,
            customerInfo.getCustomerId().getValue(),
            order.totalAmount
        ));
        
        log.info("Customer order placed: orderId={}, totalAmount={}", orderId, order.totalAmount);
        
        return order;
    }

    public static CustomerOrder reconstitute(
        OrderId orderId,
        CustomerInfo customerInfo,
        List<OrderItem> items,
        Money totalAmount,
        CustomerOrderStatus status,
        Instant placedAt,
        Instant updatedAt,
        OrderId manufacturingOrderId
    ) {
        var order = new CustomerOrder();
        order.id = orderId;
        order.customerInfo = customerInfo;
        order.items = new ArrayList<>(items != null ? items : new ArrayList<>());
        order.totalAmount = totalAmount;
        order.status = status;
        order.placedAt = placedAt;
        order.updatedAt = updatedAt;
        order.manufacturingOrderId = manufacturingOrderId;
        return order;
    }
    
    public void updateStatus(CustomerOrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", status, newStatus)
            );
        }
        
        var previousStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();
        
        log.info("Customer order status updated: orderId={}, from={}, to={}", id, previousStatus, newStatus);
        addDomainEvent(CustomerOrderStatusUpdated.of(id, previousStatus.name(), newStatus.name()));
    }
    
    public void linkManufacturingOrder(OrderId manufacturingOrderId) {
        if (manufacturingOrderId == null) {
            throw new IllegalArgumentException("Manufacturing order ID cannot be null");
        }
        
        this.manufacturingOrderId = manufacturingOrderId;
        this.updatedAt = Instant.now();
    }
    
    public void notifyManufacturingStarted() {
        if (status == CustomerOrderStatus.CONFIRMED) {
            updateStatus(CustomerOrderStatus.MANUFACTURING_IN_PROGRESS);
        }
    }
    
    public void notifyManufacturingCompleted() {
        if (status == CustomerOrderStatus.MANUFACTURING_IN_PROGRESS) {
            updateStatus(CustomerOrderStatus.MANUFACTURING_COMPLETED);
        }
    }
    
    public void markAsShipped() {
        if (status == CustomerOrderStatus.MANUFACTURING_COMPLETED) {
            updateStatus(CustomerOrderStatus.SHIPPED);
        }
    }
    
    public void markAsDelivered() {
        if (status == CustomerOrderStatus.SHIPPED) {
            updateStatus(CustomerOrderStatus.DELIVERED);
        }
    }
    
    public void cancel(String reason) {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot cancel a terminal order");
        }
        
        this.status = CustomerOrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
        
        addDomainEvent(CustomerOrderCancelled.of(id, reason));
    }
    
    public void confirm() {
        if (status == CustomerOrderStatus.PLACED) {
            updateStatus(CustomerOrderStatus.CONFIRMED);
        }
    }
    
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public boolean isActive() {
        return !status.isTerminal();
    }
    
    public boolean isCancelled() {
        return status == CustomerOrderStatus.CANCELLED;
    }
    
    public boolean isDelivered() {
        return status == CustomerOrderStatus.DELIVERED;
    }
    
    private static Money calculateTotalAmount(List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate total for empty items list");
        }
        
        Money total = Money.zero(items.getFirst().getUnitPrice().getCurrency());
        for (OrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

}