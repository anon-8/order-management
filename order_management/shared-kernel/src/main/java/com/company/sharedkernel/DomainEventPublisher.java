package com.company.sharedkernel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {
    private static final Logger businessEventLog = LoggerFactory.getLogger("BUSINESS_EVENT");
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public void publishEvent(DomainEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Publishing domain event: {}", event.eventType());
            businessEventLog.info("DOMAIN_EVENT_PUBLISHED eventType={} timestamp={}", 
                    event.eventType(), event.occurredOn());
            
            eventPublisher.publishEvent(event);
            
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                log.warn("Slow event publishing detected: {} took {}ms", event.eventType(), duration);
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to publish domain event: eventType={}, duration={}ms, error={}", 
                    event.eventType(), duration, e.getMessage(), e);
            
            businessEventLog.error("DOMAIN_EVENT_PUBLISH_FAILED eventType={} error={} duration={}ms", 
                    event.eventType(), e.getMessage(), duration);
            throw e;
        }
    }
}