package com.blogging_platform;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"status", "message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String message;
    private Map<String, String> errors;
    private T data;

    private ApiResponse(int status, String message, T data) { 
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private ApiResponse(int status, Map<String, String> errors, T data) { 
        this.status = status;
        this.errors = errors;
        this.data = data;
    }

    // Static Factory Method for Success
    public static <T> ApiResponse<T> success(HttpStatus status, T data, String message) {
        return new ApiResponse<>(status.value(), message, data);
    }

    // Static Factory Method for Errors
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null);
    }

    public static <T> ApiResponse<T> validationError(HttpStatus status, Map<String,String> errors) {
        return new ApiResponse<>(status.value(), errors, null);
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

}

