package com.blogging_platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.exceptions.AuthorizationException;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.ValidationException;
import com.blogging_platform.model.Comment;
import com.blogging_platform.repository.CommentRepository;

import jakarta.transaction.Transactional;

/**
 * Application service for comments on posts. Uses JPA repositories and maps entities
 * to {@link CommentRecord} DTOs.
 */
@Service
@Transactional(rollbackOn = { DatabaseQueryException.class, CommentNotFoundException.class })
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Adds a new comment to a post. Sets creation datetime if not already set.
     *
     * @param comment the comment (content, user id, post id)
     * @throws DatabaseQueryException if the insert fails
     */
    public void addComment(Comment comment) throws DatabaseQueryException {
        if (comment.getDatetime() == null) {
            comment.setDatetime(LocalDateTime.now());
        }
        commentRepository.save(comment);
    }

    /**
     * Returns all comments for a post.
     *
     * @param postId post id
     * @return list of comment records
     * @throws DatabaseQueryException if the query fails
     */
    public List<CommentRecord> getComments(String postId) throws DatabaseQueryException {
        UUID postUuid = UUID.fromString(postId);
        return commentRepository.findByPost_IdOrderByDatetimeDesc(postUuid).stream()
                .map(this::toRecord)
                .toList();
    }

    /**
     * Returns all comments across all posts.
     *
     * @return list of comment records
     * @throws DatabaseQueryException if the query fails
     */
    public List<CommentRecord> getComments(Pageable pageable) throws DatabaseQueryException {
        return commentRepository.findAll(pageable).stream()
                .map(this::toRecord)
                .toList();
    }

    /**
     * Fetches a single comment by id.
     *
     * @param commentId comment id
     * @return the comment record
     * @throws CommentNotFoundException if the comment does not exist
     * @throws DatabaseQueryException if the query fails
     */
    public CommentRecord getComment(String commentId) throws DatabaseQueryException, CommentNotFoundException {
        UUID id = UUID.fromString(commentId);
        return commentRepository.findById(id)
                .map(this::toRecord)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    /**
     * Updates an existing comment. Only the author may update.
     *
     * @param comment the comment with updated content
     * @throws CommentNotFoundException if the comment does not exist
     * @throws AuthorizationException if the user is not the author of the comment
     * @throws ValidationException if postId in body does not match the comment's post
     * @throws DatabaseQueryException if the update fails
     */
    public void editComment(Comment comment) throws DatabaseQueryException, CommentNotFoundException, AuthorizationException, ValidationException {
        if (comment.getId() == null || comment.getUserId() == null) {
            throw new ValidationException("Comment id and user id are required");
        }
        UUID id = comment.getId();
        UUID userId = comment.getUserId();
        Comment existing = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment with id '" + id + "' not found.", null));
        if (!existing.getUserId().equals(userId)) {
            throw new AuthorizationException("User is not the author of this comment.");
        }
        if (comment.getPostId() != null && !comment.getPostId().equals(existing.getPostId())) {
            throw new ValidationException("Post does not match this comment.");
        }
        existing.setComment(comment.getComment());
        commentRepository.save(existing);
    }

    /**
     * Deletes a comment. Only the author may delete.
     *
     * @param commentId comment id
     * @param userId    user id (must be the comment author)
     * @throws CommentNotFoundException if the comment does not exist or user is not the author
     * @throws DatabaseQueryException if the delete fails
     */
    public void deleteComment(String commentId, String userId) throws DatabaseQueryException, CommentNotFoundException {
        UUID id = UUID.fromString(commentId);
        UUID userUuid = UUID.fromString(userId);
        Comment existing = commentRepository.findByIdAndUser_Id(id, userUuid)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.delete(existing);
    }

    private CommentRecord toRecord(Comment c) {
        if (c == null) {
            return null;
        }
        return new CommentRecord(
                c.getId() != null ? c.getId().toString() : null,
                c.getPostId() != null ? c.getPostId().toString() : null,
                c.getUserId() != null ? c.getUserId().toString() : null,
                c.getUser() != null ? c.getUser().getName() : null,
                c.getComment(),
                c.getDatetime());
    }
}
