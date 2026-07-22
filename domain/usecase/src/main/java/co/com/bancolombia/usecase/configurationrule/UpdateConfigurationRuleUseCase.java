package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdateConfigurationRuleUseCase {

    private final ConfigurationRuleGateway repository;

    public Mono<ConfigurationRule> execute(String id, ConfigurationRule updatedRule) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)))
                .flatMap(existingRule -> {
                    ConfigurationRule ruleToUpdate = ConfigurationRule.builder()
                            .id(existingRule.getId())
                            .type(updatedRule.getType())
                            .name(updatedRule.getName())
                            .scope(updatedRule.getScope())
                            .status(updatedRule.getStatus())
                            .spec(updatedRule.getSpec())
                            .createdAt(existingRule.getCreatedAt())
                            .updatedAt(Instant.now())
                            .createdBy(existingRule.getCreatedBy())
                            .version(existingRule.getVersion() + 1)
                            .build();

                    return repository.save(ruleToUpdate);
                });
    }
}
