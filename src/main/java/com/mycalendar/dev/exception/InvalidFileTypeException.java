package com.mycalendar.dev.exception;

import lombok.Getter;

@Getter
public class InvalidFileTypeException extends RuntimeException {

    private final String fileType;
    private final String allowedTypes;

    public InvalidFileTypeException(String fileType, String allowedTypes) {
        super(String.format("Invalid file type [%s]. Allowed types are [%s].", fileType, allowedTypes));
        this.fileType = fileType;
        this.allowedTypes = allowedTypes;
    }
}