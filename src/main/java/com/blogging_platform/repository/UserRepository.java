package com.blogging_platform.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blogging_platform.model.User;

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
     @Query("SELECT new com.blogging_platform.classes.UserRecord(u.id, u.name, u.email, u.role) " +
           "FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}
