package com.blogging_platform.classes;

/**
 * Immutable data transfer object for a tag.
 * Contains id and tag name.
 */
public record TagRecord(
    String id,
    String tag
) {}
