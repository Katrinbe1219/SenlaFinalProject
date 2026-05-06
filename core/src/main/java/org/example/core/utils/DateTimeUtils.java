package org.example.core.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeUtils {
    public static Instant toInstant(LocalDate date){
        if (date == null) return null;
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    public static Instant toInstantEndDay(LocalDate date){
        if (date == null) return null;
        return date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
    }

    public static LocalDate toLocalDate(Instant instant){
        if (instant == null) return null;
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Instant instant){
        if (instant == null) return null;
        return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    public static Instant toInstant(LocalDateTime date){
        if (date == null) return null;
        return date.atZone(ZoneOffset.UTC).toInstant();
    }
}
