package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.model.User;
import com.blogging_platform.service.UserService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Object>> getUsers() {
        List<UserRecord> users = userService.getUsers();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, users, "Users Fetched Successfully"));
    }
    

    @PostMapping("/users/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "User Registered Successfully"));
    }

    @PostMapping("/users/login")
    public ResponseEntity<ApiResponse<Object>> loginUser(@RequestBody User user) {
        UserRecord lUser =  userService.loginUser(user.getEmail(), user.getPassword());
        if (lUser != null){
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, lUser, "User Login Successful"));
        }
        return ResponseEntity.ok(ApiResponse.error(HttpStatus.NOT_FOUND, "User Login Failed, try again"));
    }
    
}
