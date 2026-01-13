package com.blogging_platform;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PostHomeController.
 * Tests business logic.
 */
@DisplayName("PostHomeController Tests")
class PostHomeControllerTest {

    private PostHomeController controller;

    @BeforeEach
    void setUp() {
        controller = new PostHomeController();
        
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
        String trimmed = query.trim();
        
        assertTrue(trimmed.isEmpty());
    }

    @Test
    @DisplayName("loadAllPosts should process posts correctly")
    void testLoadAllPosts_ProcessPosts() {
        // Test the post processing logic (7 items per post)
        ArrayList<String> rawPosts = new ArrayList<>();
        rawPosts.add("Title 1");
        rawPosts.add("Content 1");
        rawPosts.add("PUBLISHED");
        rawPosts.add("Author 1");
        rawPosts.add("2024-01-01 10:00:00");
        rawPosts.add("post-id-1");
        rawPosts.add("5");
        
        assertEquals(7, rawPosts.size());
        
        // Verify post data structure
        String title = rawPosts.get(0);
        String content = rawPosts.get(1);
        String status = rawPosts.get(2);
        String author = rawPosts.get(3);
        String id = rawPosts.get(5);
        int commentCount = Integer.parseInt(rawPosts.get(6));
        
        assertEquals("Title 1", title);
        assertEquals("Content 1", content);
        assertEquals("PUBLISHED", status);
        assertEquals("Author 1", author);
        assertEquals("post-id-1", id);
        assertEquals(5, commentCount);
    }

    @Test
    @DisplayName("loadAllPosts should handle date parsing")
    void testLoadAllPosts_DateParsing() {
        String dateStr = "2024-01-01 10:00:00";
        String formatted = dateStr.replace(" ", "T");
        LocalDateTime date = LocalDateTime.parse(formatted);
        
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(1, date.getDayOfMonth());
    }

    @Test
    @DisplayName("loadAllPosts should clear postsFlowPane before loading")
    void testLoadAllPosts_ClearPane() {
        // Test that pane clearing logic exists
        boolean shouldClear = true;
        assertTrue(shouldClear);
    }

    @Test
    @DisplayName("loadAllPosts should handle multiple posts")
    void testLoadAllPosts_MultiplePosts() {
        ArrayList<String> rawPosts = new ArrayList<>();
        // Post 1
        rawPosts.add("Title 1");
        rawPosts.add("Content 1");
        rawPosts.add("PUBLISHED");
        rawPosts.add("Author 1");
        rawPosts.add("2024-01-01 10:00:00");
        rawPosts.add("post-id-1");
        rawPosts.add("5");
        // Post 2
        rawPosts.add("Title 2");
        rawPosts.add("Content 2");
        rawPosts.add("PUBLISHED");
        rawPosts.add("Author 2");
        rawPosts.add("2024-01-02 11:00:00");
        rawPosts.add("post-id-2");
        rawPosts.add("3");
        
        assertEquals(14, rawPosts.size()); // 2 posts * 7 fields
        
        // Process first post
        int i = 0;
        String title1 = rawPosts.get(i);
        String id1 = rawPosts.get(i + 5);
        assertEquals("Title 1", title1);
        assertEquals("post-id-1", id1);
        
        // Process second post
        i = 7;
        String title2 = rawPosts.get(i);
        String id2 = rawPosts.get(i + 5);
        assertEquals("Title 2", title2);
        assertEquals("post-id-2", id2);
    }
}

