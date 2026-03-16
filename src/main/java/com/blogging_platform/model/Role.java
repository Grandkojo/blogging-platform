package com.blogging_platform.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enumeration of the three user roles in the platform.
 * <ul>
 *   <li>{@link #ADMIN}  – full platform access</li>
 *   <li>{@link #AUTHOR} – can create and manage own posts</li>
 *   <li>{@link #READER} – read and interact (comments, reviews)</li>
 * </ul>
 *
 * <p>A {@link JsonCreator} factory allows case-insensitive deserialization and
 * handles legacy string values stored in older records ("Admin", "Author",
 * "Regular", "User").</p>
 */
public enum Role {

    ADMIN,
    AUTHOR,
    READER;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) {
            return READER;
        }
        return switch (value.trim().toUpperCase()) {
            case "ADMIN"            -> ADMIN;
            case "AUTHOR"           -> AUTHOR;
            case "READER",
                 "USER",
                 "REGULAR"          -> READER;
            default -> throw new IllegalArgumentException(
                    "Unknown role '" + value + "'. Accepted values: ADMIN, AUTHOR, READER");
        };
    }
}
