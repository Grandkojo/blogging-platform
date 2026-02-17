package com.blogging_platform.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean createTagMutation(@Argument String tag) {
        Tag t = new Tag(tag);
        tagService.createTag(t);
        return true;
    }

    /**
     * GraphQL mutation to link an existing tag to a post.
     */
    @MutationMapping(name = "linkTagToPost")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean unlinkTagsFromPostMutation(@Argument String postId) {
        tagService.unlinkAllTagsFromPost(postId);
        return true;
    }

    @Operation(
        summary = "List tags",
        description = "Returns all tags."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<Object>> getTags() {
        List<TagRecord> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tags, "Tags Fetched Successfully"));
    }

    @Operation(
        summary = "Get tag by id",
        description = "Fetches a single tag by its id."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<Object>> getTag(@PathVariable String id) {
        TagRecord tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tag , "Tag Found Successfully"));
    }

    @Operation(
        summary = "Get tag by name",
        description = "Fetches a tag by its name using the 'name' query parameter."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/tags", params = "name")
    public ResponseEntity<ApiResponse<Object>> getTagByName(@RequestParam String name) {
        TagRecord tag = tagService.getTagByTagName(name);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tag , "Tag Found Successfully"));
    }

    @Operation(
        summary = "List tags for post",
        description = "Returns all tags associated with a given post."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post tags fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/posts/{postId}/tags")
    public ResponseEntity<ApiResponse<Object>> getPostTags(@PathVariable String postId) {
        List<TagRecord> tags = tagService.getTagsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tags , "Post Tags Found Successfully"));
    }
    

    @Operation(
        summary = "Create tag",
        description = "Creates a new tag. Tag names must be unique."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tag created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> createPost(@Valid @RequestBody Tag tag) {
        tagService.createTag(tag);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Tag Created Successfully"));
      
    }

    @Operation(
        summary = "Link tag to post",
        description = "Links an existing tag to a post."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Tag linked to post successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag or post not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("tags/{tagId}/link-to-post/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> linkTagToPost(@PathVariable String tagId, @PathVariable String postId) {
        tagService.linkTagToPost(postId, tagId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Tag Linked to Post Successfully"));
    }

    @Operation(
        summary = "Unlink all tags from post",
        description = "Removes all tag associations from a post."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "All tags unlinked from post successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("tags/unlink-from-post/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> unlinkTagsFromPost(@PathVariable String postId) {
        tagService.unlinkAllTagsFromPost(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "All Tags Unlinked from Post Successfully"));
    }
}
