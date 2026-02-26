package com.blogging_platform.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.service.PostStatisticsService;
import com.blogging_platform.service.TagSearchOptimizer;
import com.blogging_platform.service.TrendingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/performance")
@Tag(name = "Performance", description = "Endpoints to test and verify system optimizations")
public class PerformanceController {

    private final TrendingService trendingService;
    private final TagSearchOptimizer tagSearchOptimizer;
    private final PostStatisticsService statisticsService;

    public PerformanceController(TrendingService trendingService,
            TagSearchOptimizer tagSearchOptimizer,
            PostStatisticsService statisticsService) {
        this.trendingService = trendingService;
        this.tagSearchOptimizer = tagSearchOptimizer;
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "Get Trending Posts", description = "Returns top-K trending posts based on views and ratings.")
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<PostRecord>>> getTrending(@RequestParam(defaultValue = "5") int k) {
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, trendingService.getTrendingPosts(k), "Trending posts fetched"));
    }

    @Operation(summary = "Tag Autocomplete", description = "Prefix-based tag search using Trie data structure.")
    @GetMapping("/tags/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(@RequestParam String prefix) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, tagSearchOptimizer.autocomplete(prefix),
                "Autocomplete results fetched"));
    }

    @Operation(summary = "Get Post Views", description = "Real-time view count from memory (ConcurrentHashMap).")
    @GetMapping("/views")
    public ResponseEntity<ApiResponse<ViewResponse>> getView(@RequestParam String postId) {
        long views = statisticsService.getViews(postId);
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, new ViewResponse(postId, views), "Post views fetched"));
    }

    public record ViewResponse(String postId, long views) {
    }
}
