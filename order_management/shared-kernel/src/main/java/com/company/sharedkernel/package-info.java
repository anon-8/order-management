/**
 * Shared Kernel Module
 * 
 * This module contains shared domain concepts, base classes, and events that are 
 * used across multiple bounded contexts in the Order Management System.
 * 
 * Key components:
 * - Base domain classes (AggregateRoot, ValueObject, DomainEvent)
 * - Common value objects (OrderId, Money)
 * - Domain events for inter-module communication
 * - Domain event publisher infrastructure
 * 
 * This module follows the Shared Kernel pattern from Domain-Driven Design,
 * providing a common vocabulary and technical infrastructure that multiple
 * bounded contexts can depend on.
 * 
 * Published events (available to all modules):
 * - ManufacturingOrderCreated, ManufacturingOrderStatusChanged, ManufacturingOrderCompleted
 * - CustomerOrderPlaced, CustomerOrderStatusUpdated, CustomerOrderCancelled
 */
package com.company.sharedkernel;