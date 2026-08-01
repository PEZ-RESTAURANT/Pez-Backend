package com.pezbackend.iam.infrastructure.persistence.jpa.repositories;

import com.pezbackend.iam.domain.model.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Permission}.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    /**
     * Busca un permiso por su código único.
     *
     * @param code el código del permiso a buscar
     * @return un opcional con el permiso si existe
     */
    Optional<Permission> findByCode(String code);

    /**
     * Verifica si existe un permiso por su código único.
     *
     * @param code el código del permiso a comprobar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCode(String code);
}
