package com.pezbackend.inventory.domain.services;

import com.pezbackend.inventory.domain.model.entities.Supply;

import java.math.BigDecimal;

/**
 * Servicio de comandos para la gestión de insumos y sus niveles de stock.
 */
public interface SupplyCommandService {

    /**
     * Crea un nuevo insumo en el sistema.
     *
     * @param name         nombre único del insumo
     * @param unit         unidad de medida
     * @param minThreshold umbral mínimo de alertas
     * @return el insumo creado
     */
    Supply createSupply(String name, String unit, BigDecimal minThreshold);

    /**
     * Actualiza los datos de un insumo existente.
     *
     * @param id           ID del insumo
     * @param name         nuevo nombre del insumo
     * @param unit         nueva unidad de medida
     * @param minThreshold nuevo umbral mínimo de alertas
     * @return el insumo actualizado
     */
    Supply updateSupply(Long id, String name, String unit, BigDecimal minThreshold);

    /**
     * Elimina un insumo del sistema.
     *
     * @param id ID del insumo
     */
    void deleteSupply(Long id);

    /**
     * Incrementa el stock de un insumo (reabastecimiento).
     *
     * @param id           ID del insumo
     * @param quantity     cantidad a ingresar (debe ser mayor a cero)
     * @param registeredBy usuario ejecutor
     */
    void restock(Long id, BigDecimal quantity, String registeredBy);

    /**
     * Realiza un ajuste de inventario manual.
     *
     * @param id           ID del insumo
     * @param quantity     cantidad de ajuste (puede ser positiva para incrementar o negativa para disminuir)
     * @param reason       justificación obligatoria del ajuste
     * @param registeredBy usuario ejecutor
     */
    void adjustManual(Long id, BigDecimal quantity, String reason, String registeredBy);
}
