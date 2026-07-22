package co.com.bancolombia.model.policymodel.gateways;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import reactor.core.publisher.Mono;

public interface ConfigurationRuleGateway {

    Mono<ConfigurationRule> save(ConfigurationRule rule);

    Mono<ConfigurationRule> findById(String id);

    Mono<Void> deleteById(String id);
}
