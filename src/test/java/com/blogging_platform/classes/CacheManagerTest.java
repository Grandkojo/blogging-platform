package com.blogging_platform.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.TagService;

/**
 * Unit tests for {@link CacheManager}.
 *
 * These tests focus on in-memory behaviour (search, sort, pagination, and
 * tag-based filtering) by injecting mocked dependencies and manipulating
 * internal fields via reflection.
 */
class CacheManagerTest {

    @Mock
    private PostService postService;

    @Mock
    private TagService tagService;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        cacheManager = CacheManager.getInstance();

        // Replace internal postService with mocked instance via reflection
        var field = CacheManager.class.getDeclaredField("postService");
        field.setAccessible(true);
        field.set(cacheManager, postService);

        // Reset caches to a known state before each test by clearing and
        // forcing a refresh using a small fixture list.
        List<PostRecord> posts = List.of(
            new PostRecord("1", "Spring Boot Guide", "Content", "PUBLISHED", "Alice",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1)),
            new PostRecord("2", "Java Testing", "Content", "PUBLISHED", "Bob",
                LocalDateTime.now().minusDays(1), LocalDateTime.now()),
            new PostRecord("3", "Tag Searching", "Content", "PUBLISHED", "Carol",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2))
        );
        when(postService.getPosts()).thenReturn(posts);
        cacheManager.setTagService(tagService);

        // When tags are requested, return simple mappings: post 1 has "spring",
        // post 2 has "java", post 3 has "search"
        when(tagService.getTagsByPostId("1")).thenReturn(List.of(new TagRecord("t1", "spring")));
        when(tagService.getTagsByPostId("2")).thenReturn(List.of(new TagRecord("t2", "java")));
        when(tagService.getTagsByPostId("3")).thenReturn(List.of(new TagRecord("t3", "search")));

        cacheManager.refreshCache();
    }

    @Test
    void getInstance_returnsSingleton() {
        CacheManager first = CacheManager.getInstance();
        CacheManager second = CacheManager.getInstance();

        assertTrue(first == second, "CacheManager should be a singleton");
    }

    @Test
    void getPostById_returnsCachedPost() {
        PostRecord post = cacheManager.getPostById("1");

        assertNotNull(post);
        assertEquals("1", post.id());
    }

    @Test
    void getPostById_returnsNullForUnknownId() {
        PostRecord post = cacheManager.getPostById("unknown");

        assertNull(post);
    }

    @Test
    void searchPosts_returnsAllWhenQueryBlank() {
        List<PostRecord> result = cacheManager.searchPosts("  ");

        assertEquals(3, result.size());
    }

    @Test
    void searchPosts_filtersByTitleAuthorAndTags() throws Exception {
        // title match
        List<PostRecord> byTitle = cacheManager.searchPosts("spring");
        assertEquals(1, byTitle.size());
        assertEquals("1", byTitle.get(0).id());

        // author match (case-insensitive)
        List<PostRecord> byAuthor = cacheManager.searchPosts("bob");
        assertEquals(1, byAuthor.size());
        assertEquals("2", byAuthor.get(0).id());

        // tag match uses tagService mapping
        List<PostRecord> byTag = cacheManager.searchPosts("search");
        assertEquals(1, byTag.size());
        assertEquals("3", byTag.get(0).id());
    }

    @Test
    void sortPosts_sortsByDateDescendingByDefault() {
        List<PostRecord> list = cacheManager.getPublishedPosts();

        cacheManager.sortPosts(list, null);

        // With the fixture data, post "2" has the latest publishedDate
        assertEquals("2", list.get(0).id());
    }

    @Test
    void sortPosts_canSortByTitleAscending() {
        List<PostRecord> list = cacheManager.getPublishedPosts();

        cacheManager.sortPosts(list, "title_asc");

        // Alphabetically: "Java Testing", "Spring Boot Guide", "Tag Searching"
        assertEquals("2", list.get(0).id());
        assertEquals("1", list.get(1).id());
        assertEquals("3", list.get(2).id());
    }

    @Test
    void getPaginatedPublishedPosts_appliesSearchSortAndPagination() {
        // Query "java" should match post with id "2"; page size 1
        PagedResult<PostRecord> page = cacheManager.getPaginatedPublishedPosts(0, 1, "java", "date_desc");

        assertEquals(1, page.content().size());
        assertEquals("2", page.content().get(0).id());
        assertEquals(0, page.page());
        assertEquals(1, page.size());
        assertEquals(1, page.totalElements());
    }

    @Test
    void invalidateCache_clearsAndReloadsFromService() throws Exception {
        // Use a different list to verify reload
        List<PostRecord> newPosts = List.of(
            new PostRecord("10", "New Post", "Content", "PUBLISHED", "NewAuthor",
                LocalDateTime.now(), LocalDateTime.now())
        );

        when(postService.getPosts()).thenReturn(newPosts);

        cacheManager.invalidateCache();

        List<PostRecord> cached = cacheManager.getPublishedPosts();
        assertEquals(1, cached.size());
        assertEquals("10", cached.get(0).id());
    }
}

