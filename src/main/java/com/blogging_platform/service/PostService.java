package com.blogging_platform.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    @Autowired
    private TagService tagService;

    /** Creates a post service with the given DAO. */
    public PostService(PostRepository postRepository, UserRepository userRepository,
            CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Creates a new post and returns its id. Sets isPublish from status.
     *
     * @param post the post to create
     * @return the new post's id
     * @throws DatabaseQueryException if the insert fails
     */
    @CacheEvict(cacheNames = { "posts", "postsById" }, allEntries = true)
    public void createPost(Post post) throws DatabaseQueryException {
        UUID userUuid = post.getUserId();
        if (userRepository.existsById(userUuid)) {
            if ("PUBLISH".equalsIgnoreCase(post.getStatus())) {
                post.setIsPublish(true);
                post.setStatus("PUBLISHED");
            } else {
                post.setIsPublish(false);
                post.setStatus("DRAFT");
            }
        }
        postRepository.save(post);
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
        UUID ownerId = UUID.fromString(userId);
        UUID postUuid = UUID.fromString(postId);
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
        UUID postUuid = UUID.fromString(postId);
        PostRecord post = postRepository.findById(postUuid)
                .map(p -> new PostRecord(
                        p.getId() != null ? p.getId().toString() : null,
                        p.getTitle(),
                        p.getContent(),
                        p.getStatus(),
                        p.getUser() != null ? p.getUser().getName() : null,
                        p.getCreatedAt(),
                        p.getPublishedDatetime(),
                        (int) commentRepository.countByPost_Id(postUuid),
                        p.getUser() != null && p.getUser().getId() != null ? p.getUser().getId().toString() : null,
                        null))
                .orElseThrow(() -> new PostNotFoundException(postId));

        return post;
    }

    public boolean existsById(String postId) {
        return postRepository.existsById(UUID.fromString(postId));
    }

    /**
     * Returns all published posts.
     *
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    @Cacheable(cacheNames = "posts", key = "'all'")
    public List<PostRecord> getPosts() throws DatabaseQueryException {
        return postRepository.findAll().stream()
                .map(p -> new PostRecord(
                        p.getId() != null ? p.getId().toString() : null,
                        p.getTitle(),
                        p.getContent(),
                        p.getStatus(),
                        p.getUser() != null ? p.getUser().getName() : null,
                        p.getCreatedAt(),
                        p.getPublishedDatetime(),
                        (int) commentRepository.countByPost_Id(p.getId()),
                        p.getUser() != null && p.getUser().getId() != null ? p.getUser().getId().toString() : null,
                        null))
                .toList();
    }

    /**
     * Returns all published posts paginated.
     *
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    @Cacheable(cacheNames = "posts", key = "T(java.util.Objects).hash(#pageable.pageNumber, #pageable.pageSize, #pageable.sort)")
    public List<PostRecord> getPosts(Pageable pageable) throws DatabaseQueryException {
        return postRepository.findAll(pageable).stream()
                .map(p -> new PostRecord(
                        p.getId() != null ? p.getId().toString() : null,
                        p.getTitle(),
                        p.getContent(),
                        p.getStatus(),
                        p.getUser() != null ? p.getUser().getName() : null,
                        p.getCreatedAt(),
                        p.getPublishedDatetime(),
                        (int) commentRepository.countByPost_Id(p.getId()),
                        p.getUser() != null && p.getUser().getId() != null ? p.getUser().getId().toString() : null,
                        null))
                .toList();
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
    public void updatePost(Post post, String postId) throws DatabaseQueryException, PostNotFoundException {
        UUID postUuid = UUID.fromString(postId);
        UUID userUuid = post.getUserId();
        if (postRepository.existsById(postUuid) && userRepository.existsById(userUuid)) {
            if ("PUBLISH".equalsIgnoreCase(post.getStatus())) {
                post.setIsPublish(true);
                post.setStatus("PUBLISHED");
            } else {
                post.setIsPublish(false);
                post.setStatus("DRAFT");
            }
            post.setId(postUuid);
            postRepository.save(post);
        } else {
            throw new PostNotFoundException("Post not found");
        }
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
    public void deletePost(String postId, String userId) throws DatabaseQueryException, PostNotFoundException {
        UUID postUuid = UUID.fromString(postId);
        UUID userUuid = UUID.fromString(userId);
        if (postRepository.existsById(postUuid) && userRepository.existsById(userUuid)) {
            postRepository.delete(postRepository.findById(postUuid).get());
        } else {
            throw new PostNotFoundException("Post not found");
        }
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
}
