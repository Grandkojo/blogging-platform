package com.blogging_platform.model;

/**
 * Domain model for a tag. Used when creating tags or linking them to posts.
 */
public class Tag {
    private String id;
    private String tag;

    public Tag(String tag) {
        this.tag = tag;
    }

    public Tag(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
