package com.mycalendar.dev.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidFieldException extends RuntimeException {
    private final String invalidField;
    private final List<String> validFields;

    public InvalidFieldException(String invalidField, List<String> validFields) {
        super("Invalid field: " + invalidField);
        this.invalidField = invalidField;
        this.validFields = validFields;
    }

}
