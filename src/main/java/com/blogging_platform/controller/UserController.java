package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.model.User;
import com.blogging_platform.service.UserService;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "User Registered Successfully"));
    }

    @PostMapping("/users/login")
    public ResponseEntity<ApiResponse<Object>> loginUser(@RequestBody User user) {
        Optional<UserRecord> newUser =  userService.login(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.FOUND, newUser, "User Login Successful"));
    }
    
}
