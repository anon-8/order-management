package com.company.manufacturingorder.domain.model;

import com.company.sharedkernel.AggregateRoot;
import com.company.sharedkernel.OrderId;
import com.company.sharedkernel.events.ManufacturingOrderCreated;
import com.company.sharedkernel.events.ManufacturingOrderStatusChanged;
import com.company.sharedkernel.events.ManufacturingOrderCompleted;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@NoArgsConstructor
public class ManufacturingOrder extends AggregateRoot<OrderId> {
    private static final Logger log = LoggerFactory.getLogger(ManufacturingOrder.class);

    @Getter
    private OrderId id;
    @Getter
    private ProductSpecification productSpecification;
    @Getter
    private OrderStatus status;
    @Getter
    private Timeline timeline;
    @Getter
    private Instant createdAt;
    @Getter
    private Instant updatedAt;

    public static ManufacturingOrder create(
        OrderId orderId,
        ProductSpecification productSpecification,
        Timeline timeline
    ) {
        var order = new ManufacturingOrder();
        order.id = orderId;
        order.productSpecification = productSpecification;
        order.status = OrderStatus.PENDING;
        order.timeline = timeline;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();

        order.addDomainEvent(ManufacturingOrderCreated.of(
            orderId,
            productSpecification.getProductCode(),
            productSpecification.getQuantity()
        ));

        log.info("Manufacturing order created: orderId={}", orderId);

        return order;
    }

    public static ManufacturingOrder reconstitute(
        OrderId orderId,
        ProductSpecification productSpecification,
        OrderStatus status,
        Timeline timeline,
        Instant createdAt,
        Instant updatedAt
    ) {
        var order = new ManufacturingOrder();
        order.id = orderId;
        order.productSpecification = productSpecification;
        order.status = status;
        order.timeline = timeline;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        return order;
    }

    public void changeStatus(OrderStatus newStatus) {
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

        log.info("Manufacturing order status changed: orderId={}, from={}, to={}", id, previousStatus, newStatus);
        addDomainEvent(ManufacturingOrderStatusChanged.of(id, previousStatus.name(), newStatus.name()));

        if (newStatus == OrderStatus.IN_PROGRESS && timeline.getActualStartDate() == null) {
            this.timeline = timeline.withActualStartDate(Instant.now());
        }

        if (newStatus == OrderStatus.COMPLETED) {
            complete();
        }
    }

    public void complete() {
        if (status == OrderStatus.COMPLETED) {
            return; // Already completed
        }
        
        if (status == OrderStatus.PENDING) {
            changeStatus(OrderStatus.IN_PROGRESS);
        }
        
        changeStatus(OrderStatus.COMPLETED);
        
        var completionTime = Instant.now();
        this.timeline = timeline.withActualCompletionDate(completionTime);
        this.updatedAt = completionTime;

        log.info("Manufacturing order completed: orderId={}", id);
        addDomainEvent(ManufacturingOrderCompleted.of(id, completionTime));
    }

    public void cancel() {
        changeStatus(OrderStatus.CANCELLED);
    }

    public boolean isOverdue() {
        return timeline.isOverdue() && status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED;
    }

    public boolean isInProgress() {
        return status == OrderStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

}