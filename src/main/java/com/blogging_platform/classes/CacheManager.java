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

    private static CacheManager getInstance() {
        return instance;
    }

    //load all published posts into cache
    public void refreshCache() throws DatabaseException {
        List<PostRecord> posts = postService.getPosts();
        for (PostRecord post: posts){
            publishedPostsCache.add(post);
        }

        postByIdCache.clear();

        for (PostRecord post: posts){
            postByIdCache.put(post.id(), post);
        }
    }

    public void refreshCache(String query) throws DatabaseException {
        List<PostRecord> posts = postService.getPosts();

        for (PostRecord post: posts){
            publishedPostsCache.add(post);
        }
        
        postByIdCache.clear();

        for (PostRecord post: posts){
            postByIdCache.put(post.id(), post);
        }
    }

    // Fast lookup by ID
    public PostRecord getPostById(String id) {
        return postByIdCache.get(id);
    }

    // Get cached list (for home page)
    public List<PostRecord> getPublishedPosts() {
        try {
            refreshCache();
        } catch (DatabaseException e) {
            return publishedPostsCache;
        }
        return new ArrayList<>(publishedPostsCache); 
    }

    // Invalidate cache after create/update/delete
    public void invalidateCache() {
        try {
            refreshCache();
        } catch (DatabaseException e) {
           System.out.println("Cache invalidation failed");
        }
    }
} 
