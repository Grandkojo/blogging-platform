package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for resource not found exceptions.
 */
@DisplayName("Resource Not Found Exception Tests")
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("ResourceNotFoundException should have correct error code")
    void testResourceNotFoundException() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("could not be found"));
    }

    @Test
    @DisplayName("ResourceNotFoundException should store resource type and ID")
    void testResourceNotFoundExceptionWithTypeAndId() {
        String resourceType = "Post";
        String resourceId = "post-123";
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);
        
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
        assertTrue(exception.getMessage().contains(resourceType));
        assertTrue(exception.getMessage().contains(resourceId));
    }

    @Test
    @DisplayName("PostNotFoundException should extend ResourceNotFoundException")
    void testPostNotFoundException() {
        String postId = "post-123";
        PostNotFoundException exception = new PostNotFoundException(postId);
        
        assertTrue(exception instanceof ResourceNotFoundException);
        assertEquals("Post", exception.getResourceType());
        assertEquals(postId, exception.getResourceId());
    }

    @Test
    @DisplayName("CommentNotFoundException should extend ResourceNotFoundException")
    void testCommentNotFoundException() {
        String commentId = "comment-456";
        CommentNotFoundException exception = new CommentNotFoundException(commentId);
        
        assertTrue(exception instanceof ResourceNotFoundException);
        assertEquals("Comment", exception.getResourceType());
        assertEquals(commentId, exception.getResourceId());
    }

    @Test
    @DisplayName("UserNotFoundException should extend ResourceNotFoundException")
    void testUserNotFoundExceptionWithId() {
        String userId = "user-789";
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        assertTrue(exception instanceof ResourceNotFoundException);
        assertEquals("User", exception.getResourceType());
        assertEquals(userId, exception.getResourceId());
    }

    @Test
    @DisplayName("UserNotFoundException should handle email lookup")
    void testUserNotFoundExceptionWithEmail() {
        String email = "test@example.com";
        UserNotFoundException exception = UserNotFoundException.forEmail(email);
        
        assertTrue(exception.getMessage().contains(email));
    }
}

