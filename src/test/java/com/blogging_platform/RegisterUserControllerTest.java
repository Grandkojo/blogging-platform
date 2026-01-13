package com.blogging_platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RegisterUserController.
 * Tests validation logic.
 */
@DisplayName("RegisterUserController Tests")
class RegisterUserControllerTest {

    private RegisterUserController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new RegisterUserController();
        
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }
        
        // Initialize FXML fields that are injected by JavaFX
        setField(controller, "emailError", new javafx.scene.control.Label());
        setField(controller, "passwordError", new javafx.scene.control.Label());
        setField(controller, "nameError", new javafx.scene.control.Label());
        setField(controller, "roleError", new javafx.scene.control.Label());
        setField(controller, "userName", new javafx.scene.control.TextField());
        setField(controller, "userEmail", new javafx.scene.control.TextField());
        setField(controller, "userPassword", new javafx.scene.control.PasswordField());
        setField(controller, "userRole", new javafx.scene.control.ComboBox<>());
    }

    @Test
    @DisplayName("validateForm should return false when name is empty")
    void testValidateForm_EmptyName() throws Exception {
        setField(controller, "userName", createTextField(""));
        setField(controller, "userEmail", createTextField("test@example.com"));
        setField(controller, "userPassword", createPasswordField("password123"));
        setField(controller, "userRole", createComboBox("Admin"));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return false when email is invalid")
    void testValidateForm_InvalidEmail() throws Exception {
        setField(controller, "userName", createTextField("John Doe"));
        setField(controller, "userEmail", createTextField("invalid-email"));
        setField(controller, "userPassword", createPasswordField("password123"));
        setField(controller, "userRole", createComboBox("Admin"));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return false when role is not selected")
    void testValidateForm_NoRole() throws Exception {
        setField(controller, "userName", createTextField("John Doe"));
        setField(controller, "userEmail", createTextField("test@example.com"));
        setField(controller, "userPassword", createPasswordField("password123"));
        setField(controller, "userRole", createComboBox(null));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return true when all fields are valid")
    void testValidateForm_ValidInput() throws Exception {
        setField(controller, "userName", createTextField("John Doe"));
        setField(controller, "userEmail", createTextField("test@example.com"));
        setField(controller, "userPassword", createPasswordField("password123"));
        setField(controller, "userRole", createComboBox("Admin"));
        
        boolean result = invokeValidateForm(controller);
        assertTrue(result);
    }

    @Test
    @DisplayName("validateForm should return false when password is empty")
    void testValidateForm_EmptyPassword() throws Exception {
        setField(controller, "userName", createTextField("John Doe"));
        setField(controller, "userEmail", createTextField("test@example.com"));
        setField(controller, "userPassword", createPasswordField(""));
        setField(controller, "userRole", createComboBox("Admin"));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    // Helper methods
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private boolean invokeValidateForm(RegisterUserController controller) throws Exception {
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

    private javafx.scene.control.ComboBox<String> createComboBox(String value) {
        javafx.scene.control.ComboBox<String> combo = new javafx.scene.control.ComboBox<>();
        if (value != null) {
            combo.setValue(value);
        }
        return combo;
    }
}

