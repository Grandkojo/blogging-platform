package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;



/**
 * REST and GraphQL controller for user registration, authentication and session management.
 */
@RestController
@Tag(name = "Users", description = "User registration, authentication and listing APIs")
public class UserController {
    private final UserService userService;

    /**
     * Creates a controller with the required {@link UserService}.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GraphQL query that returns all users.
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserRecord> getUserss() {
        return userService.getUsers();
    }

    @Operation(
        summary = "List users",
        description = "Returns all registered users."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Users fetched successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected server error"
        )
    })
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getUsers() {
        List<UserRecord> users = userService.getUsers();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, users, "Users Fetched Successfully"));
    }
}
