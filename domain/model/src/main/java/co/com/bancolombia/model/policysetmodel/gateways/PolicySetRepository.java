package co.com.bancolombia.model.policysetmodel.gateways;

import co.com.bancolombia.model.policysetmodel.PolicySet;
import reactor.core.publisher.Mono;

public interface PolicySetRepository {

    Mono<PolicySet> save(PolicySet policySet);

    Mono<PolicySet> findById(String id);

    Mono<PolicySet> findByChannelAndTransactionCode(String channel, String transactionCode);

    Mono<Void> deleteById(String id);
}
