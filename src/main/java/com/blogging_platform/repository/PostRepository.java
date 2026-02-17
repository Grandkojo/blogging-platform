package com.blogging_platform.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blogging_platform.model.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {
    
    Optional<Post> findByIdAndUser_Id(UUID id, UUID userId);

    /**
     * Searches posts by title, author name, or tag name (case-insensitive),
     * returning a paginated and sortable result set.
     */
    @Query("""
        select distinct p from Post p
        left join p.user u
        left join p.tags t
        where (:query is null or :query = '')
           or lower(p.title) like lower(concat('%', :query, '%'))
           or lower(u.name) like lower(concat('%', :query, '%'))
           or lower(t.tag)  like lower(concat('%', :query, '%'))
        """)
    Page<Post> searchByTitleAuthorOrTag(@Param("query") String query, Pageable pageable);
}
