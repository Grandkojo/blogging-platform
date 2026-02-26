package com.blogging_platform.service;

import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;

/**
 * Service for calculating and retrieving trending posts.
 * Uses a PriorityQueue (Heap) to efficiently find the top-K items.
 */
@Service
public class TrendingService {

    private final PostService postService;
    private final PostStatisticsService statisticsService;
    private final ReviewService reviewService;

    public TrendingService(PostService postService, PostStatisticsService statisticsService,
            ReviewService reviewService) {
        this.postService = postService;
        this.statisticsService = statisticsService;
        this.reviewService = reviewService;
    }

    /**
     * Gets the top K trending posts based on a custom score:
     * Score = (Views * 0.4) + (AvgRating * 10 * 0.6)
     *
     * @param k the number of trending posts to return
     * @return list of trending post records
     * @throws DatabaseQueryException if data retrieval fails
     */
    public List<PostRecord> getTrendingPosts(int k) throws DatabaseQueryException {
        List<PostRecord> allPosts = postService.getPosts();

        // Min-heap to keep track of the top K scores efficiently
        PriorityQueue<ScoredPost> topK = new PriorityQueue<>(k);

        for (PostRecord post : allPosts) {
            double score = calculateScore(post);
            ScoredPost scored = new ScoredPost(post, score);

            if (topK.size() < k) {
                topK.offer(scored);
            } else if (score > topK.peek().score) {
                topK.poll();
                topK.offer(scored);
            }
        }

        return topK.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score)) // Sort the final K items
                .map(sp -> sp.post)
                .collect(Collectors.toList());
    }

    private double calculateScore(PostRecord post) throws DatabaseQueryException {
        long views = statisticsService.getViews(post.id());
        double avgRating = reviewService.getAverageRating(post.id());
        return (views * 0.4) + (avgRating * 10 * 0.6);
    }

    private static class ScoredPost implements Comparable<ScoredPost> {
        PostRecord post;
        double score;

        ScoredPost(PostRecord post, double score) {
            this.post = post;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredPost other) {
            return Double.compare(this.score, other.score);
        }
    }
}
