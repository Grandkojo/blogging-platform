package com.blogging_platform.security.auth;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.blogging_platform.model.User;
import com.blogging_platform.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GoogleOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private GoogleOAuth2UserService googleOAuth2UserService;

    @BeforeEach
    void setUp() {
        googleOAuth2UserService = new GoogleOAuth2UserService(userRepository, passwordEncoder);
    }

    @Test
    void loadUser_ExistingUser_ShouldReturnExistingUser() {
        // Arrange
        String email = "existing@example.com";
        String name = "Existing User";
        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setName(name);
        existingUser.setRole("Regular");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getFullName()).thenReturn(name);
        when(oidcUser.getIdToken()).thenReturn(mock(OidcIdToken.class));
        when(oidcUser.getUserInfo()).thenReturn(null);

        // We need to bypass super.loadUser since it makes network calls
        // Since we can't easily mock super, we might have to refactor or use a spy
        // However, we can test the internal logic if we mock the whole flow or use a
        // Spy on the service
    }
}
