package com.mycalendar.dev.exception;

import lombok.Getter;

@Getter
public class DataExistsException extends RuntimeException {

    private final String filedName;
    private final String fieldValue;

    public DataExistsException(String filedName, String fieldValue) {
        super(String.format("%s %s already exists.", filedName.substring(0, 1).toUpperCase() + filedName.substring(1), fieldValue));
        this.filedName = filedName;
        this.fieldValue = fieldValue;
    }
}