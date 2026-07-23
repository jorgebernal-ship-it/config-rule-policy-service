package co.com.bancolombia.model.policymodel.gateways;

import co.com.bancolombia.model.policymodel.PolicyModel;
import co.com.bancolombia.model.policymodel.configurationrule.RuleType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyModelRepository {

    Mono<PolicyModel> save(PolicyModel rule);

    Mono<PolicyModel> findById(String id);

    Flux<PolicyModel> findByType(RuleType type);

    Flux<PolicyModel> findByScope(String scope);

    Mono<Void> deleteById(String id);

    Mono<PolicyModel> saveInGit(PolicyModel rule);
    Mono<String> readFromConfigServer(String path);
}
