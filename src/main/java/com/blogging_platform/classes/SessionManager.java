package com.blogging_platform.classes;

/**
 * Singleton that holds the currently logged-in user for the duration of the session.
 * Used by controllers to obtain user id, name, email, and role after login.
 */
public class SessionManager {
    private static SessionManager instance;
    private UserRecord currentUser;

    private SessionManager() {}

    /** Returns the singleton instance. */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Sets the current user after successful login.
     *
     * @param user the logged-in user record
     */
    public void login(UserRecord user) {
        this.currentUser = user;
    }

    /** Clears the current user (logout). */
    public void logout() {
        this.currentUser = null;
    }

    /** Returns the current user's id, or null if not logged in. */
    public String getUserId() {
        return currentUser != null ? currentUser.id() : null;
    }

    /** Returns the current user's name, or null if not logged in. */
    public String getUserName() {
        return currentUser != null ? currentUser.name() : null;
    }

    /** Returns the current user's email, or null if not logged in. */
    public String getUserEmail() {
        return currentUser != null ? currentUser.email() : null;
    }

    /** Returns the current user's role, or null if not logged in. */
    public String getUserRole() {
        return currentUser != null ? currentUser.role() : null;
    }

    /** Returns true if a user is currently logged in. */
    public boolean isLoggedIn() {
        return currentUser != null && currentUser.id() != null;
    }

    /** Returns the full current user record, or null if not logged in. */
    public UserRecord getCurrentUser() {
        return this.currentUser;
    }
}
