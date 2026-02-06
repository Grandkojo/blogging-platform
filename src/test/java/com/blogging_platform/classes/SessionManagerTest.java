package com.blogging_platform.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SessionManager}.
 */
class SessionManagerTest {

    @Test
    void getInstance_returnsSingleton() {
        SessionManager first = SessionManager.getInstance();
        SessionManager second = SessionManager.getInstance();

        assertSame(first, second, "getInstance should always return the same instance");
    }

    @Test
    void login_setsCurrentUser_andIsLoggedInTrue() {
        SessionManager manager = SessionManager.getInstance();
        UserRecord user = new UserRecord("id-1", "John Doe", "john@example.com", "Regular");

        manager.logout(); // ensure clean state
        manager.login(user);

        assertTrue(manager.isLoggedIn());
        assertEquals("id-1", manager.getUserId());
        assertEquals("John Doe", manager.getUserName());
        assertEquals("john@example.com", manager.getUserEmail());
        assertEquals("Regular", manager.getUserRole());
        assertSame(user, manager.getCurrentUser());
    }

    @Test
    void logout_clearsCurrentUser_andIsLoggedInFalse() {
        SessionManager manager = SessionManager.getInstance();
        UserRecord user = new UserRecord("id-1", "John Doe", "john@example.com", "Regular");

        manager.login(user);
        manager.logout();

        assertFalse(manager.isLoggedIn());
        assertNull(manager.getUserId());
        assertNull(manager.getUserName());
        assertNull(manager.getUserEmail());
        assertNull(manager.getUserRole());
        assertNull(manager.getCurrentUser());
    }

    @Test
    void isLoggedIn_falseWhenNoUserId() {
        SessionManager manager = SessionManager.getInstance();
        // user with null id should not be considered logged in
        UserRecord user = new UserRecord(null, "John Doe", "john@example.com", "Regular");

        manager.login(user);

        assertFalse(manager.isLoggedIn());
    }
}

