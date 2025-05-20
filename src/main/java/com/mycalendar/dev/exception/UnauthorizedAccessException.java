package com.mycalendar.dev.exception;

import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends RuntimeException {

    private final String primaryResourceName;
    private final String primaryFieldName;
    private final String primaryFieldValue;
    private final String secondaryResourceName;
    private final String secondaryFieldName;
    private final String secondaryFieldValue;

    public UnauthorizedAccessException(String primaryResourceName, String primaryFieldName, String primaryFieldValue, String secondaryResourceName, String secondaryFieldName, String secondaryFieldValue) {
        super(String.format("This %s with %s [%s] does not fall into the %s with %s [%s] as specified.", primaryResourceName.toLowerCase(), primaryFieldName.toLowerCase(), primaryFieldValue, secondaryResourceName.toLowerCase(), secondaryFieldName.toLowerCase(), secondaryFieldValue));
        this.primaryResourceName = primaryResourceName;
        this.primaryFieldName = primaryFieldName;
        this.primaryFieldValue = primaryFieldValue;
        this.secondaryResourceName = secondaryResourceName;
        this.secondaryFieldName = secondaryFieldName;
        this.secondaryFieldValue = secondaryFieldValue;
    }
}
