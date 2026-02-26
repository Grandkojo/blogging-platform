package com.blogging_platform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.blogging_platform.repository.TagRepository;
import com.blogging_platform.utils.TagTrie;

import jakarta.annotation.PostConstruct;

/**
 * Optimizer for tag-related operations using advanced DSA.
 */
@Service
public class TagSearchOptimizer {

    private final TagRepository tagRepository;
    private final TagTrie tagTrie = new TagTrie();

    public TagSearchOptimizer(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    public void init() {
        refreshTrie();
    }

    public void refreshTrie() {
        tagRepository.findAll().forEach(t -> tagTrie.insert(t.getTag()));
    }

    public List<String> autocomplete(String prefix) {
        return tagTrie.searchPrefix(prefix);
    }
}
