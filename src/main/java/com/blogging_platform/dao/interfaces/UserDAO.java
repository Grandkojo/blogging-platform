package com.blogging_platform.dao.interfaces;

import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.model.User;

/**
 * Data access interface for users. Handles registration, login, and email existence checks.
 */
public interface UserDAO {

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Registers a new user.
     *
     * @param user the user to register (name, email, password, role)
     */
    void register(User user);

    /**
     * Validates credentials and returns the user record if successful.
     *
     * @param email    user email
     * @param password user password
     * @return the user record (id, name, email, role), or null if credentials are invalid
     */
    UserRecord login(String email, String password);

    /** Clears any server-side session state (if applicable). */
    void logout();
}
