package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteConfigurationRuleUseCase {

    private final ConfigurationRuleGateway repository;

    public Mono<Void> execute(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ConfigurationRuleNotFoundException(id)))
                .flatMap(rule -> repository.deleteById(id));
    }
}
