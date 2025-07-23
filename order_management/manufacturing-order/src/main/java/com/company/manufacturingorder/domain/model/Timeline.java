package com.company.manufacturingorder.domain.model;

import com.company.sharedkernel.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class Timeline extends ValueObject {
    private final Instant expectedStartDate;
    private final Instant expectedCompletionDate;
    private final Instant actualStartDate;
    private final Instant actualCompletionDate;
    
    public static Timeline create(Instant expectedStartDate, Instant expectedCompletionDate) {
        if (expectedStartDate == null) {
            throw new IllegalArgumentException("Expected start date cannot be null");
        }
        if (expectedCompletionDate == null) {
            throw new IllegalArgumentException("Expected completion date cannot be null");
        }
        if (expectedCompletionDate.isBefore(expectedStartDate)) {
            throw new IllegalArgumentException("Expected completion date cannot be before start date");
        }
        
        return new Timeline(expectedStartDate, expectedCompletionDate, null, null);
    }
    
    public Timeline withActualStartDate(Instant actualStartDate) {
        if (actualStartDate == null) {
            throw new IllegalArgumentException("Actual start date cannot be null");
        }
        return new Timeline(expectedStartDate, expectedCompletionDate, actualStartDate, actualCompletionDate);
    }
    
    public Timeline withActualCompletionDate(Instant actualCompletionDate) {
        if (actualCompletionDate == null) {
            throw new IllegalArgumentException("Actual completion date cannot be null");
        }
        if (actualStartDate != null && actualCompletionDate.isBefore(actualStartDate)) {
            throw new IllegalArgumentException("Actual completion date cannot be before actual start date");
        }
        return new Timeline(expectedStartDate, expectedCompletionDate, actualStartDate, actualCompletionDate);
    }
    
    public boolean isOverdue() {
        Instant now = Instant.now();
        return actualCompletionDate == null && now.isAfter(expectedCompletionDate);
    }
    
    public boolean isCompleted() {
        return actualCompletionDate != null;
    }
    
    public long getDurationInDays() {
        if (actualStartDate == null || actualCompletionDate == null) {
            return ChronoUnit.DAYS.between(expectedStartDate, expectedCompletionDate);
        }
        return ChronoUnit.DAYS.between(actualStartDate, actualCompletionDate);
    }
}