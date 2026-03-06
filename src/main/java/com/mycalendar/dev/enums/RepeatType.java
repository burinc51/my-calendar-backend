package com.mycalendar.dev.enums;

/**
 * ประเภทการทำซ้ำของกิจกรรม
 */
public enum RepeatType {
    NONE,       // ไม่ทำซ้ำ
    DAILY,      // ทุกวัน
    WEEKLY,     // ทุกสัปดาห์
    MONTHLY,    // ทุกเดือน
    YEARLY,     // ทุกปี
    CUSTOM      // กำหนดเอง (ใช้ repeatInterval และ repeatDays)
}

