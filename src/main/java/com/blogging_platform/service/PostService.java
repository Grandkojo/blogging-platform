package com.blogging_platform.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.PagedResult;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.dao.interfaces.UserDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;

/**
 * Application service for blog posts. Delegates to {@link PostDAO} and
 * normalizes
 * publish status (PUBLISHED vs DRAFT) when creating or updating posts.
 */
@Service
public class PostService {

    @Autowired
    private PostDAO postDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private com.blogging_platform.service.TagService tagService;

    /** Creates a post service with the given DAO. */
    public PostService(PostDAO postDAO, UserDAO userDAO) {
        this.postDAO = postDAO;
        this.userDAO = userDAO;
    }

    /**
     * Creates a new post and returns its id. Sets isPublish from status.
     *
     * @param post the post to create
     * @return the new post's id
     * @throws DatabaseQueryException if the insert fails
     */
    public String createPost(Post post) throws DatabaseQueryException {
        if (userDAO.existsById(post.getUserId())) {
            if ("PUBLISH".equalsIgnoreCase(post.getStatus())) {
                post.setIsPublish(true);
                post.setStatus("PUBLISHED");
            } else {
                post.setIsPublish(false);
                post.setStatus("DRAFT");
            }
        }
        return postDAO.create(post);
    }

    /**
     * Returns all posts for a given user (e.g. admin list).
     *
     * @param userId user id
     * @return list of post records
     * @throws DatabaseQueryException if the query fails
     */
    // public List<PostRecord> getUserPosts(String userId) throws DatabaseQueryException {
    //     return postDAO.getAll(userId);
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
        return postDAO.getByID(postId, userId);
    }

    /**
     * Fetches a post by id (no ownership check).
     *
     * @param postId post id
     * @return the post record
     * @throws PostNotFoundException  if the post does not exist
     * @throws DatabaseQueryException if the query fails
     */
    public PostRecord getPost(String postId) throws DatabaseQueryException, PostNotFoundException {
        return postDAO.getByID(postId);
    }

    public boolean existsById(String postId) {
        return postDAO.existsById(postId);
    }

    /**
     * Returns all published posts.
     *
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    public List<PostRecord> getPosts() throws DatabaseQueryException {
        return postDAO.getAll();
    }

    /**
     * Updates an existing post. Sets isPublish from status.
     *
     * @param post the post with updated fields
     * @throws PostNotFoundException  if the post does not exist
     * @throws DatabaseQueryException if the update fails
     */
    public void updatePost(Post post, String postId) throws DatabaseQueryException, PostNotFoundException {
        if (postDAO.existsById(postId) && userDAO.existsById(post.getUserId())) {
            if ("PUBLISH".equalsIgnoreCase(post.getStatus())) {
                post.setIsPublish(true);
                post.setStatus("PUBLISHED");
            } else {
                post.setIsPublish(false);
                post.setStatus("DRAFT");
            }
            post.setId(postId);
            postDAO.edit(post);
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
    public void deletePost(String postId, String userId) throws DatabaseQueryException, PostNotFoundException {
        if (postDAO.existsById(postId) && userDAO.existsById(userId)) {
            postDAO.delete(postId, userId);
        } else {
            throw new PostNotFoundException("Post not found");
        }
    }

    /**
     * Enriches a paged result of posts with their tags.
     */
    public PagedResult<PostRecord> mapToPostWithTags(PagedResult<PostRecord> source) {
        List<PostRecord> content = source.content().stream()
            .map(this::toPostWithTags)
            .toList();
        return new PagedResult<>(
            content,
            source.page(),
            source.size(),
            source.totalElements()
        );
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
            resolveTagsForPost(post.id())
        );
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
