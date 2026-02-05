package com.blogging_platform;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Generic API response wrapper used by both REST and GraphQL controllers.
 * <p>
 * The response shape is:
 * <pre>
 * {
 *   "status": 200,
 *   "message": "Posts Fetched Successfully",
 *   "data": [...],
 *   "errors": { ... } // optional validation errors
 * }
 * </pre>
 *
 * @param <T> type of the payload in the {@code data} field
 */
@JsonPropertyOrder({ "status", "message", "data" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String message;
    private Map<String, String> errors;
    private T data;

    /**
     * Creates a successful response with a status code, message, and payload.
     */
    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a response that carries validation or field‑level errors.
     */
    private ApiResponse(int status, Map<String, String> errors, T data) {
        this.status = status;
        this.errors = errors;
        this.data = data;
    }

    /**
     * Builds a success {@link ApiResponse} with a given HTTP status, payload and message.
     */
    public static <T> ApiResponse<T> success(HttpStatus status, T data, String message) {
        return new ApiResponse<>(status.value(), message, data);
    }

    /**
     * Builds an error {@link ApiResponse} with only status and message.
     * The {@code data} field will be {@code null}.
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null);
    }

    /**
     * Builds a validation error response containing a map of field -> message.
     */
    public static <T> ApiResponse<T> validationError(HttpStatus status, Map<String, String> errors) {
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

