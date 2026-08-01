package com.pezbackend.iam.infrastructure.persistence.jpa.repositories;

import com.pezbackend.iam.domain.model.entities.RolePermissionDefault;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link RolePermissionDefault}.
 */
@Repository
public interface RolePermissionDefaultRepository extends JpaRepository<RolePermissionDefault, Long> {
    /**
     * Busca el valor por defecto para un rol y permiso concretos.
     *
     * @param role         el rol de IAM
     * @param permissionId el ID de permiso
     * @return un opcional con la regla por defecto
     */
    Optional<RolePermissionDefault> findByRoleAndPermissionId(Roles role, Long permissionId);

    /**
     * Verifica si existe alguna regla por defecto que conceda el permiso para alguno de los roles proporcionados.
     *
     * @param permissionId el ID del permiso
     * @param roles        la colección de roles a comprobar
     * @return true si al menos un rol tiene concedido el permiso, false en caso contrario
     */
    boolean existsByPermissionIdAndRoleInAndGrantedTrue(Long permissionId, Collection<Roles> roles);
}
