package com.blogging_platform.classes;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}  

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Login: set user data
    public void login(User user) {
        this.currentUser = user;
    }

    // Logout: clear data
    public void logout() {
        this.currentUser = null;
    }

    public String getUserId() {
        return currentUser != null ? currentUser.id() : null;
    }

    public String getUserName() {
        return currentUser != null ? currentUser.name() : null;
    }

    public String getUserEmail() {
        return currentUser != null ? currentUser.email() : null;
    }

    public String getUserRole() {
        return currentUser != null ? currentUser.role() : null;
    }

    // Check if logged in
    public boolean isLoggedIn() {
        return currentUser.id() != null;
    }
}
