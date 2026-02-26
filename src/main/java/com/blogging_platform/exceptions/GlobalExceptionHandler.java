package com.blogging_platform.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.blogging_platform.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Object>> handleBinaryErrors(HttpMessageNotReadableException ex) {
                // Malformed JSON / request body -> client error
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "Malformed request body"));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.DuplicateEmailException.class)
        public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(
                        com.blogging_platform.exceptions.DuplicateEmailException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(HttpStatus.CONFLICT, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.AuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
                        com.blogging_platform.exceptions.AuthenticationException ex) {
                // Invalid credentials
                log.warn("Domain authentication failure: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.security.oauth2.core.OAuth2AuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleOAuth2AuthenticationException(
                        org.springframework.security.oauth2.core.OAuth2AuthenticationException ex) {
                log.warn("OAuth2 authentication failure: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED,
                                                "OAuth2 authentication failed: " + ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleSpringSecurityAuthenticationException(
                        org.springframework.security.core.AuthenticationException ex) {
                log.warn("Spring Security authentication failure: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.DatabaseQueryException.class)
        public ResponseEntity<ApiResponse<Object>> handleDatabaseQueryException(
                        com.blogging_platform.exceptions.DatabaseQueryException ex) {
                // Server-side database error
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred"));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.PostNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handlePostNotFoundException(
                        com.blogging_platform.exceptions.PostNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.CommentNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleCommentNotFoundException(
                        com.blogging_platform.exceptions.CommentNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.TagNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleTagNotFoundException(
                        com.blogging_platform.exceptions.TagNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.ReviewNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleReviewNotFoundException(
                        com.blogging_platform.exceptions.ReviewNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(error -> {
                        System.out.println("Error found: " + error.getField() + " - " + error.getDefaultMessage());
                        errors.put(error.getField(), error.getDefaultMessage());
                });
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.validationError(HttpStatus.BAD_REQUEST, errors));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.DuplicateResourceException.class)
        public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(
                        com.blogging_platform.exceptions.DuplicateResourceException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(HttpStatus.CONFLICT, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
                        com.blogging_platform.exceptions.ResourceNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.UserNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleUserNotFound(
                        com.blogging_platform.exceptions.UserNotFoundException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.AuthorizationException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthorizationException(
                        com.blogging_platform.exceptions.AuthorizationException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(HttpStatus.FORBIDDEN, ex.getMessage()));
        }

        @ExceptionHandler({ com.blogging_platform.exceptions.ValidationException.class,
                        com.blogging_platform.exceptions.UserValidationException.class })
        public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(RuntimeException ex) {
                // Domain-level validation errors
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
        }

        @ExceptionHandler({ com.blogging_platform.exceptions.DatabaseConnectionException.class,
                        com.blogging_platform.exceptions.DatabaseTransactionException.class,
                        com.blogging_platform.exceptions.DatabaseException.class })
        public ResponseEntity<ApiResponse<Object>> handleDatabaseExceptions(
                        com.blogging_platform.exceptions.DatabaseException ex) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.BusinessLogicException.class)
        public ResponseEntity<ApiResponse<Object>> handleBusinessLogicException(
                        com.blogging_platform.exceptions.BusinessLogicException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
        }

        @ExceptionHandler(com.blogging_platform.exceptions.ConfigurationException.class)
        public ResponseEntity<ApiResponse<Object>> handleConfigurationException(
                        com.blogging_platform.exceptions.ConfigurationException ex) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
}
