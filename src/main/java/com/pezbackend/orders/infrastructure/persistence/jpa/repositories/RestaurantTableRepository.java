package com.pezbackend.orders.infrastructure.persistence.jpa.repositories;

import com.pezbackend.orders.domain.model.entities.RestaurantTable;
import com.pezbackend.orders.domain.model.valueobjects.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de persistencia de la entidad {@link RestaurantTable}.
 */
@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    
    /**
     * Busca una mesa por su número identificador único.
     *
     * @param number número de la mesa
     * @return opcional con la mesa encontrada
     */
    Optional<RestaurantTable> findByNumber(Integer number);

    /**
     * Comprueba si existe una mesa con el número proporcionado.
     *
     * @param number número de mesa
     * @return true si existe, false en caso contrario
     */
    boolean existsByNumber(Integer number);

    /**
     * Recupera todas las mesas que se encuentren en un estado específico.
     *
     * @param status estado de las mesas a filtrar
     * @return listado de mesas
     */
    List<RestaurantTable> findAllByStatus(TableStatus status);
}
