package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Post;
import com.blogging_platform.model.Tag;
import com.blogging_platform.repository.PostRepository;
import com.blogging_platform.repository.TagRepository;

class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTag_savesTag() throws DatabaseQueryException, DuplicateResourceException {
        Tag tag = new Tag("tech");

        tagService.createTag(tag);

        verify(tagRepository).save(tag);
    }

    @Test
    void getAllTags_mapsEntitiesToRecords() throws DatabaseQueryException {
        Tag tag = new Tag(UUID.randomUUID(), "tech");
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        List<TagRecord> records = tagService.getAllTags();

        assertEquals(1, records.size());
        assertEquals("tech", records.get(0).tag());
    }

    @Test
    void linkTagToPost_addsTagToPost() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        Post post = new Post();
        post.setId(postId);
        Tag tag = new Tag();
        tag.setId(tagId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        tagService.linkTagToPost(postId.toString(), tagId.toString());

        verify(postRepository).save(post);
        assertEquals(1, post.getTags().size());
    }

    @Test
    void getTagsByPostId_mapsEntitiesToRecords() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        Tag tag = new Tag(UUID.randomUUID(), "spring");
        when(tagRepository.findByPosts_Id(postId)).thenReturn(List.of(tag));

        List<TagRecord> records = tagService.getTagsByPostId(postId.toString());

        assertEquals(1, records.size());
        assertEquals("spring", records.get(0).tag());
    }
}

