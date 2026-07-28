package co.com.bancolombia.postgresql.repository;

import co.com.bancolombia.postgresql.entity.PolicyEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PolicyR2dbcRepository extends R2dbcRepository<PolicyEntity, UUID> {
    
    Flux<PolicyEntity> findByPolicySetIdOrderBySequenceAsc(UUID policySetId);
}
