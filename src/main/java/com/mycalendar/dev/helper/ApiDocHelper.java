package com.mycalendar.dev.helper;

public class ApiDocHelper {

    public static final String CREATE_EVENT_SUMMARY = "Create Event";

    public static final String CREATE_EVENT_DESCRIPTION = """
            Used for creating or updating an event.
            
            **Parameters**
            
            - **body** (Content-Type: `application/json`):
            
            Example:
            ```json
            {
              "userId": 1,
              "title": "string",
              "description": "string",
              "startDate": "2025-08-06T15:31:21.038Z",
              "endDate": "2025-08-06T15:31:21.038Z",
              "location": "string",
              "notificationTime": "2025-08-06T15:31:21.038Z",
              "repeating": "string",
              "color": "string",
              "category": "string",
              "priority": "string",
              "groupId": 1,
              "assigneeIds": [1, 2],
              "pinned": true
            }
            ```
            
            - **file** (Content-Type: `image/png` or `image/*`):
              Image to be attached to the event
            """;

}
