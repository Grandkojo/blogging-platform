package com.blogging_platform.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple Trie (Prefix Tree) implementation for efficient string retrieval.
 */
public class TagTrie {

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
        List<String> tagsAtThisPrefix = new ArrayList<>(); // For fast retrieval of completions
    }

    private final TrieNode root = new TrieNode();

    public void insert(String tag) {
        TrieNode current = root;
        for (char ch : tag.toLowerCase().toCharArray()) {
            current = current.children.computeIfAbsent(ch, k -> new TrieNode());
            if (!current.tagsAtThisPrefix.contains(tag)) {
                current.tagsAtThisPrefix.add(tag);
            }
        }
        current.isEndOfWord = true;
    }

    public List<String> searchPrefix(String prefix) {
        TrieNode current = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return List.of();
            }
        }
        return current.tagsAtThisPrefix;
    }
}
