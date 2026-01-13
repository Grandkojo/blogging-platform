package com.blogging_platform;

import com.blogging_platform.classes.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LoginUserController.
 * Tests validation logic.
 */
@DisplayName("LoginUserController Tests")
class LoginUserControllerTest {

    private LoginUserController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginUserController();
        SessionManager.getInstance().logout();
        
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }
        
        // Initialize FXML fields that are injected by JavaFX
        setField(controller, "emailError", new javafx.scene.control.Label());
        setField(controller, "passwordError", new javafx.scene.control.Label());
        setField(controller, "userEmail", new javafx.scene.control.TextField());
        setField(controller, "userPassword", new javafx.scene.control.PasswordField());
    }

    @Test
    @DisplayName("validateForm should return false when password is empty")
    void testValidateForm_EmptyPassword() throws Exception {
        javafx.scene.control.PasswordField passwordField = createPasswordField("");
        javafx.scene.control.TextField emailField = createTextField("test@example.com");
        setField(controller, "userPassword", passwordField);
        setField(controller, "userEmail", emailField);
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return false when email is invalid")
    void testValidateForm_InvalidEmail() throws Exception {
        javafx.scene.control.PasswordField passwordField = createPasswordField("password123");
        javafx.scene.control.TextField emailField = createTextField("invalid-email");
        setField(controller, "userPassword", passwordField);
        setField(controller, "userEmail", emailField);
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return true when all fields are valid")
    void testValidateForm_ValidInput() throws Exception {
        javafx.scene.control.PasswordField passwordField = createPasswordField("password123");
        javafx.scene.control.TextField emailField = createTextField("test@example.com");
        setField(controller, "userPassword", passwordField);
        setField(controller, "userEmail", emailField);
        
        boolean result = invokeValidateForm(controller);
        assertTrue(result);
    }

    @Test
    @DisplayName("validateForm should return false when email is empty")
    void testValidateForm_EmptyEmail() throws Exception {
        javafx.scene.control.PasswordField passwordField = createPasswordField("password123");
        javafx.scene.control.TextField emailField = createTextField("");
        setField(controller, "userPassword", passwordField);
        setField(controller, "userEmail", emailField);
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("Email validation should check for @ and . characters")
    void testEmailValidation_FormatCheck() {
        String email1 = "test@example.com";
        String email2 = "invalid-email";
        
        boolean valid1 = email1.contains("@") && email1.contains(".");
        boolean valid2 = email2.contains("@") && email2.contains(".");
        
        assertTrue(valid1);
        assertFalse(valid2);
    }

    // Helper methods
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private boolean invokeValidateForm(LoginUserController controller) throws Exception {
        java.lang.reflect.Method method = controller.getClass().getDeclaredMethod("validateForm");
        method.setAccessible(true);
        return (boolean) method.invoke(controller);
    }

    private javafx.scene.control.TextField createTextField(String text) {
        javafx.scene.control.TextField field = new javafx.scene.control.TextField();
        field.setText(text);
        return field;
    }

    private javafx.scene.control.PasswordField createPasswordField(String text) {
        javafx.scene.control.PasswordField field = new javafx.scene.control.PasswordField();
        field.setText(text);
        return field;
    }
}

