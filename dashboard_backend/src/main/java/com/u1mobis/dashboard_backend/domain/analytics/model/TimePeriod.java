package com.u1mobis.dashboard_backend.domain.analytics.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class TimePeriod extends ValueObject {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final PeriodType type;
    
    public TimePeriod(LocalDateTime startTime, LocalDateTime endTime, PeriodType type) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type != null ? type : PeriodType.CUSTOM;
    }
    
    public static TimePeriod hourly(LocalDateTime hour) {
        LocalDateTime start = hour.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime end = start.plusHours(1);
        return new TimePeriod(start, end, PeriodType.HOURLY);
    }
    
    public static TimePeriod daily(LocalDateTime day) {
        LocalDateTime start = day.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime end = start.plusDays(1);
        return new TimePeriod(start, end, PeriodType.DAILY);
    }
    
    public static TimePeriod weekly(LocalDateTime week) {
        LocalDateTime start = week.truncatedTo(ChronoUnit.WEEKS);
        LocalDateTime end = start.plusWeeks(1);
        return new TimePeriod(start, end, PeriodType.WEEKLY);
    }
    
    public static TimePeriod monthly(LocalDateTime month) {
        LocalDateTime start = month.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime end = start.plusMonths(1);
        return new TimePeriod(start, end, PeriodType.MONTHLY);
    }
    
    public static TimePeriod custom(LocalDateTime startTime, LocalDateTime endTime) {
        return new TimePeriod(startTime, endTime, PeriodType.CUSTOM);
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public PeriodType getType() {
        return type;
    }
    
    public long getDurationMinutes() {
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }
    
    public long getDurationHours() {
        return ChronoUnit.HOURS.between(startTime, endTime);
    }
    
    public long getDurationDays() {
        return ChronoUnit.DAYS.between(startTime, endTime);
    }
    
    public boolean contains(LocalDateTime dateTime) {
        return !dateTime.isBefore(startTime) && dateTime.isBefore(endTime);
    }
    
    public boolean overlaps(TimePeriod other) {
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }
    
    public TimePeriod getNextPeriod() {
        return switch (type) {
            case HOURLY -> hourly(endTime);
            case DAILY -> daily(endTime);
            case WEEKLY -> weekly(endTime);
            case MONTHLY -> monthly(endTime);
            case CUSTOM -> throw new UnsupportedOperationException("Cannot get next period for custom time period");
        };
    }
    
    public TimePeriod getPreviousPeriod() {
        return switch (type) {
            case HOURLY -> hourly(startTime.minusHours(1));
            case DAILY -> daily(startTime.minusDays(1));
            case WEEKLY -> weekly(startTime.minusWeeks(1));
            case MONTHLY -> monthly(startTime.minusMonths(1));
            case CUSTOM -> throw new UnsupportedOperationException("Cannot get previous period for custom time period");
        };
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TimePeriod that = (TimePeriod) obj;
        return Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime) &&
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, type);
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s - %s", type.getDisplayName(), startTime, endTime);
    }
    
    public enum PeriodType {
        HOURLY("Hourly"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        CUSTOM("Custom");
        
        private final String displayName;
        
        PeriodType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}