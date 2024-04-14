package com.pokertools;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeCalculator {

    static class TimeInterval implements Comparable<TimeInterval> {
        LocalDateTime start;
        LocalDateTime end;

        TimeInterval(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(TimeInterval o) {
            return this.start.compareTo(o.start);
        }
    }

    public static long calculateTotalHoursPlayed(List<TimeInterval> intervals) {
        if (intervals.isEmpty()) return 0;

        // Sort intervals by start time
        Collections.sort(intervals);

        // Merge overlapping intervals
        List<TimeInterval> merged = new ArrayList<>();
        TimeInterval prev = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            TimeInterval current = intervals.get(i);
            if (prev.end.isBefore(current.start)) {
                merged.add(prev);
                prev = current;
            } else {
                prev.end = max(prev.end, current.end);
            }
        }
        merged.add(prev);

        // Calculate total duration in hours
        long totalMinutes = 0;
        for (TimeInterval interval : merged) {
            long duration = ChronoUnit.MINUTES.between(interval.start, interval.end);
            totalMinutes += duration;
        }

        return totalMinutes / 60;
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }


}
