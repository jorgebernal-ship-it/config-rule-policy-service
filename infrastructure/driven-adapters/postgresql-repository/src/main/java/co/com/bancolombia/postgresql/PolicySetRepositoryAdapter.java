package co.com.bancolombia.postgresql;

import co.com.bancolombia.model.policysetmodel.Policy;
import co.com.bancolombia.model.policysetmodel.PolicySet;
import co.com.bancolombia.model.policysetmodel.Rule;
import co.com.bancolombia.model.policysetmodel.gateways.PolicySetRepository;
import co.com.bancolombia.postgresql.entity.PolicyEntity;
import co.com.bancolombia.postgresql.entity.PolicySetEntity;
import co.com.bancolombia.postgresql.entity.RuleEntity;
import co.com.bancolombia.postgresql.mapper.PolicySetMapper;
import co.com.bancolombia.postgresql.repository.PolicyR2dbcRepository;
import co.com.bancolombia.postgresql.repository.PolicySetR2dbcRepository;
import co.com.bancolombia.postgresql.repository.RuleR2dbcRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PolicySetRepositoryAdapter implements PolicySetRepository {

    private final PolicySetR2dbcRepository policySetRepository;
    private final PolicyR2dbcRepository policyRepository;
    private final RuleR2dbcRepository ruleRepository;
    private final PolicySetMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Mono<PolicySet> save(PolicySet policySet) {
        log.info("Saving PolicySet with id: {}", policySet.getId());

        return savePolicySetWithHierarchy(policySet)
                .doOnSuccess(saved -> log.info("Successfully saved PolicySet with id: {}", saved.getId()))
                .doOnError(error -> log.error("Error saving PolicySet: {}", error.getMessage(), error));
    }

    private Mono<PolicySet> savePolicySetWithHierarchy(PolicySet policySet) {
        PolicySetEntity policySetEntity = mapper.toPolicySetEntity(policySet);
        
        // No establecer ID si es null, dejar que PostgreSQL lo genere con gen_random_uuid()
        // Solo establecer si ya existe (para updates)
        
        Instant now = Instant.now();
        if (policySetEntity.getCreatedAt() == null) {
            policySetEntity.setCreatedAt(now);
        }
        policySetEntity.setUpdatedAt(now);
        
        if (policySetEntity.getVersion() == null) {
            policySetEntity.setVersion(1);
        } else {
            policySetEntity.setVersion(policySetEntity.getVersion() + 1);
        }

        return policySetRepository.save(policySetEntity)
                .flatMap(savedPolicySet -> {
                    if (policySet.getPolicies() == null || policySet.getPolicies().isEmpty()) {
                        return Mono.just(mapper.toPolicySet(savedPolicySet));
                    }
                    
                    return Flux.fromIterable(policySet.getPolicies())
                            .flatMap(policy -> savePolicyWithRules(policy, savedPolicySet.getId()))
                            .collectList()
                            .map(policies -> {
                                PolicySet result = mapper.toPolicySet(savedPolicySet);
                                return result.toBuilder().policies(policies).build();
                            });
                });
    }

    private Mono<Policy> savePolicyWithRules(Policy policy, UUID policySetId) {
        PolicyEntity policyEntity = mapper.toPolicyEntity(policy);
        
        // No establecer ID si es null, dejar que PostgreSQL lo genere
        policyEntity.setPolicySetId(policySetId);

        return policyRepository.save(policyEntity)
                .flatMap(savedPolicy -> {
                    if (policy.getRules() == null || policy.getRules().isEmpty()) {
                        return Mono.just(mapper.toPolicy(savedPolicy));
                    }
                    
                    return Flux.fromIterable(policy.getRules())
                            .flatMap(rule -> saveRule(rule, savedPolicy.getId()))
                            .collectList()
                            .map(rules -> {
                                Policy result = mapper.toPolicy(savedPolicy);
                                return result.toBuilder().rules(rules).build();
                            });
                });
    }

    private Mono<Rule> saveRule(Rule rule, UUID policyId) {
        RuleEntity ruleEntity = mapper.toRuleEntity(rule, objectMapper);
        
        // No establecer ID si es null, dejar que PostgreSQL lo genere
        ruleEntity.setPolicyId(policyId);

        return ruleRepository.save(ruleEntity)
                .map(saved -> mapper.toRule(saved, objectMapper));
    }

    @Override
    public Mono<PolicySet> findById(String id) {
        log.info("Finding PolicySet by id: {}", id);

        return policySetRepository.findById(UUID.fromString(id))
                .flatMap(this::loadFullHierarchy)
                .doOnSuccess(found -> {
                    if (found != null) {
                        log.info("PolicySet found with id: {}", id);
                    } else {
                        log.info("PolicySet not found with id: {}", id);
                    }
                });
    }

    @Override
    public Mono<PolicySet> findByChannelAndTransactionCode(String channel, String transactionCode) {
        log.info("Finding PolicySet by channel: {} and transactionCode: {}", channel, transactionCode);

        return policySetRepository.findByChannelAndTransactionCode(channel, transactionCode)
                .flatMap(this::loadFullHierarchy)
                .doOnSuccess(found -> {
                    if (found != null) {
                        log.info("PolicySet found with channel: {} and transactionCode: {}", channel, transactionCode);
                    } else {
                        log.info("PolicySet not found with channel: {} and transactionCode: {}", channel, transactionCode);
                    }
                });
    }

    private Mono<PolicySet> loadFullHierarchy(PolicySetEntity policySetEntity) {
        return policyRepository.findByPolicySetIdOrderBySequenceAsc(policySetEntity.getId())
                .flatMap(policyEntity -> 
                    ruleRepository.findByPolicyIdOrderBySequenceAsc(policyEntity.getId())
                            .map(ruleEntity -> mapper.toRule(ruleEntity, objectMapper))
                            .collectList()
                            .map(rules -> {
                                Policy policy = mapper.toPolicy(policyEntity);
                                return policy.toBuilder().rules(rules).build();
                            })
                )
                .collectList()
                .map(policies -> {
                    PolicySet policySet = mapper.toPolicySet(policySetEntity);
                    return policySet.toBuilder().policies(policies).build();
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(String id) {
        log.info("Deleting PolicySet with id: {}", id);

        return policySetRepository.findById(UUID.fromString(id))
                .flatMap(policySet -> 
                    policyRepository.findByPolicySetIdOrderBySequenceAsc(policySet.getId())
                            .flatMap(policy -> 
                                ruleRepository.findByPolicyIdOrderBySequenceAsc(policy.getId())
                                        .flatMap(ruleRepository::delete)
                                        .then(policyRepository.delete(policy))
                            )
                            .then(policySetRepository.delete(policySet))
                )
                .doOnSuccess(v -> log.info("Successfully deleted PolicySet with id: {}", id))
                .doOnError(error -> log.error("Error deleting PolicySet: {}", error.getMessage(), error));
    }
}
