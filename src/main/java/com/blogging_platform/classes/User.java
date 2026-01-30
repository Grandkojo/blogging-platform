package com.blogging_platform.classes;

/**
 * Immutable record representing a user (id, name, email, role).
 * Used in the classes package for session/display.
 */
public record User(
    String id,
    String name,
    String email,
    String role
) {}

