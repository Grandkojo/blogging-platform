package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.model.Post;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;





/**
 * REST and GraphQL controller for blog posts.
 */
@RestController
@Tag(name = "Posts", description = "APIs for managing blog posts")
public class PostController {

    private final PostService postService;

    /**
     * Creates a controller with the required {@link PostService}.
     */
    public PostController(PostService postService, TagService tagService) {
        this.postService = postService;
    }

    /**
     * GraphQL query that returns published posts, optionally filtered and paginated.
     * Mirrors the REST /posts endpoint behaviour (including tag search).
     */
    @QueryMapping
    public List<PostRecord> getPosts(
        @Argument String query,
        @Argument Integer page,
        @Argument Integer size,
        @Argument String sortBy,
        @Argument String dir

    ) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        String direction = dir != null ? dir : "DESC";

        Sort sort = Sort.by(
            Sort.Direction.fromString(direction),
            sortBy != null ? sortBy : "createdAt"
        );
        Pageable pagination = PageRequest.of(p, s, sort);
        List<PostRecord> posts = postService.getPosts(query, pagination);
        List<PostRecord> dto = posts.stream()
            .map(postService::toPostWithTags)
            .toList();
        return dto;
    }

    @QueryMapping
    public List<PostRecord> getPostss(
    ) {
        List<PostRecord> posts = postService.getPosts(null);
        List<PostRecord> dto = posts.stream()
            .map(postService::toPostWithTags)
            .toList();
        return dto;
    }

    /**
     * GraphQL query to fetch a single post by its id.
     *
     * @param id post identifier (UUID as string)
     */
    // @QueryMapping(name = "getPost")
    // public PostRecord getPostById(@Argument String id) {
    //     return postService.toPostWithTags(postService.getPost(id));
    // }

    // @Operation(
    //     summary = "List posts (paginated)",
    //     description = "Returns a paginated list of published posts, optionally filtered by search query (title, author, or tag) and sorted."
    // )
    // @ApiResponses({
    //     @io.swagger.v3.oas.annotations.responses.ApiResponse(
    //         responseCode = "200",
    //         description = "Posts fetched successfully",
    //         content = @Content(schema = @Schema(implementation = ApiResponse.class))
    //     ),
    //     @io.swagger.v3.oas.annotations.responses.ApiResponse(
    //         responseCode = "400",
    //         description = "Invalid pagination parameters"
    //     ),
    //     @io.swagger.v3.oas.annotations.responses.ApiResponse(
    //         responseCode = "500",
    //         description = "Unexpected server error"
    //     )
    // })
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostRecord>>> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String query,
        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false, defaultValue = "DESC") String dir

    ) {
        Sort sort = Sort.by(
            Sort.Direction.fromString(dir),
            sortBy
        );
        Pageable pagination = PageRequest.of(page, size, sort);
        List<PostRecord> posts = postService.getPosts(query, pagination);
        List<PostRecord> dto = posts.stream()
            .map(postService::toPostWithTags)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, dto, "Posts Fetched Successfully"));
    }

    @Operation(
        summary = "List all posts",
        description = "Returns the full list of published posts without pagination."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Posts fetched successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected server error"
        )
    })
    @GetMapping("/postss")
    public ResponseEntity<ApiResponse<List<PostRecord>>> getPostsFull() {
        List<PostRecord> posts = postService.getPosts(null);
        List<PostRecord> dto = posts.stream()
            .map(postService::toPostWithTags)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, dto, "Posts Fetched Successfully"));
    }

    @Operation(
        summary = "Get post by id",
        description = "Fetches a single published post by its id, including tags and comment count."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Post found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Post not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected server error"
        )
    })
    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Object>> getPost(@PathVariable String id) {
        PostRecord post = postService.getPost(id);
        PostRecord dto = postService.toPostWithTags(post);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, dto , "Post Found Successfully"));
    }
    

    @Operation(
        summary = "Create post",
        description = "Creates a new post for a user. The status determines whether the post is published or saved as draft."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Post created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected server error"
        )
    })
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<Object>> createPost(@Valid @RequestBody Post post) {
        postService.createPost(post);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Post Created Successfully"));
      
    }

    @Operation(
        summary = "Update post",
        description = "Updates an existing post owned by the given user."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Post updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Post or user not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected server error"
        )
    })
    @PutMapping("posts/{id}")
    public ResponseEntity<ApiResponse<Object>> editPost(@PathVariable String id, @Valid @RequestBody Post post) {
        postService.updatePost(post, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Post Updated Successfully"));

    }

    @Operation(
        summary = "Delete post",
        description = "Deletes a post owned by the given user."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "Post deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Post or user not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Unexpected server error"
        )
    })
    @DeleteMapping("/{userId}/posts/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String userId, @PathVariable String id) {
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Post Deleted Successfully"));
    }

    /**
     * GraphQL mutation to create a new post.
     */
    @MutationMapping(name = "createPost")
    public void createPostMutation(
        @Argument String userId,
        @Argument String title,
        @Argument String content,
        @Argument String status
    ) {
        Post post = new Post();
        post.setUserId(UUID.fromString(userId));
        post.setTitle(title);
        post.setContent(content);
        post.setStatus(status);
        postService.createPost(post);
    }

    /**
     * GraphQL mutation to update an existing post.
     */
    @MutationMapping(name = "updatePost")
    public Boolean updatePostMutation(
        @Argument String id,
        @Argument String userId,
        @Argument String title,
        @Argument String content,
        @Argument String status
    ) {
        Post post = new Post();
        post.setUserId(UUID.fromString(userId));
        post.setTitle(title);
        post.setContent(content);
        post.setStatus(status);
        postService.updatePost(post, id);
        return true;
    }

    /**
     * GraphQL mutation to delete a post for a given user.
     */
    @MutationMapping(name = "deletePost")
    public Boolean deletePostMutation(
        @Argument String userId,
        @Argument String id
    ) {
        postService.deletePost(id, userId);
        return true;
    }

}
