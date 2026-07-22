package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Request para crear o actualizar una regla de configuración")
public record ConfigurationRuleRequest(
        @Schema(description = "Tipo de configuración",
                example = "routing",
                allowableValues = {"routing", "pip-source", "post-action", "event", "policy-rego"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

        @Schema(description = "Nombre descriptivo de la regla",
                example = "Payment API Routing",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(description = "Ámbito o dominio de la regla",
                example = "payment-domain",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String scope,

        @Schema(description = "Estado de la regla",
                example = "active",
                allowableValues = {"active", "inactive"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status,

        @Schema(description = "Especificación de la regla (estructura varía según el tipo). Para policy-rego, debe incluir el campo 'content'.",
                example = "{\"matcher\":{\"path\":\"/api/v1/payments\"},\"target\":{\"url\":\"http://payment-service:8080\"}}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, Object> spec
) {}
