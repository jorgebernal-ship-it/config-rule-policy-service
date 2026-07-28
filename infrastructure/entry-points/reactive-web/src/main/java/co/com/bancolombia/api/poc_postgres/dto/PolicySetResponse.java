package co.com.bancolombia.api.poc_postgres.dto;

import java.time.Instant;
import java.util.List;

public record PolicySetResponse(
        String id,
        String name,
        String channel,
        String transactionCode,
        String algorithmCombination,
        String status,
        Integer version,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        List<PolicyResponse> policies
) {
}
