package com.pezbackend.iam.infrastructure.persistence.jpa.repositories;

import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role Repository
 * <p>
 * This repository is responsible for managing Role entities in the database.
 * </p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find a role by name
     * @param name the role name to search for
     * @return Optional containing the role if found, empty otherwise
     */
    Optional<Role> findByName(Roles name);
    
    /**
     * Check if a role exists by name
     * @param name the role name to check
     * @return true if role exists, false otherwise
     */
    boolean existsByName(Roles name);
}

