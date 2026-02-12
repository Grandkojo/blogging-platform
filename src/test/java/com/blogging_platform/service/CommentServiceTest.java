package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;
import com.blogging_platform.model.Post;
import com.blogging_platform.model.User;
import com.blogging_platform.repository.CommentRepository;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addComment_savesEntity() throws DatabaseQueryException {
        Comment comment = Comment.forCreate("Nice post", UUID.randomUUID().toString(), UUID.randomUUID().toString());

        commentService.addComment(comment);

        verify(commentRepository).save(comment);
    }

    @Test
    void getCommentsByPostId_mapsEntitiesToRecords() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setName("John Doe");

        Post post = new Post();
        post.setId(postId);

        Comment entity = Comment.forCreate("Nice post", userId.toString(), postId.toString());
        entity.setUser(user);
        entity.setPost(post);

        when(commentRepository.findByPost_IdOrderByDatetimeDesc(postId)).thenReturn(List.of(entity));

        List<CommentRecord> result = commentService.getComments(postId.toString());

        assertEquals(1, result.size());
        CommentRecord record = result.get(0);
        assertEquals(postId.toString(), record.postId());
        assertEquals(userId.toString(), record.userId());
        assertEquals("John Doe", record.authorName());
    }

    @Test
    void getComments_withPageable_usesRepository() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        Comment entity = Comment.forCreate("Nice", UUID.randomUUID().toString(), postId.toString());
        Pageable pageable = PageRequest.of(0, 10);

        when(commentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));

        List<CommentRecord> result = commentService.getComments(pageable);

        assertEquals(1, result.size());
    }

    @Test
    void getCommentById_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.getComment(id.toString()));
    }
}

