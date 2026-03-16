package com.blogging_platform.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.exceptions.AuthenticationException;
import com.blogging_platform.exceptions.DuplicateEmailException;
import com.blogging_platform.model.User;
import com.blogging_platform.repository.UserRepository;

/**
 * Application service for user registration and authentication. Hashes passwords
 * and delegates persistence to {@link UserDAO}; updates {@link SessionManager} on login.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Creates a user service with the repository. */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user. Password is hashed before storage.
     *
     * @param user the user (name, email, password, role)
     * @throws DuplicateEmailException if the email is already registered
     */
    @CacheEvict(cacheNames = "users", allEntries = true)
    public void registerUser(User user) throws DuplicateEmailException {
        if(userRepository.existsByEmail(user.getEmail())){
            throw new DuplicateEmailException("An account with this email already exists");
        }

        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /**
     * Authenticates the user and sets the session on success.
     *
     * @param email    user email
     * @param password plain-text password
     * @return true if login succeeded
     * @throws AuthenticationException if credentials are invalid
     */
    public Optional<UserRecord> login(String email, String password) throws AuthenticationException {

        Optional<User> userEntity = userRepository.findByEmail(email);
        if (userEntity.isPresent()) {
            User user = userEntity.get();
            if(passwordEncoder.matches(password, user.getPassword())){
                return Optional.of(new UserRecord(user.getId().toString(), user.getName(), user.getEmail(), user.getRole().name()));
            }
        } 
        throw new AuthenticationException("Invalid email or password");
    }

    @Cacheable(cacheNames = "users")
    public List<UserRecord> getUsers() {
        return userRepository.findAll().stream()
        .map(u -> new UserRecord(
            u.getId() != null ? u.getId().toString() : null,
            u.getName(),
            u.getEmail(),
            u.getRole() != null ? u.getRole().name() : null
        ))
        .toList();
    }

    // public boolean loginUser(String email, String password) throws AuthenticationException {

    //     Optional<UserRecord> user = userRepository.findByEmail(email);
    //     if (user.isEmpty()) {
    //         throw new AuthenticationException("Invalid email or password");
    //     } else {
    //         // SessionManager.getInstance().login(user);
    //         return true;
    //     }
    // }
}
