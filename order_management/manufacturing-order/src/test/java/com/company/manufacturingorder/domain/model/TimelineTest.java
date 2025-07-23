package com.company.manufacturingorder.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Timeline Value Object Tests")
class TimelineTest {

    @Test
    @DisplayName("Should create valid Timeline")
    void shouldCreateValidTimeline() {
        Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant completionDate = Instant.now().plus(7, ChronoUnit.DAYS);

        Timeline timeline = Timeline.create(startDate, completionDate);

        assertNotNull(timeline);
        assertEquals(startDate, timeline.getExpectedStartDate());
        assertEquals(completionDate, timeline.getExpectedCompletionDate());
        assertNull(timeline.getActualStartDate());
        assertNull(timeline.getActualCompletionDate());
    }

    @Test
    @DisplayName("Should throw exception when start date is after completion date")
    void shouldThrowExceptionWhenStartDateIsAfterCompletionDate() {
        Instant startDate = Instant.now().plus(7, ChronoUnit.DAYS);
        Instant completionDate = Instant.now().plus(1, ChronoUnit.DAYS);

        assertThrows(IllegalArgumentException.class, () -> 
            Timeline.create(startDate, completionDate));
    }

    @Test
    @DisplayName("Should throw exception for null dates")
    void shouldThrowExceptionForNullDates() {
        Instant now = Instant.now();
        
        assertThrows(IllegalArgumentException.class, () -> Timeline.create(null, now));
        assertThrows(IllegalArgumentException.class, () -> Timeline.create(now, null));
    }

    @Test
    @DisplayName("Should detect overdue timeline")
    void shouldDetectOverdueTimeline() {
        Instant pastStartDate = Instant.now().minus(5, ChronoUnit.DAYS);
        Instant pastCompletionDate = Instant.now().minus(1, ChronoUnit.DAYS);

        Timeline timeline = Timeline.create(pastStartDate, pastCompletionDate);

        assertTrue(timeline.isOverdue());
    }

    @Test
    @DisplayName("Should not be overdue when within timeline")
    void shouldNotBeOverdueWhenWithinTimeline() {
        Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant completionDate = Instant.now().plus(7, ChronoUnit.DAYS);

        Timeline timeline = Timeline.create(startDate, completionDate);

        assertFalse(timeline.isOverdue());
    }

    @Test
    @DisplayName("Should update actual start date")
    void shouldUpdateActualStartDate() {
        Instant expectedStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant expectedCompletion = Instant.now().plus(7, ChronoUnit.DAYS);
        Instant actualStart = Instant.now();

        Timeline timeline = Timeline.create(expectedStart, expectedCompletion);
        Timeline updated = timeline.withActualStartDate(actualStart);

        assertEquals(actualStart, updated.getActualStartDate());
        assertEquals(expectedStart, updated.getExpectedStartDate());
        assertEquals(expectedCompletion, updated.getExpectedCompletionDate());
    }

    @Test
    @DisplayName("Should update actual completion date")
    void shouldUpdateActualCompletionDate() {
        Instant expectedStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant expectedCompletion = Instant.now().plus(7, ChronoUnit.DAYS);
        Instant actualCompletion = Instant.now().plus(5, ChronoUnit.DAYS);

        Timeline timeline = Timeline.create(expectedStart, expectedCompletion);
        Timeline updated = timeline.withActualCompletionDate(actualCompletion);

        assertEquals(actualCompletion, updated.getActualCompletionDate());
        assertEquals(expectedStart, updated.getExpectedStartDate());
        assertEquals(expectedCompletion, updated.getExpectedCompletionDate());
    }
}