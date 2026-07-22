package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.exceptions.RawContentNotAvailableException;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
public class GetConfigurationRuleRawUseCase {

    private static final String CONTENT_KEY = "content";

    private final ConfigurationRuleGateway repository;

    public Mono<String> execute(String id) {
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
