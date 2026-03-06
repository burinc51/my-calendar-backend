package com.mycalendar.dev.helper;

public class ApiDocHelper {

    public static final String CREATE_EVENT_SUMMARY = "Create Event";

    public static final String CREATE_EVENT_DESCRIPTION = "Used for creating or updating an event.\n\n" +
            "**Parameters**\n\n" +
            "- **body** (Content-Type: application/json):\n\n" +
            "Example:\n" +
            "{\n" +
            "  \"userId\": 1,\n" +
            "  \"title\": \"string\",\n" +
            "  \"description\": \"string\",\n" +
            "  \"startDate\": \"2025-08-06T15:31:21.038Z\",\n" +
            "  \"endDate\": \"2025-08-06T15:31:21.038Z\",\n" +
            "  \"location\": \"string\",\n" +
            "  \"notificationTime\": \"2025-08-06T15:31:21.038Z\",\n" +
            "  \"repeating\": \"string\",\n" +
            "  \"color\": \"string\",\n" +
            "  \"category\": \"string\",\n" +
            "  \"priority\": \"string\",\n" +
            "  \"groupId\": 1,\n" +
            "  \"assigneeIds\": [1, 2],\n" +
            "  \"pinned\": true\n" +
            "}\n\n" +
            "- **file** (Content-Type: image/png or image/*):\n" +
            "  Image to be attached to the event";

}
