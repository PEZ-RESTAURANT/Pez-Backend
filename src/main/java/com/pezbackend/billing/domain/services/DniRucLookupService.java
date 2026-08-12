package com.pezbackend.billing.domain.services;

/**
 * Interfaz para la consulta externa de información de documentos (DNI / RUC).
 */
public interface DniRucLookupService {
    
    /**
     * Consulta información asociada a un DNI.
     *
     * @param dni número de DNI a consultar (8 dígitos)
     * @return resultado de la búsqueda
     */
    LookupResult lookupDni(String dni);

    /**
     * Consulta información asociada a un RUC.
     *
     * @param ruc número de RUC a consultar (11 dígitos)
     * @return resultado de la búsqueda
     */
    LookupResult lookupRuc(String ruc);
}
