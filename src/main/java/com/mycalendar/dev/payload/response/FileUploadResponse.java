package com.mycalendar.dev.payload.response;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileName;
    private String fileExtension;
    private String url;
    private String path;
}
