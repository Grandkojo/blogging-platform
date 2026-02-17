package com.blogging_platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.UserRecord;
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
}

