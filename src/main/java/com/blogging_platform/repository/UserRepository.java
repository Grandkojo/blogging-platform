package com.blogging_platform.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blogging_platform.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Validates credentials and returns the user record if successful.
     *
     * @param email    user email
     * @return the user record (id, name, email, role), or null if credentials are invalid
     */
    Optional<User> findByEmail(@Param("email") String email);
}
