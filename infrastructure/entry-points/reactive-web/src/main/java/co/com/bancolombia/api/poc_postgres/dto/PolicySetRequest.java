package co.com.bancolombia.api.poc_postgres.dto;

import java.util.List;

public record PolicySetRequest(
        String name,
        String channel,
        String transactionCode,
        String algorithmCombination,
        String status,
        String createdBy,
        List<PolicyRequest> policies
) {
}
