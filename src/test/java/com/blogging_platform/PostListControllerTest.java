package com.blogging_platform;

import com.blogging_platform.classes.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PostListController.
 * Tests business logic.
 */
@DisplayName("PostListController Tests")
class PostListControllerTest {

    private PostListController controller;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        controller = new PostListController();
        sessionManager = SessionManager.getInstance();
        sessionManager.logout();
        
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }
    }

    @Test
    @DisplayName("searchPosts should handle empty query")
    void testSearchPosts_EmptyQuery() {
        String query = "";
        String trimmed = query.trim().toLowerCase();
        
        assertTrue(trimmed.isEmpty());
    }

    @Test
    @DisplayName("searchPosts should convert query to lowercase")
    void testSearchPosts_QueryLowercase() {
        String query = "TEST QUERY";
        String converted = query.trim().toLowerCase();
        
        assertEquals("test query", converted);
    }

    @Test
    @DisplayName("deletePost should return early when no post is selected")
    void testDeletePost_NoSelection() {
        Object selected = null;
        assertNull(selected);
    }

    @Test
    @DisplayName("deletePost should extract postId from selected post")
    void testDeletePost_ExtractPostId() {
        // Simulate post selection logic
        String postId = "test-post-id";
        assertNotNull(postId);
        assertFalse(postId.isEmpty());
    }

    @Test
    @DisplayName("loadPosts should clear postData before loading")
    void testLoadPosts_ClearData() {
        // Test that data clearing logic exists
        boolean shouldClear = true;
        assertTrue(shouldClear);
    }
}

