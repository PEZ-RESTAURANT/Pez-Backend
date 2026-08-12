package com.pezbackend.billing.infrastructure.services;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.pezbackend.billing.domain.services.DniRucLookupService;
import com.pezbackend.billing.domain.services.LookupResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Implementación del servicio de lookup que consume la API de apis.net.pe v2.
 * Realiza llamadas HTTP no bloqueantes con un timeout estricto de 3 segundos
 * y maneja fallos de forma silenciosa para asegurar que el flujo de venta no se interrumpa.
 */
@Service
@Slf4j
public class ApisNetPeLookupService implements DniRucLookupService {

    private final String apiToken;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ApisNetPeLookupService(
            @Value("${lookup.api.token:}") String apiToken,
            @Value("${lookup.api.url:https://api.apis.net.pe/v2}") String apiUrl,
            ObjectMapper objectMapper
    ) {
        this.apiToken = apiToken;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public LookupResult lookupDni(String dni) {
        if (dni == null || dni.length() != 8 || !dni.matches("\\d+")) {
            log.warn("Lookup DNI: Formato inválido '{}'", dni);
            return LookupResult.failed();
        }

        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Lookup DNI: API Token no configurado en el servidor.");
            return LookupResult.failed();
        }

        String url = String.format("%s/reniec/dni?numero=%s", apiUrl, dni);
        try {
            log.info("Lookup DNI: Consultando documento {}...", dni);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String nombres = root.path("nombres").asText("");
                String apellidoPaterno = root.path("apellidoPaterno").asText("");
                String apellidoMaterno = root.path("apellidoMaterno").asText("");
                String fullName = String.format("%s %s %s", nombres, apellidoPaterno, apellidoMaterno).trim().replaceAll("\\s+", " ");
                
                log.info("Lookup DNI: Éxito en consulta externa para {}", dni);
                return new LookupResult(dni, fullName, "", true);
            } else {
                log.warn("Lookup DNI: La API externa devolvió código HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Lookup DNI: Error o timeout al consultar proveedor externo para {}: {}", dni, e.getMessage());
        }

        return LookupResult.failed();
    }

    @Override
    public LookupResult lookupRuc(String ruc) {
        if (ruc == null || ruc.length() != 11 || !ruc.matches("\\d+")) {
            log.warn("Lookup RUC: Formato inválido '{}'", ruc);
            return LookupResult.failed();
        }

        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Lookup RUC: API Token no configurado en el servidor.");
            return LookupResult.failed();
        }

        String url = String.format("%s/sunat/ruc?numero=%s", apiUrl, ruc);
        try {
            log.info("Lookup RUC: Consultando documento {}...", ruc);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String razonSocial = root.path("razonSocial").asText("");
                String direccion = root.path("direccion").asText("");
                
                log.info("Lookup RUC: Éxito en consulta externa para {}", ruc);
                return new LookupResult(ruc, razonSocial, direccion, true);
            } else {
                log.warn("Lookup RUC: La API externa devolvió código HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Lookup RUC: Error o timeout al consultar proveedor externo para {}: {}", ruc, e.getMessage());
        }

        return LookupResult.failed();
    }
}
