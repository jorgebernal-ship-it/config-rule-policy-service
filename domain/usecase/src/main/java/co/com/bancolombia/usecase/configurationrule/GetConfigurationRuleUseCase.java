package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetConfigurationRuleUseCase {

    private final ConfigurationRuleGateway repository;

    public Mono<ConfigurationRule> execute(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)));
    }
}
