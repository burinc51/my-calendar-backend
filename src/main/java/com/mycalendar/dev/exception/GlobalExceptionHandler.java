package com.mycalendar.dev.exception;

import com.mycalendar.dev.payload.response.ExceptionResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @ExceptionHandler(APIException.class)
    public ResponseEntity<ExceptionResponse> handlerAPIException(APIException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(e.getStatus().value(), e.getStatus().getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, e.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidFileTypeException(InvalidFileTypeException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(AuthenticationException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.UNAUTHORIZED); // 401
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.FORBIDDEN); // 403
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorizedAccessException(UnauthorizedAccessException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.FORBIDDEN); // 403
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.NOT_FOUND); // 404
    }

    @ExceptionHandler(DataExistsException.class)
    public ResponseEntity<ExceptionResponse> handleDataExistsException(DataExistsException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.CONFLICT); // 409
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        ExceptionResponse errorExceptions = new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), url, new Date(), e.getMessage());
        return new ResponseEntity<>(errorExceptions, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException e, WebRequest w) {
        String url = ((ServletWebRequest) w).getRequest().getRequestURI();
        List<String> errors = new ArrayList<>();

        e.getBindingResult().getFieldErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        e.getBindingResult().getGlobalErrors().forEach(error -> errors.add(error.getDefaultMessage()));

        String combinedErrors = errors.isEmpty() ? "Validation failed" : String.join(", ", errors);

        ExceptionResponse errorResponse = new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), url, new Date(), combinedErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
