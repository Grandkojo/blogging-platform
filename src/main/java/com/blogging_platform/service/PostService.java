package com.blogging_platform.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.AuthorizationException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.exceptions.UserNotFoundException;
import com.blogging_platform.exceptions.ValidationException;
import com.blogging_platform.model.Post;
import com.blogging_platform.model.Tag;
import com.blogging_platform.repository.CommentRepository;
import com.blogging_platform.repository.PostRepository;
import com.blogging_platform.repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Application service for blog posts.
 * normalizes
 * publish status (PUBLISHED vs DRAFT) when creating or updating posts.
 */
@Service
@Transactional(rollbackOn = { com.blogging_platform.exceptions.DatabaseQueryException.class,
        com.blogging_platform.exceptions.PostNotFoundException.class })
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    @Autowired
    private TagService tagService;
    @Autowired
    private NotificationService notificationService;

    /** Creates a post service with the given DAO. */
    public PostService(PostRepository postRepository, UserRepository userRepository,
            CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Creates a new post and returns its id. Sets isPublish from status.
     * Wrapped in a transaction to ensure the insert and any related changes
     * are committed atomically.
     *
     * @param post the post to create
     * @return the new post's id
     * @throws DatabaseQueryException if the insert fails
     */
    @CacheEvict(cacheNames = { "posts", "postsById" }, allEntries = true)
    public void createPost(Post post) throws DatabaseQueryException {
        UUID userUuid = post.getUserId();
        if (userUuid == null) {
            throw new ValidationException("userId is required");
        }
        if (userRepository.existsById(Objects.requireNonNull(userUuid, "userId is required"))) {
            if ("PUBLISH".equalsIgnoreCase(post.getStatus())) {
                post.setIsPublish(true);
                post.setStatus("PUBLISHED");
            } else {
                post.setIsPublish(false);
                post.setStatus("DRAFT");
            }
        }
        postRepository.save(post);

        // Asynchronously notify subscribers (mock)
        notificationService.sendNotification("subscribers@example.com", "New post created: " + post.getTitle());
    }

    /**
     * Returns all posts for a given user (e.g. admin list).
     *
     * @param userId user id
     * @return list of post records
     * @throws DatabaseQueryException if the query fails
     */
    // public List<PostRecord> getUserPosts(String userId) throws
    // DatabaseQueryException {
    // return postDAO.getAll(userId);
    // }

    /**
     * Fetches a post by id for a specific user (ownership check).
     *
     * @param postId post id
     * @param userId user id (must own the post)
     * @return the post record
     * @throws PostNotFoundException  if the post does not exist or user does not
     *                                own it
     * @throws DatabaseQueryException if the query fails
     */
    public PostRecord getPost(String postId, String userId) throws DatabaseQueryException, PostNotFoundException {
        if (postId == null || userId == null) {
            throw new ValidationException("postId and userId are required");
        }
        UUID ownerId = Objects.requireNonNull(UUID.fromString(userId), "userId is required");
        UUID postUuid = Objects.requireNonNull(UUID.fromString(postId), "postId is required");
        Post post = postRepository.findByIdAndUser_Id(postUuid, ownerId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return new PostRecord(
                post.getId() != null ? post.getId().toString() : null,
                post.getTitle(),
                post.getContent(),
                post.getStatus(),
                post.getUser() != null ? post.getUser().getName() : null,
                post.getCreatedAt(),
                post.getPublishedDatetime(),
                (int) commentRepository.countByPost_Id(postUuid),
                post.getUser() != null && post.getUser().getId() != null ? post.getUser().getId().toString() : null,
                null);
    }

    /**
     * Fetches a post by id (no ownership check).
     *
     * @param postId post id
     * @return the post record
     * @throws PostNotFoundException  if the post does not exist
     * @throws DatabaseQueryException if the query fails
     */
    @Cacheable(cacheNames = "postsById", key = "#postId")
    public PostRecord getPost(String postId) throws DatabaseQueryException, PostNotFoundException {
        if (postId == null) {
            throw new ValidationException("postId is required");
        }
        UUID postUuid = Objects.requireNonNull(UUID.fromString(postId), "postId is required");

        Post p = postRepository.findByIdWithDetails(postUuid)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Use CompletableFuture to fetch data in parallel
        CompletableFuture<Integer> commentCountFuture = CompletableFuture
                .supplyAsync(
                        () -> (int) commentRepository.countByPost_Id(postUuid), notificationService.taskExecutor());

        CompletableFuture<List<String>> tagsFuture = CompletableFuture
                .supplyAsync(
                        () -> resolveTagsForPost(postId), notificationService.taskExecutor());

        try {
            return new PostRecord(
                    p.getId() != null ? p.getId().toString() : null,
                    p.getTitle(),
                    p.getContent(),
                    p.getStatus(),
                    p.getUser() != null ? p.getUser().getName() : null,
                    p.getCreatedAt(),
                    p.getPublishedDatetime(),
                    commentCountFuture.get(), // Wait for parallel tasks
                    p.getUser() != null && p.getUser().getId() != null ? p.getUser().getId().toString() : null,
                    tagsFuture.get());
        } catch (Exception e) {
            throw new DatabaseQueryException("Failed to fetch post details in parallel", e);
        }
    }

    public boolean existsById(String postId) {
        if (postId == null) {
            throw new ValidationException("postId is required");
        }
        return postRepository.existsById(Objects.requireNonNull(UUID.fromString(postId), "postId is required"));
    }

    /**
     * Returns all published posts.
     * 
     * @param pagination
     * @param query
     *
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    @Cacheable(cacheNames = "posts", key = "'all'")
    public List<PostRecord> getPosts(String query, Pageable pagination) throws DatabaseQueryException {
        if (pagination == null) {
            // Unpaged query
            if (query == null || query.isBlank()) {
                return mapToRecords(postRepository.findAll());
            }
            return mapToRecords(postRepository.searchByTitleAuthorOrTag(query, Pageable.unpaged()).getContent());
        }

        // Paged query
        if (query == null || query.isBlank()) {
            return mapToRecords(postRepository.findAll(pagination).getContent());
        }
        return mapToRecords(postRepository.searchByTitleAuthorOrTag(query, pagination).getContent());
    }

    /**
     * Returns all published posts without pagination.
     *
     * <p>
     * This convenience overload exists for legacy call sites and tests.
     * </p>
     *
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    public List<PostRecord> getPosts() throws DatabaseQueryException {
        return getPosts((Pageable) null);
    }

    /**
     * Returns published posts paginated and optionally filtered by a free-text
     * query
     * matching title, author name, or tag name.
     *
     * @param query    optional search string; if null/blank all posts are returned
     * @param pageable pagination and sorting information
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    @Cacheable(cacheNames = "posts", key = "T(java.util.Objects).hash(#pageable.pageNumber, #pageable.pageSize, #pageable.sort)")
    public List<PostRecord> getPosts(Pageable pageable) throws DatabaseQueryException {
        return getPosts(null, pageable);
    }

    /**
     * Updates an existing post. Sets isPublish from status.
     *
     * @param post the post with updated fields
     * @throws PostNotFoundException  if the post does not exist
     * @throws DatabaseQueryException if the update fails
     */
    @Transactional
    @CacheEvict(cacheNames = { "posts", "postsById" }, allEntries = true)
    public void updatePost(Post post, String postId)
            throws DatabaseQueryException, PostNotFoundException, UserNotFoundException, AuthorizationException {
        UUID userUuid = post.getUserId();
        if (postId == null) {
            throw new ValidationException("postId is required");
        }
        if (userUuid == null) {
            throw new ValidationException("userId is required");
        }
        UUID postUuid = Objects.requireNonNull(UUID.fromString(postId), "postId is required");
        if (!postRepository.existsById(postUuid)) {
            throw new PostNotFoundException("Post with id '" + postId + "' not found.", null);
        }
        if (!userRepository.existsById(userUuid)) {
            throw new UserNotFoundException("User with id '" + userUuid + "' not found.", null);
        }
        Post existing = postRepository.findById(postUuid)
                .orElseThrow(() -> new PostNotFoundException("Post with id '" + postId + "' not found.", null));
        if (!existing.getUserId().equals(userUuid)) {
            throw new AuthorizationException("User is not the author of this post.");
        }
        if ("PUBLISH".equalsIgnoreCase(post.getStatus())) {
            post.setIsPublish(true);
            post.setStatus("PUBLISHED");
        } else {
            post.setIsPublish(false);
            post.setStatus("DRAFT");
        }
        post.setId(postUuid);
        postRepository.save(post);
    }

    /**
     * Deletes a post. Only the owner can delete.
     *
     * @param postId post id
     * @param userId user id (must own the post)
     * @throws PostNotFoundException  if the post does not exist or user does not
     *                                own it
     * @throws DatabaseQueryException if the delete fails
     */
    @CacheEvict(cacheNames = { "posts", "postsById" }, allEntries = true)
    public void deletePost(String postId, String userId)
            throws DatabaseQueryException, PostNotFoundException, UserNotFoundException, AuthorizationException {
        if (postId == null || userId == null) {
            throw new ValidationException("postId and userId are required");
        }
        UUID postUuid = Objects.requireNonNull(UUID.fromString(postId), "postId is required");
        UUID userUuid = Objects.requireNonNull(UUID.fromString(userId), "userId is required");
        if (!postRepository.existsById(postUuid)) {
            throw new PostNotFoundException("Post with id '" + postId + "' not found.", null);
        }
        if (!userRepository.existsById(userUuid)) {
            throw new UserNotFoundException("User with id '" + userUuid + "' not found.", null);
        }
        Post post = postRepository.findById(postUuid)
                .orElseThrow(() -> new PostNotFoundException("Post with id '" + postId + "' not found.", null));
        if (!post.getUserId().equals(userUuid)) {
            throw new AuthorizationException("User is not the author of this post.");
        }
        postRepository.delete(post);
    }

    /**
     * Enriches a single post record with its tags.
     */
    public PostRecord toPostWithTags(PostRecord post) {
        if (post == null) {
            return null;
        }
        return new PostRecord(
                post.id(),
                post.title(),
                post.content(),
                post.status(),
                post.author(),
                post.createdAt(),
                post.publishedDate(),
                post.commentCount(),
                post.userId(),
                resolveTagsForPost(post.id()));
    }

    /**
     * Resolves tag names for a given post id using {@link TagService}.
     */
    private List<String> resolveTagsForPost(String postId) {
        try {
            List<TagRecord> tags = tagService.getTagsByPostId(postId);
            if (tags == null) {
                return List.of();
            }
            return tags.stream()
                    .map(TagRecord::tag)
                    .toList();
        } catch (DatabaseQueryException e) {
            // On error, return empty tags list rather than failing the whole request
            return List.of();
        }
    }

    private List<PostRecord> mapToRecords(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        // Collect all post IDs to fetch comment counts in bulk
        List<UUID> postIds = posts.stream()
                .filter(Objects::nonNull)
                .map(Post::getId)
                .filter(Objects::nonNull)
                .toList();

        // Fetch comment counts: List of Object[] {post_id, count}
        Map<UUID, Long> countsMap = commentRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));

        return posts.stream()
                .filter(Objects::nonNull)
                .map(p -> {
                    UUID postId = p.getId();
                    int commentCount = postId != null ? countsMap.getOrDefault(postId, 0L).intValue() : 0;

                    // Map tags directly from the entity (fetched via JOIN FETCH in repository)
                    List<String> tagNames = p.getTags() != null
                            ? p.getTags().stream().map(Tag::getTag).toList()
                            : List.of();

                    return new PostRecord(
                            postId != null ? postId.toString() : null,
                            p.getTitle(),
                            p.getContent(),
                            p.getStatus(),
                            p.getUser() != null ? p.getUser().getName() : null,
                            p.getCreatedAt(),
                            p.getPublishedDatetime(),
                            commentCount,
                            p.getUser() != null && p.getUser().getId() != null ? p.getUser().getId().toString() : null,
                            tagNames);
                })
                .toList();
    }
}
