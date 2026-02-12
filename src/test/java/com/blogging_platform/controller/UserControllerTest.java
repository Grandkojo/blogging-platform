package com.blogging_platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.exceptions.AuthenticationException;
import com.blogging_platform.model.User;
import com.blogging_platform.service.UserService;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUsers_rest_returnsWrappedList() {
        List<UserRecord> records = List.of(new UserRecord("id", "John", "john@example.com", "USER"));
        when(userService.getUsers()).thenReturn(records);

        ResponseEntity<ApiResponse<Object>> response = controller.getUsers();

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Users Fetched Successfully", response.getBody().getMessage());
        assertEquals(records, response.getBody().getData());
        verify(userService).getUsers();
    }

    @Test
    void getUsers_graphql_delegatesToService() {
        List<UserRecord> records = List.of(new UserRecord("id", "John", "john@example.com", "USER"));
        when(userService.getUsers()).thenReturn(records);

        List<UserRecord> result = controller.getUserss();

        assertEquals(records, result);
        verify(userService).getUsers();
    }

    @Test
    void registerUser_rest_callsServiceAndReturnsCreated() {
        User user = new User("John", "john@example.com", "password123", "USER");

        ResponseEntity<ApiResponse<Object>> response = controller.registerUser(user);

        verify(userService).registerUser(user);
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
        assertEquals("User Registered Successfully", response.getBody().getMessage());
    }

    @Test
    void loginUser_rest_successReturnsUserAndOk() throws AuthenticationException {
        User request = new User("John", "john@example.com", "password123", "USER");
        UserRecord record = new UserRecord("id", "John", "john@example.com", "USER");
        when(userService.login(request.getEmail(), request.getPassword()))
            .thenReturn(Optional.of(record));

        ResponseEntity<ApiResponse<Object>> response = controller.loginUser(request);

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("User Login Successful", response.getBody().getMessage());
        assertEquals(Optional.of(record), response.getBody().getData());
        verify(userService).login(request.getEmail(), request.getPassword());
    }

    @Test
    void registerUser_graphql_callsServiceAndReturnsTrue() {
        Boolean result = controller.registerUser("John", "john@example.com", "password123", "USER");

        verify(userService).registerUser(any(User.class));
        assertEquals(true, result);
    }

    @Test
    void loginUser_graphql_delegatesToService() throws AuthenticationException {
        UserRecord record = new UserRecord("id", "John", "john@example.com", "USER");
        when(userService.login("john@example.com", "password"))
            .thenReturn(Optional.of(record));

        Optional<UserRecord> result = controller.loginUser("john@example.com", "password");

        assertEquals(Optional.of(record), result);
        verify(userService).login("john@example.com", "password");
    }
}

