package com.mycalendar.dev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteImageUploadResponse {
    private String url;
    private String fileName;
    private String mimeType;
    private long size;
}

