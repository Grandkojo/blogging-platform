package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.model.User;
import com.blogging_platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<ApiResponse<Object>> getUsers() {
        List<UserRecord> users = userService.getUsers();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, users, "Users Fetched Successfully"));
    }
    

    @Operation(
        summary = "Register user",
        description = "Registers a new user with name, email, password and role."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/users/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "User Registered Successfully"));
    }

    @Operation(
        summary = "Login user",
        description = "Authenticates a user using email and password. On success, the session is stored."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Malformed request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/users/login")
    public ResponseEntity<ApiResponse<Object>> loginUser(@RequestBody User user) {
        try {
            UserRecord lUser = userService.loginUser(user.getEmail(), user.getPassword());
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, lUser, "User Login Successful"));
        } catch (com.blogging_platform.exceptions.AuthenticationException ex) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage()));
        }
    }

    @Operation(
        summary = "Logout user",
        description = "Logs out the current user by clearing their session."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No active session"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/users/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser() {
        userService.logoutUser();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "User Logout Successful"));
    }

    /**
     * GraphQL mutation to register a new user.
     */
    @MutationMapping
    public Boolean registerUser(
        @Argument String name,
        @Argument String email,
        @Argument String password,
        @Argument String role
    ) {
        User user = new User(name, email, password, role);
        userService.registerUser(user);
        return true;
    }

    /**
     * GraphQL mutation to authenticate a user and return their profile.
     */
    @MutationMapping
    public UserRecord loginUser(
        @Argument String email,
        @Argument String password
    ) {
        return userService.loginUser(email, password);
    }

    /**
     * GraphQL mutation to log out the current user.
     */
    @MutationMapping
    public Boolean logoutUserMutation() {
        userService.logoutUser();
        return true;
    }
    
}
