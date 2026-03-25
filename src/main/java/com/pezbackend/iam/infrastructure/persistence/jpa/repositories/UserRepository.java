package io.github.rafaviv.panther.pantherbackend.iam.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.panther.pantherbackend.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * User Repository
 * <p>
 * This repository is responsible for managing User entities in the database.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by email
     * @param email the email to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists by email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by workshop ID
     * @param workshopId the workshop ID
     * @return List of users for the workshop
     */
    
} 