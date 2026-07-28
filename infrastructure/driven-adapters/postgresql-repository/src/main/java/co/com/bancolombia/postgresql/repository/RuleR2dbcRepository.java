package co.com.bancolombia.postgresql.repository;

import co.com.bancolombia.postgresql.entity.RuleEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RuleR2dbcRepository extends R2dbcRepository<RuleEntity, UUID> {
    
    Flux<RuleEntity> findByPolicyIdOrderBySequenceAsc(UUID policyId);
}
