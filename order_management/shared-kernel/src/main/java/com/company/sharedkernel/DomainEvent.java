package com.company.sharedkernel;

import java.time.Instant;

/**
 * Base interface for all domain events in the system.
 * 
 * Domain events represent important business events that have occurred
 * and may be of interest to other bounded contexts or external systems.
 * 
 * Individual event implementations can be annotated with @Externalized
 * for publishing to external message brokers.
 */
public interface DomainEvent {
    Instant occurredOn();
    String eventType();
}