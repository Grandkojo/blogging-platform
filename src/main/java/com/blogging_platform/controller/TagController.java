package com.blogging_platform.controller;

import java.util.List;

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

@RestController
public class TagController {
    
    private final TagService tagService;

    public TagController(TagService tagService){
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<Object>> getTags() {
        List<TagRecord> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tags, "Tags Fetched Successfully"));
    }

    @GetMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<Object>> getTag(@PathVariable String id) {
        TagRecord tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tag , "Tag Found Successfully"));
    }

    @GetMapping(value = "/tags", params = "name")
    public ResponseEntity<ApiResponse<Object>> getTagByName(@RequestParam String name) {
        TagRecord tag = tagService.getTagByTagName(name);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tag , "Tag Found Successfully"));
    }

    @GetMapping("/posts/{postId}/tags")
    public ResponseEntity<ApiResponse<Object>> getPostTags(@PathVariable String postId) {
        List<TagRecord> tags = tagService.getTagsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tags , "Post Tags Found Successfully"));
    }
    

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<Object>> createPost(@RequestBody Tag tag) {
        tagService.createTag(tag);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Tag Created Successfully"));
      
    }

    @GetMapping("tags/{tagId}/link-to-post/{postId}")
    public ResponseEntity<ApiResponse<Object>> linkTagToPost(@PathVariable String tagId, @PathVariable String postId) {
        tagService.linkTagToPost(postId, tagId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Tag Linked to Post Successfully"));
    }

    @GetMapping("tags/unlink-from-post/{postId}")
    public ResponseEntity<ApiResponse<Object>> unlinkTagsFromPost(@PathVariable String postId) {
        tagService.unlinkAllTagsFromPost(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "All Tags Unlinked from Post Successfully"));
    }
}
