package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.ConfigurationRuleRequest;
import co.com.bancolombia.api.dto.ConfigurationRuleResponse;
import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.configurationrule.Status;

public final class ConfigurationRuleMapper {

    private ConfigurationRuleMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ConfigurationRule toDomain(ConfigurationRuleRequest dto) {
        return ConfigurationRule.builder()
                .type(ConfigurationType.fromValue(dto.type()))
                .name(dto.name())
                .scope(dto.scope())
                .status(Status.fromValue(dto.status()))
                .spec(dto.spec())
                .build();
    }

    public static ConfigurationRuleResponse toResponseDTO(ConfigurationRule rule) {
        return new ConfigurationRuleResponse(
                rule.getId(),
                rule.getType().getValue(),
                rule.getName(),
                rule.getScope(),
                rule.getStatus().getValue(),
                rule.getSpec(),
                rule.getCreatedAt(),
                rule.getUpdatedAt(),
                rule.getCreatedBy(),
                rule.getVersion()
        );
    }
}
