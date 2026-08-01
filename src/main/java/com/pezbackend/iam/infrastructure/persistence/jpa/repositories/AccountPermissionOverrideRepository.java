package com.pezbackend.iam.infrastructure.persistence.jpa.repositories;

import com.pezbackend.iam.domain.model.entities.AccountPermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link AccountPermissionOverride}.
 */
@Repository
public interface AccountPermissionOverrideRepository extends JpaRepository<AccountPermissionOverride, Long> {
    /**
     * Busca una anulación para un usuario y permiso específicos.
     *
     * @param userId       el identificador del usuario
     * @param permissionId el identificador del permiso
     * @return un opcional con la anulación
     */
    Optional<AccountPermissionOverride> findByUserIdAndPermissionId(Long userId, Long permissionId);

    /**
     * Busca una anulación para un usuario y código de permiso específicos.
     *
     * @param userId         el identificador del usuario
     * @param permissionCode el código de permiso a buscar
     * @return un opcional con la anulación
     */
    Optional<AccountPermissionOverride> findByUserIdAndPermissionCode(Long userId, String permissionCode);

    /**
     * Obtiene el listado completo de anulaciones de permisos de un usuario específico.
     *
     * @param userId el identificador del usuario
     * @return lista de anulaciones registradas para el usuario
     */
    List<AccountPermissionOverride> findAllByUserId(Long userId);
}
