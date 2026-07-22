package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.configurationrule.Status;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateConfigurationRuleUseCase {

    private final ConfigurationRuleGateway repository;

    public Mono<ConfigurationRule> execute(ConfigurationRule rule) {
        String id = rule.getId() != null ? rule.getId() : UUID.randomUUID().toString();

        SpecValidator.validateSpec(rule.getType(), rule.getSpec());

        Instant now = Instant.now();
        ConfigurationRule ruleToCreate = ConfigurationRule.builder()
                .id(id)
                .type(rule.getType())
                .name(rule.getName())
                .scope(rule.getScope())
                .status(rule.getStatus() != null ? rule.getStatus() : Status.ACTIVE)
                .spec(rule.getSpec())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(rule.getCreatedBy())
                .version(1)
                .build();

        return repository.save(ruleToCreate);
    }
}
