package com.blogging_platform.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;

/**
 * Centralized exception handling for GraphQL endpoints.
 * Maps domain and validation exceptions to meaningful GraphQL error messages
 * instead of the default generic INTERNAL_ERROR.
 */
@ControllerAdvice
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler(PostNotFoundException.class)
    public GraphQLError handlePostNotFound(PostNotFoundException ex) {
        return GraphqlErrorBuilder.newError()
                .message(ex.getUserMessage())
                .errorType(ErrorType.NOT_FOUND)
                .build();
    }

    @GraphQlExceptionHandler(DuplicateEmailException.class)
    public GraphQLError handleDuplicateEmail(DuplicateEmailException ex) {
        return GraphqlErrorBuilder.newError()
                .message(ex.getUserMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .build();
    }

    @GraphQlExceptionHandler(DuplicateResourceException.class)
    public GraphQLError handleDuplicateResource(DuplicateResourceException ex) {
        return GraphqlErrorBuilder.newError()
                .message(ex.getUserMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .build();
    }

    @GraphQlExceptionHandler(DatabaseQueryException.class)
    public GraphQLError handleDatabaseQuery(DatabaseQueryException ex) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", ex.getErrorCode());
        extensions.put("detail", ex.getMessage());

        return GraphqlErrorBuilder.newError()
                // Include the error code prefix in the message, e.g. "[DB_QUERY_ERROR] Failed to create post ..."
                .message(ex.toString())
                .errorType(ErrorType.INTERNAL_ERROR)
                .extensions(extensions)
                .build();
    }

    @GraphQlExceptionHandler(AuthenticationException.class)
    public GraphQLError handleAuthentication(AuthenticationException ex) {
        return GraphqlErrorBuilder.newError()
                .message(ex.getUserMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .build();
    }

    /**
     * Fallback for any other unhandled exception types.
     */
    @GraphQlExceptionHandler(Exception.class)
    public GraphQLError handleGeneric(Exception ex) {
        return GraphqlErrorBuilder.newError()
                .message(ex.getMessage())
                .errorType(ErrorType.INTERNAL_ERROR)
                .build();
    }
}

