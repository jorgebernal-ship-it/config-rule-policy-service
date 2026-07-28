package co.com.bancolombia.api.poc_postgres.dto;

import java.util.List;

public record PolicyRequest(
        String name,
        String algorithmCombinationRules,
        Integer sequence,
        List<RuleRequest> rules
) {
}
