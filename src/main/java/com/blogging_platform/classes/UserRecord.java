package com.blogging_platform.classes;

/**
 * Immutable data transfer object for a user (e.g. after login).
 * Contains id, name, email, and role.
 */
public record UserRecord(
    String id,
    String name,
    String email,
    String role
) {}

