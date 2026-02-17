package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.exceptions.AuthenticationException;
import com.blogging_platform.exceptions.DuplicateEmailException;
import com.blogging_platform.model.User;
import com.blogging_platform.repository.UserRepository;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_hashesPasswordAndSaves_whenEmailNotExisting() throws DuplicateEmailException {
        User user = new User("John Doe", "john@example.com", "plainPassword", "USER");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");

        userService.registerUser(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = java.util.Objects.requireNonNull(captor.getValue(), "saved user must not be null");

        assertEquals("hashedPassword", saved.getPassword());
    }

    @Test
    void registerUser_throwsDuplicateEmail_whenEmailAlreadyExists() {
        User user = new User("John Doe", "john@example.com", "plainPassword", "USER");
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(user));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void login_returnsUserRecord_whenCredentialsValid() throws AuthenticationException {
        String email = "john@example.com";
        String password = "plainPassword";
        UUID id = UUID.randomUUID();

        User user = new User("John Doe", email, "hashed", "USER");
        user.setId(id);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashed")).thenReturn(true);

        Optional<UserRecord> result = userService.login(email, password);

        assertEquals(true, result.isPresent());
        assertEquals(email, result.get().email());
    }

    @Test
    void login_throwsAuthenticationException_whenCredentialsInvalid() {
        String email = "john@example.com";
        String password = "plainPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> userService.login(email, password));
    }

    @Test
    void getUsers_mapsEntitiesToRecords() {
        User user = new User("John", "john@example.com", "pwd", "USER");
        user.setId(UUID.randomUUID());
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserRecord> records = userService.getUsers();

        assertEquals(1, records.size());
        assertEquals(user.getEmail(), records.get(0).email());
    }
}

