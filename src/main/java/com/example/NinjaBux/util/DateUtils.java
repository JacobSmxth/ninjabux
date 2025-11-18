package com.example.NinjaBux.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtils {

    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    public static LocalDate getWeekEnd(LocalDate date) {
        return date.with(DayOfWeek.SUNDAY);
    }
}
