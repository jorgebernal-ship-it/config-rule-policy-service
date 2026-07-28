package co.com.bancolombia.api.poc_postgres.dto;

import java.util.List;

public record PolicyResponse(
        String id,
        String name,
        String algorithmCombinationRules,
        Integer sequence,
        List<RuleResponse> rules
) {
}
