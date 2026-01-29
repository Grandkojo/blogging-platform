package com.blogging_platform.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcPostDAO;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.service.PostService;




public class CacheManager {
    private PostDAO postDAO = new JdbcPostDAO();
    private PostService postService = new PostService(postDAO);

    private static final CacheManager instance = new CacheManager();

    private List<PostRecord> publishedPostsCache = new ArrayList<>();
    private Map<String, PostRecord> postByIdCache = new ConcurrentHashMap<>();

    public static CacheManager getInstance() {
        return instance;
    }

    //load all published posts into cache
    public void refreshCache() throws DatabaseException {
        publishedPostsCache.clear();
        postByIdCache.clear();

        List<PostRecord> posts = postService.getPosts();
        for (PostRecord post : posts) {
            publishedPostsCache.add(post);
            postByIdCache.put(post.id(), post);
        }
    }

    public void refreshCache(String query) throws DatabaseException {
        publishedPostsCache.clear();
        postByIdCache.clear();

        List<PostRecord> posts = postService.getPosts();
        for (PostRecord post : posts) {
            publishedPostsCache.add(post);
            postByIdCache.put(post.id(), post);
        }
    }

    // Fast lookup by ID
    public PostRecord getPostById(String id) {
        return postByIdCache.get(id);
    }

    // Get cached list (for home page) – always returns fresh data after optional refresh
    public List<PostRecord> getPublishedPosts() {
        try {
            refreshCache();
        } catch (DatabaseException e) {
            return new ArrayList<>(publishedPostsCache);
        }
        return new ArrayList<>(publishedPostsCache);
    }

    // Invalidate cache and load fresh data (call after comment/review create, update, delete)
    public void invalidateCache() {
        try {
            publishedPostsCache.clear();
            postByIdCache.clear();
            refreshCache();
        } catch (DatabaseException e) {
            System.err.println("Cache invalidation failed: " + e.getMessage());
        }
    }
} 
