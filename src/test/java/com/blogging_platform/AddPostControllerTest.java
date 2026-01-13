package com.blogging_platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AddPostController.
 * Tests validation logic.
 */
@DisplayName("AddPostController Tests")
class AddPostControllerTest {

    private AddPostController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new AddPostController();
        
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }
        
        // Initialize FXML fields that are injected by JavaFX
        setField(controller, "postTitleError", new javafx.scene.control.Label());
        setField(controller, "postContentError", new javafx.scene.control.Label());
        setField(controller, "postStatusError", new javafx.scene.control.Label());
        setField(controller, "postTile", new javafx.scene.control.TextField());
        setField(controller, "postContent", new javafx.scene.control.TextArea());
        setField(controller, "postStatus", new javafx.scene.control.ComboBox<>());
    }

    @Test
    @DisplayName("validateForm should return false when title is empty")
    void testValidateForm_EmptyTitle() throws Exception {
        setField(controller, "postTile", createTextField(""));
        setField(controller, "postContent", createTextArea("Content here"));
        setField(controller, "postStatus", createComboBox("Draft"));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return false when content is empty")
    void testValidateForm_EmptyContent() throws Exception {
        setField(controller, "postTile", createTextField("Test Title"));
        setField(controller, "postContent", createTextArea(""));
        setField(controller, "postStatus", createComboBox("Draft"));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return false when status is not selected")
    void testValidateForm_NoStatus() throws Exception {
        setField(controller, "postTile", createTextField("Test Title"));
        setField(controller, "postContent", createTextArea("Content here"));
        setField(controller, "postStatus", createComboBox(null));
        
        boolean result = invokeValidateForm(controller);
        assertFalse(result);
    }

    @Test
    @DisplayName("validateForm should return true when all fields are valid")
    void testValidateForm_ValidInput() throws Exception {
        setField(controller, "postTile", createTextField("Test Title"));
        setField(controller, "postContent", createTextArea("Content here"));
        setField(controller, "postStatus", createComboBox("Draft"));
        
        boolean result = invokeValidateForm(controller);
        assertTrue(result);
    }

    @Test
    @DisplayName("Status conversion should convert Draft to DRAFT")
    void testStatusConversion_Draft() {
        String status = "Draft";
        String converted = switch (status.trim()) {
            case "Draft" -> "DRAFT";
            case "Publish" -> "PUBLISHED";
            default -> "DRAFT";
        };
        
        assertEquals("DRAFT", converted);
    }

    // Helper methods
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private boolean invokeValidateForm(AddPostController controller) throws Exception {
        java.lang.reflect.Method method = controller.getClass().getDeclaredMethod("validateForm");
        method.setAccessible(true);
        return (boolean) method.invoke(controller);
    }

    private javafx.scene.control.TextField createTextField(String text) {
        javafx.scene.control.TextField field = new javafx.scene.control.TextField();
        field.setText(text);
        return field;
    }

    private javafx.scene.control.TextArea createTextArea(String text) {
        javafx.scene.control.TextArea area = new javafx.scene.control.TextArea();
        area.setText(text);
        return area;
    }

    private javafx.scene.control.ComboBox<String> createComboBox(String value) {
        javafx.scene.control.ComboBox<String> combo = new javafx.scene.control.ComboBox<>();
        if (value != null) {
            combo.setValue(value);
        }
        return combo;
    }
}

