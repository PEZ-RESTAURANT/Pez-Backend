package com.pezbackend.catalog.domain.model.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clase que representa la clave compuesta para la entidad {@link Recipe}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeId implements Serializable {
    private Long productId;
    private Long supplyId;
}
