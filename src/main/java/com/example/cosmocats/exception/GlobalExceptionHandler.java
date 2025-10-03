package com.example.cosmocats.exception;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;


@Slf4j
@ControllerAdvice

public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMessage = buildValidationErrorMessage(ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                errorMessage,
                path
        );
        
        log.warn("Validation error: {} - Path: {}", errorMessage, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String buildValidationErrorMessage(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();
        
        String objectName = ex.getBindingResult().getObjectName();
        
        errorMessage.append("Validation failed for object '")
                   .append(objectName)
                   .append("': ");
        
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            FieldError firstError = ex.getBindingResult().getFieldErrors().get(0);
            errorMessage.append("Field '")
                       .append(firstError.getField())
                       .append("' ")
                       .append(firstError.getDefaultMessage());
        } else if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            errorMessage.append(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        } else {
            errorMessage.append("Validation failed");
        }
        
        return errorMessage.toString();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.info("Product not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return errorResponse;
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleProductAlreadyExists(
            ProductAlreadyExistsException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.info("Product already exists: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return errorResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        
        log.error("Internal server error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        return errorResponse;
    }
}