package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;
import com.blogging_platform.model.User;
import com.blogging_platform.repository.CommentRepository;
import com.blogging_platform.repository.PostRepository;
import com.blogging_platform.repository.UserRepository;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPost_setsPublishFields_whenUserExistsAndStatusPublish() throws DatabaseQueryException {
        UUID userId = UUID.randomUUID();
        Post post = new Post();
        post.setUserId(userId);
        post.setStatus("PUBLISH");

        when(userRepository.existsById(userId)).thenReturn(true);

        postService.createPost(post);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertEquals("PUBLISHED", saved.getStatus());
        assertEquals(true, saved.getIsPublish());
    }

    @Test
    void createPost_keepsStatusWhenUserDoesNotExist() throws DatabaseQueryException {
        UUID userId = UUID.randomUUID();
        Post post = new Post();
        post.setUserId(userId);
        post.setStatus("UNKNOWN");

        when(userRepository.existsById(userId)).thenReturn(false);

        postService.createPost(post);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertEquals("UNKNOWN", saved.getStatus());
    }

    @Test
    void getPostById_mapsEntityToRecord() throws DatabaseQueryException, PostNotFoundException {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setName("Author");

        Post entity = new Post();
        entity.setId(postId);
        entity.setTitle("Title");
        entity.setContent("Content");
        entity.setStatus("PUBLISHED");
        entity.setUser(user);

        when(postRepository.findById(postId)).thenReturn(Optional.of(entity));
        when(commentRepository.countByPost_Id(postId)).thenReturn(3L);

        PostRecord record = postService.getPost(postId.toString());

        assertEquals(postId.toString(), record.id());
        assertEquals("Title", record.title());
        assertEquals("Author", record.author());
        assertEquals(3, record.commentCount());
    }

    @Test
    void getPostById_throwsWhenMissing() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPost(postId.toString()));
    }

    @Test
    void getPosts_returnsMappedRecords() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        Post entity = new Post();
        entity.setId(postId);
        entity.setTitle("Title");
        entity.setContent("Content");
        entity.setStatus("PUBLISHED");

        when(postRepository.findAll()).thenReturn(List.of(entity));
        when(commentRepository.countByPost_Id(postId)).thenReturn(0L);

        List<PostRecord> records = postService.getPosts();

        assertEquals(1, records.size());
        assertEquals(postId.toString(), records.get(0).id());
    }

    @Test
    void getPosts_withPageable_usesRepositoryAndMaps() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        Post entity = new Post();
        entity.setId(postId);
        entity.setTitle("Title");
        entity.setContent("Content");
        entity.setStatus("PUBLISHED");

        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(commentRepository.countByPost_Id(postId)).thenReturn(1L);

        List<PostRecord> records = postService.getPosts(pageable);

        assertEquals(1, records.size());
        assertEquals(1, records.get(0).commentCount());
    }

    @Test
    void updatePost_updatesWhenPostAndUserExist() throws DatabaseQueryException, PostNotFoundException {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = new Post();
        post.setUserId(userId);
        post.setStatus("PUBLISH");

        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);

        postService.updatePost(post, postId.toString());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertEquals(postId, saved.getId());
        assertEquals("PUBLISHED", saved.getStatus());
    }

    @Test
    void updatePost_throwsWhenPostOrUserMissing() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = new Post();
        post.setUserId(userId);

        when(postRepository.existsById(postId)).thenReturn(false);
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(PostNotFoundException.class, () -> postService.updatePost(post, postId.toString()));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_deletesWhenPostAndUserExist() throws DatabaseQueryException, PostNotFoundException {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post entity = new Post();
        entity.setId(postId);

        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(entity));

        postService.deletePost(postId.toString(), userId.toString());

        verify(postRepository).delete(entity);
    }

    @Test
    void deletePost_throwsWhenPostOrUserMissing() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(postRepository.existsById(postId)).thenReturn(false);
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(PostNotFoundException.class, () -> postService.deletePost(postId.toString(), userId.toString()));
        verify(postRepository, never()).delete(any(Post.class));
    }
}

