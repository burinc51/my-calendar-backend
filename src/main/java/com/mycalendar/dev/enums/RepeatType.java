package com.mycalendar.dev.enums;

/**
 * Repeat type for events
 */
public enum RepeatType {
    NONE,       // No repeat
    DAILY,      // Every day
    WEEKLY,     // Every week
    MONTHLY,    // Every month
    YEARLY,     // Every year
    CUSTOM      // Custom (uses repeatInterval and repeatDays)
}
