package com.company.customerorder.domain.model;

public enum CustomerOrderStatus {
    PLACED,
    CONFIRMED,
    MANUFACTURING_IN_PROGRESS,
    MANUFACTURING_COMPLETED,
    SHIPPED,
    DELIVERED,
    CANCELLED;
    
    public boolean canTransitionTo(CustomerOrderStatus newStatus) {
        return switch (this) {
            case PLACED -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == MANUFACTURING_IN_PROGRESS || newStatus == CANCELLED;
            case MANUFACTURING_IN_PROGRESS -> newStatus == MANUFACTURING_COMPLETED || newStatus == CANCELLED;
            case MANUFACTURING_COMPLETED -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
    
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}