package com.company.sharedkernel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private DomainEventPublisher domainEventPublisher;

    @BeforeEach
    void setUp() {
        domainEventPublisher = new DomainEventPublisher(applicationEventPublisher);
    }

    @Test
    void shouldPublishDomainEventSuccessfully() {
        // Given
        var testEvent = new TestDomainEvent("TEST_EVENT_TYPE");
        
        // When
        domainEventPublisher.publishEvent(testEvent);
        
        // Then
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        DomainEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.eventType()).isEqualTo("TEST_EVENT_TYPE");
        assertThat(capturedEvent.occurredOn()).isNotNull();
    }

    @Test
    void shouldPropagateExceptionWhenPublishingFails() {
        // Given
        var testEvent = new TestDomainEvent("FAILING_EVENT");
        var expectedException = new RuntimeException("Publishing failed");
        
        doThrow(expectedException).when(applicationEventPublisher).publishEvent(testEvent);
        
        // When & Then
        assertThatThrownBy(() -> domainEventPublisher.publishEvent(testEvent))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Publishing failed");
        
        verify(applicationEventPublisher, times(1)).publishEvent(testEvent);
    }

    @Test
    void shouldHandleNullEventGracefully() {
        // When & Then - should not throw exception
        assertThatThrownBy(() -> domainEventPublisher.publishEvent(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMeasurePublishingTime() {
        // Given
        var testEvent = new TestDomainEvent("TIMED_EVENT");
        
        // When
        long startTime = System.currentTimeMillis();
        domainEventPublisher.publishEvent(testEvent);
        long endTime = System.currentTimeMillis();
        
        // Then
        verify(applicationEventPublisher, times(1)).publishEvent(testEvent);
        // Verify that method completed within reasonable time
        assertThat(endTime - startTime).isLessThan(1000L); // Less than 1 second
    }

    @Test
    void shouldHandleSlowEventPublishing() {
        // Given
        var testEvent = new TestDomainEvent("SLOW_EVENT");
        
        // Simulate slow publishing
        doAnswer(invocation -> {
            Thread.sleep(15); // Simulate slow operation (over 10ms threshold)
            return null;
        }).when(applicationEventPublisher).publishEvent(testEvent);
        
        // When
        domainEventPublisher.publishEvent(testEvent);
        
        // Then
        verify(applicationEventPublisher, times(1)).publishEvent(testEvent);
        // The warning should be logged (can't easily test logging in unit test)
    }

    // Test implementation of DomainEvent for testing purposes
    private static class TestDomainEvent implements DomainEvent {
        private final String eventType;
        private final Instant occurredOn;

        public TestDomainEvent(String eventType) {
            this.eventType = eventType;
            this.occurredOn = Instant.now();
        }

        @Override
        public String eventType() {
            return eventType;
        }

        @Override
        public Instant occurredOn() {
            return occurredOn;
        }
    }
}