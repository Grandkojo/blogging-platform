package com.blogging_platform.classes;

import java.util.UUID;

/**
 * Immutable data transfer object for a user (e.g. after login).
 * Contains id, name, email, and role.
 */
public record UserRecord(
    UUID id,
    String name,
    String email,
    String role
) {}

