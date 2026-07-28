package co.com.bancolombia.postgresql.repository;

import co.com.bancolombia.postgresql.entity.RuleEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface RuleR2dbcRepository extends R2dbcRepository<RuleEntity, String> {
    
    Flux<RuleEntity> findByPolicyIdOrderBySequenceAsc(String policyId);
}
