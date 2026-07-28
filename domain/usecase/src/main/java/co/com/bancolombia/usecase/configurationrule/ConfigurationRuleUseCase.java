package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.configurationrule.Status;
import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.exceptions.RawContentNotAvailableException;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ConfigurationRuleUseCase {

    private static final String CONTENT_KEY = "content";

    private final ConfigurationRuleGateway repository;

    public Mono<ConfigurationRule> create(ConfigurationRule rule) {
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

    public Mono<ConfigurationRule> findById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)));
    }

    public Mono<ConfigurationRule> update(String id, ConfigurationRule updatedRule) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)))
                .flatMap(existingRule -> {
                    SpecValidator.validateSpec(updatedRule.getType(), updatedRule.getSpec());

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

    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)))
                .flatMap(rule -> repository.deleteById(id));
    }

    public Mono<String> getRawContent(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)))
                .flatMap(this::extractRawContent);
    }

    private Mono<String> extractRawContent(ConfigurationRule rule) {
        if (rule.getType() != ConfigurationType.POLICY_REGO) {
            return Mono.error(new RawContentNotAvailableException(
                    "Raw content is only available for type 'policy-rego', but rule has type: " + rule.getType().getValue()
            ));
        }

        Map<String, Object> spec = rule.getSpec();
        if (spec == null || !spec.containsKey(CONTENT_KEY)) {
            return Mono.error(new RawContentNotAvailableException(
                    "Raw content not found in spec for rule: " + rule.getId()
            ));
        }

        Object content = spec.get(CONTENT_KEY);
        if (!(content instanceof String) || ((String) content).isBlank()) {
            return Mono.error(new RawContentNotAvailableException(
                    "Raw content is empty or invalid for rule: " + rule.getId()
            ));
        }

        return Mono.just((String) content);
    }
}
