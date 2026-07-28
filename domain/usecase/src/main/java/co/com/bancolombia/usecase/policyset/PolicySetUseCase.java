package co.com.bancolombia.usecase.policyset;

import co.com.bancolombia.model.policysetmodel.PolicySet;
import co.com.bancolombia.model.policysetmodel.gateways.PolicySetRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class PolicySetUseCase {

    private final PolicySetRepository repository;

    public Mono<PolicySet> create(PolicySet policySet) {
        // No generar ID aquí, dejar que PostgreSQL lo genere con gen_random_uuid()

        Instant now = Instant.now();
        PolicySet policySetToCreate = policySet.toBuilder()
                .id(null)  // Asegurar que el ID sea null para que PostgreSQL lo genere
                .status(policySet.getStatus() != null ? policySet.getStatus() : "ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .version(1)
                .build();

        return repository.save(policySetToCreate);
    }

    public Mono<PolicySet> findById(String id) {
        return repository.findById(id);
    }

    public Mono<PolicySet> findByChannelAndTransactionCode(String channel, String transactionCode) {
        return repository.findByChannelAndTransactionCode(channel, transactionCode);
    }

    public Mono<PolicySet> update(String id, PolicySet policySet) {
        return repository.findById(id)
                .flatMap(existing -> {
                    PolicySet updated = policySet.toBuilder()
                            .id(id)
                            .createdAt(existing.getCreatedAt())
                            .createdBy(existing.getCreatedBy())
                            .updatedAt(Instant.now())
                            .version(existing.getVersion() + 1)
                            .build();
                    
                    return repository.save(updated);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("PolicySet not found with id: " + id)));
    }

    public Mono<Void> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("PolicySet not found with id: " + id)))
                .flatMap(existing -> repository.deleteById(id));
    }
}
