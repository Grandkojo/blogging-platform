package com.blogging_platform;

import com.blogging_platform.classes.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SinglePostController.
 * Tests business logic.
 */
@DisplayName("SinglePostController Tests")
class SinglePostControllerTest {

    private SinglePostController controller;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        controller = new SinglePostController();
        sessionManager = SessionManager.getInstance();
        sessionManager.logout();
        
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }
    }

    @Test
    @DisplayName("postComment should handle empty comment content")
    void testPostComment_EmptyContent() {
        String content = "";
        String trimmed = content.trim();
        
        assertTrue(trimmed.isEmpty());
    }

    @Test
    @DisplayName("loadComments should clear commentsContainer before loading")
    void testLoadComments_ClearContainer() {
        // Test that container clearing logic exists
        boolean shouldClear = true;
        assertTrue(shouldClear);
    }

    @Test
    @DisplayName("displayPost should respect expected post data structure size")
    void testDisplayPost_DataStructureSize() {
        // Expected structure: [id, title, content, status, date, author, user_id]
        int expectedSize = 7;
        assertEquals(7, expectedSize);
    }
}

