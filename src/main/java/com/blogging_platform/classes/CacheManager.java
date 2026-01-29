package com.blogging_platform.classes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcPostDAO;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.TagService;

/**
 * In-memory cache for published posts. Uses:
 * - Hashing: postByIdCache (ConcurrentHashMap) for O(1) lookup by id – analogous to a DB hash index.
 * - Caching: publishedPostsCache holds the full list; search/sort run on this in memory instead of querying the DB.
 * - Sorting: QuickSort for ordering results (relates to in-memory ordering like indexed DB sort).
 */
public class CacheManager {
    private PostDAO postDAO = new JdbcPostDAO();
    private PostService postService = new PostService(postDAO);

    private static final CacheManager instance = new CacheManager();

    private List<PostRecord> publishedPostsCache = new ArrayList<>();
    /** Hash index for O(1) lookup by post id – similar to a database primary-key index. */
    private Map<String, PostRecord> postByIdCache = new ConcurrentHashMap<>();
    /** Post id -> tag names for in-memory search by tag (populated at refresh if tagService set). */
    private Map<String, List<String>> postIdToTagNames = new ConcurrentHashMap<>();

    private TagService tagService;

    public static CacheManager getInstance() {
        return instance;
    }

    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    /** Load all published posts into cache and build tag index for search-by-tag. */
    public void refreshCache() throws DatabaseException {
        publishedPostsCache.clear();
        postByIdCache.clear();
        postIdToTagNames.clear();

        List<PostRecord> posts = postService.getPosts();
        for (PostRecord post : posts) {
            publishedPostsCache.add(post);
            postByIdCache.put(post.id(), post);
            if (tagService != null) {
                try {
                    List<TagRecord> tags = tagService.getTagsByPostId(post.id());
                    List<String> names = new ArrayList<>();
                    if (tags != null) for (TagRecord t : tags) names.add(t.tag());
                    postIdToTagNames.put(post.id(), names);
                } catch (DatabaseQueryException e) {
                    postIdToTagNames.put(post.id(), List.of());
                }
            }
        }
    }

    /** Fast lookup by ID (hash index – O(1)). */
    public PostRecord getPostById(String id) {
        return postByIdCache.get(id);
    }

    /** Get a copy of the cached list (refreshes from DB first). */
    public List<PostRecord> getPublishedPosts() {
        try {
            refreshCache();
        } catch (DatabaseException e) {
            return new ArrayList<>(publishedPostsCache);
        }
        return new ArrayList<>(publishedPostsCache);
    }

    /**
     * In-memory search: filter cached posts by title, author, or tag (case-insensitive contains).
     * No DB query – uses the cache and tag index.
     */
    public List<PostRecord> searchPosts(String query) {
        if (query == null || query.trim().isEmpty())
            return new ArrayList<>(publishedPostsCache);
        String q = query.trim().toLowerCase();
        List<PostRecord> result = new ArrayList<>();
        for (PostRecord p : publishedPostsCache) {
            if (matchesSearch(p, q)) result.add(p);
        }
        return result;
    }

    private boolean matchesSearch(PostRecord p, String q) {
        if (p.title() != null && p.title().toLowerCase().contains(q)) return true;
        if (p.author() != null && p.author().toLowerCase().contains(q)) return true;
        List<String> tags = postIdToTagNames.get(p.id());
        if (tags != null) for (String t : tags) if (t != null && t.toLowerCase().contains(q)) return true;
        return false;
    }

    /**
     * Sort list in place using QuickSort (O(n log n) average). sortBy: "date_desc", "date_asc", "title_asc", "title_desc", "author_asc", "author_desc".
     */
    public void sortPosts(List<PostRecord> list, String sortBy) {
        if (list == null || list.isEmpty()) return;
        Comparator<PostRecord> cmp = comparatorFor(sortBy);
        if (cmp != null) quickSort(list, 0, list.size() - 1, cmp);
    }

    private Comparator<PostRecord> comparatorFor(String sortBy) {
        if (sortBy == null) return (a, b) -> compareDateDesc(a.publishedDate(), b.publishedDate());
        switch (sortBy) {
            case "date_asc":
                return Comparator.comparing(PostRecord::publishedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "date_desc":
                return Comparator.comparing(PostRecord::publishedDate, Comparator.nullsLast(Comparator.reverseOrder()));
            case "title_asc":
                return Comparator.comparing(p -> p.title() != null ? p.title().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER);
            case "title_desc":
                return (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                    b.title() != null ? b.title() : "",
                    a.title() != null ? a.title() : "");
            case "author_asc":
                return Comparator.comparing(p -> p.author() != null ? p.author().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER);
            case "author_desc":
                return (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                    b.author() != null ? b.author() : "",
                    a.author() != null ? a.author() : "");
            default:
                return Comparator.comparing(PostRecord::publishedDate, Comparator.nullsLast(Comparator.reverseOrder()));
        }
    }

    private static int compareDateDesc(java.time.LocalDateTime a, java.time.LocalDateTime b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return b.compareTo(a);
    }

    /** QuickSort: partition and recurse – in-memory sort analogous to DB ORDER BY. */
    private static void quickSort(List<PostRecord> list, int low, int high, Comparator<PostRecord> cmp) {
        if (low < high) {
            int pi = partition(list, low, high, cmp);
            quickSort(list, low, pi - 1, cmp);
            quickSort(list, pi + 1, high, cmp);
        }
    }

    private static int partition(List<PostRecord> list, int low, int high, Comparator<PostRecord> cmp) {
        PostRecord pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static void swap(List<PostRecord> list, int i, int j) {
        PostRecord t = list.get(i);
        list.set(i, list.get(j));
        list.set(j, t);
    }

    /**
     * Search (in-memory from cache) then sort (QuickSort). Use this for home page list.
     * sortBy: "date_desc", "date_asc", "title_asc", "title_desc", "author_asc", "author_desc".
     */
    public List<PostRecord> getPublishedPostsSearchAndSort(String query, String sortBy) {
        try {
            refreshCache();
        } catch (DatabaseException e) {
            // use stale cache
        }
        List<PostRecord> list = searchPosts(query);
        sortPosts(list, sortBy);
        return list;
    }

    public void invalidateCache() {
        try {
            publishedPostsCache.clear();
            postByIdCache.clear();
            postIdToTagNames.clear();
            refreshCache();
        } catch (DatabaseException e) {
            System.err.println("Cache invalidation failed: " + e.getMessage());
        }
    }
} 
