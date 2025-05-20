package com.mycalendar.dev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private int statusCode;
    private String statusMessage;
    private String path;
    private Date timestamp;
    private String message;
}