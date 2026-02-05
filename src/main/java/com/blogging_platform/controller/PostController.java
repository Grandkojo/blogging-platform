package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.CacheManager;
import com.blogging_platform.classes.PagedResult;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.model.Post;
import com.blogging_platform.service.PostService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
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





@RestController
public class PostController {

    private final PostService postService;
    private final CacheManager cacheManager;

    public PostController(PostService postService, CacheManager cacheManager){
        this.postService = postService;
        this.cacheManager = cacheManager;
    }

    @QueryMapping
    public List<PostRecord> getPosts(){
        return postService.getPosts();
    }

    @QueryMapping(name = "getPost")
    public PostRecord getPostById(@Argument String id) {
        return postService.getPost(id);
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PagedResult<PostRecord>>> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String sortBy
    ) {
        
        PagedResult<PostRecord> posts = cacheManager.getPaginatedPublishedPosts(page, size, query, sortBy);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, posts, "Posts Fetched Successfully"));
    }

    @GetMapping("/postss")
    public ResponseEntity<ApiResponse<Object>> getPostsFull() {
        List<PostRecord> posts = postService.getPosts();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, posts, "Posts Fetched Successfully"));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Object>> getPost(@PathVariable String id) {
        PostRecord post = postService.getPost(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, post , "Post Found Successfully"));
    }
    

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<Object>> createPost(@Valid @RequestBody Post post) {
        String postId = postService.createPost(post);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, postId, "Post Created Successfully"));
      
    }

    @PutMapping("posts/{id}")
    public ResponseEntity<ApiResponse<Object>> editPost(@PathVariable String id, @Valid @RequestBody Post post) {
        postService.updatePost(post, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Post Updated Successfully"));

    }

    @DeleteMapping("/{userId}/posts/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String userId, @PathVariable String id) {
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Post Deleted Successfully"));
    }

    @MutationMapping(name = "createPost")
    public String createPostMutation(
        @Argument String userId,
        @Argument String title,
        @Argument String content,
        @Argument String status
    ) {
        Post post = new Post(userId, title, content, status);
        return postService.createPost(post);
    }

    @MutationMapping(name = "updatePost")
    public Boolean updatePostMutation(
        @Argument String id,
        @Argument String userId,
        @Argument String title,
        @Argument String content,
        @Argument String status
    ) {
        Post post = new Post(userId, title, content, status);
        postService.updatePost(post, id);
        return true;
    }

    @MutationMapping(name = "deletePost")
    public Boolean deletePostMutation(
        @Argument String userId,
        @Argument String id
    ) {
        postService.deletePost(id, userId);
        return true;
    }
    
    
}
