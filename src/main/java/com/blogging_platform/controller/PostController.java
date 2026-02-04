package com.blogging_platform.controller;

import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.model.Post;
import com.blogging_platform.service.PostService;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;





@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService){
        this.postService = postService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Object>> getPosts() {
        List<PostRecord> posts = postService.getPosts();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, posts, "Posts Fetched Successfully"));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Object>> getPost(@PathVariable String id) {
        PostRecord post = postService.getPost(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, post , "Post Found Successfully"));
    }
    

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<Object>> createPost(@RequestBody Post post) {
        String postId = postService.createPost(post);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, postId, "Post Created Successfully"));
      
    }

    @PutMapping("posts/{id}")
    public ResponseEntity<ApiResponse<Object>> editPost(@PathVariable String id, @RequestBody Post post) {
        postService.updatePost(post, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Post Updated Successfully"));

    }

    @DeleteMapping("/{userId}/posts/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String userId, @PathVariable String id) {
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Post Deleted Successfully"));
    }
    
    
}
