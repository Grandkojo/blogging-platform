package com.blogging_platform.classes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.blogging_platform.MySQLDriver;




public class CacheManager {
    private static final CacheManager instance = new CacheManager();

    private List<PostRecordF> publishedPostsCache = new ArrayList<>();
    private Map<String, PostRecordF> postByIdCache = new ConcurrentHashMap<>();

    private static CacheManager getInstance() {
        return instance;
    }

    //load all published posts into cache
    public void refreshCache(){
        MySQLDriver sqlDriver = new MySQLDriver();
        ArrayList<String> posts = sqlDriver.getAllPosts();
        List<PostRecordF> postss = null;

        for (int i = 0; i < posts.size(); i += 7) {
            if (i + 6 >= posts.size()) break;
            PostRecordF postRecord = new PostRecordF(posts.get(i + 5), posts.get(i), posts.get(i + 1), posts.get(i + 2), posts.get(i + 3 ), LocalDateTime.parse(posts.get(i + 4)), Integer.parseInt(posts.get(i + 6)));
            publishedPostsCache.add(postRecord);
            postss.add(postRecord);
        }
        postByIdCache.clear();

        for (PostRecordF post: postss){
            postByIdCache.put(post.id(), post);
        }
    }

    public void refreshCache(String query){
        MySQLDriver sqlDriver = new MySQLDriver();
        ArrayList<String> posts = sqlDriver.getAllPosts(query);
        List<PostRecordF> postss = null;

        for (int i = 0; i < posts.size(); i += 7) {
            if (i + 6 >= posts.size()) break;
            PostRecordF postRecord = new PostRecordF(posts.get(i + 5), posts.get(i), posts.get(i + 1), posts.get(i + 2), posts.get(i + 3 ), LocalDateTime.parse(posts.get(i + 4)), Integer.parseInt(posts.get(i + 6)));
            publishedPostsCache.add(postRecord);
            postss.add(postRecord);
        }
        postByIdCache.clear();

        for (PostRecordF post: postss){
            postByIdCache.put(post.id(), post);
        }
    }

    // Fast lookup by ID
    public PostRecordF getPostById(String id) {
        return postByIdCache.get(id);
    }

    // Get cached list (for home page)
    public List<PostRecordF> getPublishedPosts() {
        return new ArrayList<>(publishedPostsCache); 
    }

    // Invalidate cache after create/update/delete
    public void invalidateCache() {
        refreshCache();
    }
} 
