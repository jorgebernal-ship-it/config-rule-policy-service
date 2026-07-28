package co.com.bancolombia.postgresql;

import co.com.bancolombia.model.policysetmodel.Policy;
import co.com.bancolombia.model.policysetmodel.PolicySet;
import co.com.bancolombia.model.policysetmodel.Rule;
import co.com.bancolombia.postgresql.entity.PolicyEntity;
import co.com.bancolombia.postgresql.entity.PolicySetEntity;
import co.com.bancolombia.postgresql.entity.RuleEntity;
import co.com.bancolombia.postgresql.mapper.PolicySetMapper;
import co.com.bancolombia.postgresql.repository.PolicyR2dbcRepository;
import co.com.bancolombia.postgresql.repository.PolicySetR2dbcRepository;
import co.com.bancolombia.postgresql.repository.RuleR2dbcRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicySetRepositoryAdapterTest {

    @Mock
    private PolicySetR2dbcRepository policySetRepository;

    @Mock
    private PolicyR2dbcRepository policyRepository;

    @Mock
    private RuleR2dbcRepository ruleRepository;

    @Mock
    private PolicySetMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PolicySetRepositoryAdapter adapter;

    private PolicySet testPolicySet;
    private PolicySetEntity testPolicySetEntity;

    @BeforeEach
    void setUp() {
        testPolicySet = PolicySet.builder()
                .id("ps-001")
                .name("Test Policy Set")
                .channel("MOBILE")
                .transactionCode("TXN-001")
                .algorithmCombination("FIRST_APPLICABLE")
                .status("ACTIVE")
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("admin")
                .policies(List.of())
                .build();

        testPolicySetEntity = PolicySetEntity.builder()
                .id("ps-001")
                .name("Test Policy Set")
                .channel("MOBILE")
                .transactionCode("TXN-001")
                .algorithmCombination("FIRST_APPLICABLE")
                .status("ACTIVE")
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("admin")
                .build();
    }

    @Test
    void testFindById_Success() {
        PolicySet basePolicySet = PolicySet.builder()
                .id("ps-001")
                .name("Test Policy Set")
                .channel("MOBILE")
                .transactionCode("TXN-001")
                .algorithmCombination("FIRST_APPLICABLE")
                .status("ACTIVE")
                .version(1)
                .createdAt(testPolicySetEntity.getCreatedAt())
                .updatedAt(testPolicySetEntity.getUpdatedAt())
                .createdBy("admin")
                .policies(List.of())
                .build();

        when(policySetRepository.findById("ps-001")).thenReturn(Mono.just(testPolicySetEntity));
        when(policyRepository.findByPolicySetIdOrderBySequenceAsc("ps-001")).thenReturn(Flux.empty());
        when(mapper.toPolicySet(testPolicySetEntity)).thenReturn(basePolicySet);

        StepVerifier.create(adapter.findById("ps-001"))
                .expectNextMatches(policySet -> 
                        policySet.getId().equals("ps-001") &&
                        policySet.getName().equals("Test Policy Set") &&
                        policySet.getChannel().equals("MOBILE")
                )
                .verifyComplete();
    }

    @Test
    void testFindById_NotFound() {
        when(policySetRepository.findById("non-existent")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("non-existent"))
                .verifyComplete();
    }

    @Test
    void testFindByChannelAndTransactionCode_Success() {
        PolicySet basePolicySet = PolicySet.builder()
                .id("ps-001")
                .name("Test Policy Set")
                .channel("MOBILE")
                .transactionCode("TXN-001")
                .algorithmCombination("FIRST_APPLICABLE")
                .status("ACTIVE")
                .version(1)
                .createdAt(testPolicySetEntity.getCreatedAt())
                .updatedAt(testPolicySetEntity.getUpdatedAt())
                .createdBy("admin")
                .policies(List.of())
                .build();

        when(policySetRepository.findByChannelAndTransactionCode("MOBILE", "TXN-001"))
                .thenReturn(Mono.just(testPolicySetEntity));
        when(policyRepository.findByPolicySetIdOrderBySequenceAsc("ps-001")).thenReturn(Flux.empty());
        when(mapper.toPolicySet(testPolicySetEntity)).thenReturn(basePolicySet);

        StepVerifier.create(adapter.findByChannelAndTransactionCode("MOBILE", "TXN-001"))
                .expectNextMatches(policySet -> 
                        policySet.getChannel().equals("MOBILE") &&
                        policySet.getTransactionCode().equals("TXN-001")
                )
                .verifyComplete();
    }

    @Test
    void testDeleteById_Success() {
        when(policySetRepository.findById("ps-001")).thenReturn(Mono.just(testPolicySetEntity));
        when(policyRepository.findByPolicySetIdOrderBySequenceAsc("ps-001")).thenReturn(Flux.empty());
        when(policySetRepository.delete(any(PolicySetEntity.class))).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById("ps-001"))
                .verifyComplete();
    }
}
