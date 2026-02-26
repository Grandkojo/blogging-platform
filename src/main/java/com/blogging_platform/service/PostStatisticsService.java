package com.blogging_platform.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

/**
 * Service for tracking post statistics in real-time.
 * Uses thread-safe data structures to handle concurrent updates.
 */
@Service
public class PostStatisticsService {

    private final ConcurrentHashMap<String, AtomicLong> postViews = new ConcurrentHashMap<>();

    /**
     * Increments the view count for a specific post.
     *
     * @param postId the ID of the post
     */
    public void incrementViews(String postId) {
        postViews.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Gets the current view count for a post.
     *
     * @param postId the ID of the post
     * @return the current view count
     */
    public long getViews(String postId) {
        AtomicLong views = postViews.get(postId);
        return views != null ? views.get() : 0;
    }

    /**
     * Resets the view count (e.g., after persisting to DB).
     *
     * @param postId the ID of the post
     */
    public void resetViews(String postId) {
        postViews.remove(postId);
    }
}
