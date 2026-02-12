package com.blogging_platform.model;

import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Domain model for a tag. Used when creating tags or linking them to posts.
 * Mapped as a JPA entity with a many-to-many relationship to {@link Post}.
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(Types.BINARY)
    private UUID id;
    
    @NotBlank(message = "Tag name is required")
    private String tag;

    /**
     * Posts that are associated with this tag.
     * Managed by the owning side in {@link Post#tags}.
     */
    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();

    public Tag(){}

    public Tag(String tag) {
        this.tag = tag;
    }

    public Tag(UUID id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public void setPosts(Set<Post> posts) {
        this.posts = posts;
    }
}
