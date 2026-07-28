package co.com.bancolombia.api.poc_postgres.dto;

import java.util.Map;

public record RuleResponse(
        String id,
        String name,
        String effect,
        String decisionCode,
        Map<String, Object> target,
        String whenAttribute,
        String whenOperator,
        String whenValue,
        Integer sequence
) {
}
