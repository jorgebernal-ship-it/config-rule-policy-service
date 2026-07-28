package co.com.bancolombia.postgresql.repository;

import co.com.bancolombia.postgresql.entity.PolicySetEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface PolicySetR2dbcRepository extends R2dbcRepository<PolicySetEntity, String> {
    
    @Query("SELECT * FROM policy_set WHERE channel = :channel AND transaction_code = :transactionCode LIMIT 1")
    Mono<PolicySetEntity> findByChannelAndTransactionCode(String channel, String transactionCode);
}
