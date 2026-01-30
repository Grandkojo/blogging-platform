package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;

/**
 * Application service for blog posts. Delegates to {@link PostDAO} and normalizes
 * publish status (PUBLISHED vs DRAFT) when creating or updating posts.
 */
public class PostService {
    private PostDAO postDAO;

    /** Creates a post service with the given DAO. */
    public PostService(PostDAO postDAO) {
        this.postDAO = postDAO;
    }

    /**
     * Creates a new post and returns its id. Sets isPublish from status.
     *
     * @param post the post to create
     * @return the new post's id
     * @throws DatabaseQueryException if the insert fails
     */
    public String createPost(Post post) throws DatabaseQueryException {
        if (post.getStatus().equals("PUBLISHED")){
            post.setIsPublish(true);
        } else {
            post.setIsPublish(false);
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
    public List<PostRecord> getUserPosts(String userId) throws DatabaseQueryException {
        return postDAO.getAll(userId);
    }

    /**
     * Fetches a post by id for a specific user (ownership check).
     *
     * @param postId post id
     * @param userId user id (must own the post)
     * @return the post record
     * @throws PostNotFoundException if the post does not exist or user does not own it
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
     * @throws PostNotFoundException if the post does not exist
     * @throws DatabaseQueryException if the query fails
     */
    public PostRecord getPost(String postId) throws DatabaseQueryException, PostNotFoundException {
        return postDAO.getByID(postId);
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
     * @throws PostNotFoundException if the post does not exist
     * @throws DatabaseQueryException if the update fails
     */
    public void updatePost(Post post) throws DatabaseQueryException, PostNotFoundException {
        if (post.getStatus().equals("PUBLISHED")){
            post.setIsPublish(true);
        } else {
            post.setIsPublish(false);
        }
        postDAO.edit(post);
    }

    /**
     * Deletes a post. Only the owner can delete.
     *
     * @param postId post id
     * @param userId user id (must own the post)
     * @throws PostNotFoundException if the post does not exist or user does not own it
     * @throws DatabaseQueryException if the delete fails
     */
    public void deletePost(String postId, String userId) throws DatabaseQueryException, PostNotFoundException {
        postDAO.delete(postId, userId);
    }
}
