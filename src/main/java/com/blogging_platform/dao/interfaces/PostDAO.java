package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;

/**
 * Data access interface for blog posts. Handles create, read, update, delete,
 * and listing of posts (all published or by user).
 */
public interface PostDAO {

    /**
     * Inserts a new post and returns its generated id.
     *
     * @param post the post to create
     * @return the new post's id
     * @throws DatabaseQueryException if the insert fails
     */
    String create(Post post) throws DatabaseQueryException;

    /**
     * Fetches a post by id for a specific user (ownership check).
     *
     * @param postId post id
     * @param userId user id (must own the post)
     * @return the post record
     * @throws PostNotFoundException if the post does not exist or user does not own it
     * @throws DatabaseQueryException if the query fails
     */
    PostRecord getByID(String postId, String userId) throws DatabaseQueryException, PostNotFoundException;

    /**
     * Returns all published posts.
     *
     * @return list of published post records
     * @throws DatabaseQueryException if the query fails
     */
    List<PostRecord> getAll() throws DatabaseQueryException;

    /**
     * Returns posts for a given user (for admin/list view).
     *
     * @param userId user id
     * @return list of post records
     * @throws DatabaseQueryException if the query fails
     */
    List<PostRecord> getAll(String userId) throws DatabaseQueryException;

    /**
     * Fetches a post by id (no ownership check).
     *
     * @param postId post id
     * @return the post record
     * @throws PostNotFoundException if the post does not exist
     * @throws DatabaseQueryException if the query fails
     */
    PostRecord getByID(String postId) throws DatabaseQueryException, PostNotFoundException;

    /**
     * Updates an existing post.
     *
     * @param post the post with updated fields
     * @throws PostNotFoundException if the post does not exist
     * @throws DatabaseQueryException if the update fails
     */
    void edit(Post post) throws DatabaseQueryException, PostNotFoundException;

    /**
     * Deletes a post. Only the owner can delete.
     *
     * @param postId post id
     * @param userId user id (must own the post)
     * @throws PostNotFoundException if the post does not exist or user does not own it
     * @throws DatabaseQueryException if the delete fails
     */
    void delete(String postId, String userId) throws DatabaseQueryException, PostNotFoundException;
}
