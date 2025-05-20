package com.mycalendar.dev.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String resourceName;
    private final String filedName;
    private final String fieldValue;

    public NotFoundException(String resourceName, String filedName, String fieldValue) {
        super(String.format("%s not found with %s equal to [%s]", resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1), filedName, fieldValue));
        this.resourceName = resourceName;
        this.filedName = filedName;
        this.fieldValue = fieldValue;
    }
}
