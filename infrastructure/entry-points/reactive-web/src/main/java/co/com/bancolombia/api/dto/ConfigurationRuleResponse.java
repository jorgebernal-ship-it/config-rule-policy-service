package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Response con todos los campos de una regla de configuración")
public record ConfigurationRuleResponse(
        @Schema(description = "ID único de la regla (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Tipo de configuración", example = "routing")
        String type,

        @Schema(description = "Nombre descriptivo de la regla", example = "Payment API Routing")
        String name,

        @Schema(description = "Ámbito o dominio de la regla", example = "payment-domain")
        String scope,

        @Schema(description = "Estado de la regla", example = "active")
        String status,

        @Schema(description = "Especificación de la regla (estructura varía según el tipo)",
                example = "{\"matcher\":{\"path\":\"/api/v1/payments\"},\"target\":{\"url\":\"http://payment-service:8080\"}}")
        Map<String, Object> spec,

        @Schema(description = "Fecha de creación (se preserva en UPDATE)", example = "2026-07-17T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Fecha de última actualización", example = "2026-07-17T10:30:00Z")
        Instant updatedAt,

        @Schema(description = "Usuario que creó la regla (se preserva en UPDATE)", example = "admin@bancolombia.com", nullable = true)
        String createdBy,

        @Schema(description = "Número de versión (se incrementa automáticamente con cada UPDATE)", example = "1")
        int version
) {}
