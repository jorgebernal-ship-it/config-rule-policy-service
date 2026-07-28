package co.com.bancolombia.postgresql.repository;

import co.com.bancolombia.postgresql.entity.PolicyEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface PolicyR2dbcRepository extends R2dbcRepository<PolicyEntity, String> {
    
    Flux<PolicyEntity> findByPolicySetIdOrderBySequenceAsc(String policySetId);
}
