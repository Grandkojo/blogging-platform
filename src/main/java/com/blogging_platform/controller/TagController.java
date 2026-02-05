package com.blogging_platform.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.model.Tag;
import com.blogging_platform.service.TagService;

import jakarta.validation.Valid;

/**
 * REST and GraphQL controller for tags and post–tag relationships.
 */
@RestController
public class TagController {
    
    private final TagService tagService;

    /**
     * Creates a controller with the required {@link TagService}.
     */
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * GraphQL query that returns all tags.
     */
    @QueryMapping
    public List<TagRecord> getTagss() {
        return tagService.getAllTags();
    }

    /**
     * GraphQL mutation to create a new tag.
     */
    @MutationMapping(name = "createTag")
    public Boolean createTagMutation(@Argument String tag) {
        Tag t = new Tag(tag);
        tagService.createTag(t);
        return true;
    }

    /**
     * GraphQL mutation to link an existing tag to a post.
     */
    @MutationMapping(name = "linkTagToPost")
    public Boolean linkTagToPostMutation(
        @Argument String tagId,
        @Argument String postId
    ) {
        tagService.linkTagToPost(postId, tagId);
        return true;
    }

    /**
     * GraphQL mutation to unlink all tags from a post.
     */
    @MutationMapping(name = "unlinkTagsFromPost")
    public Boolean unlinkTagsFromPostMutation(@Argument String postId) {
        tagService.unlinkAllTagsFromPost(postId);
        return true;
    }

    /**
     * REST endpoint that returns all tags.
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<Object>> getTags() {
        List<TagRecord> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tags, "Tags Fetched Successfully"));
    }

    /**
     * REST endpoint returning a single tag by id.
     */
    @GetMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<Object>> getTag(@PathVariable String id) {
        TagRecord tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tag , "Tag Found Successfully"));
    }

    /**
     * REST endpoint returning a tag by its name (query parameter).
     */
    @GetMapping(value = "/tags", params = "name")
    public ResponseEntity<ApiResponse<Object>> getTagByName(@RequestParam String name) {
        TagRecord tag = tagService.getTagByTagName(name);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tag , "Tag Found Successfully"));
    }

    /**
     * REST endpoint returning all tags associated with a given post.
     */
    @GetMapping("/posts/{postId}/tags")
    public ResponseEntity<ApiResponse<Object>> getPostTags(@PathVariable String postId) {
        List<TagRecord> tags = tagService.getTagsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tags , "Post Tags Found Successfully"));
    }
    

    /**
     * REST endpoint to create a new tag.
     */
    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<Object>> createPost(@Valid @RequestBody Tag tag) {
        tagService.createTag(tag);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Tag Created Successfully"));
      
    }

    /**
     * REST endpoint to link an existing tag to a post.
     */
    @GetMapping("tags/{tagId}/link-to-post/{postId}")
    public ResponseEntity<ApiResponse<Object>> linkTagToPost(@PathVariable String tagId, @PathVariable String postId) {
        tagService.linkTagToPost(postId, tagId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Tag Linked to Post Successfully"));
    }

    /**
     * REST endpoint to unlink all tags from a post.
     */
    @GetMapping("tags/unlink-from-post/{postId}")
    public ResponseEntity<ApiResponse<Object>> unlinkTagsFromPost(@PathVariable String postId) {
        tagService.unlinkAllTagsFromPost(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "All Tags Unlinked from Post Successfully"));
    }
}
