package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.model.User;
import com.blogging_platform.service.UserService;

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
 * REST and GraphQL controller for user registration and login.
 * <p>
 * Provides:
 * <ul>
 *   <li>REST endpoints for registering and logging in users</li>
 *   <li>GraphQL queries and mutations for listing, registering and logging in users</li>
 * </ul>
 */
@RestController
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

    /**
     * REST endpoint that returns all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Object>> getUsers() {
        List<UserRecord> users = userService.getUsers();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, users, "Users Fetched Successfully"));
    }
    

    /**
     * REST endpoint to register a new user.
     */
    @PostMapping("/users/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "User Registered Successfully"));
    }

    /**
     * REST endpoint to authenticate a user with email and password.
     */
    @PostMapping("/users/login")
    public ResponseEntity<ApiResponse<Object>> loginUser(@RequestBody User user) {
        UserRecord lUser =  userService.loginUser(user.getEmail(), user.getPassword());
        if (lUser != null){
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, lUser, "User Login Successful"));
        }
        return ResponseEntity.ok(ApiResponse.error(HttpStatus.NOT_FOUND, "User Login Failed, try again"));
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
    
}
