package co.com.bancolombia.api.poc_postgres.mapper;

import co.com.bancolombia.api.poc_postgres.dto.*;
import co.com.bancolombia.model.policysetmodel.Policy;
import co.com.bancolombia.model.policysetmodel.PolicySet;
import co.com.bancolombia.model.policysetmodel.Rule;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PolicySetDTOMapper {

    public PolicySet toDomain(PolicySetRequest request) {
        if (request == null) {
            return null;
        }

        List<Policy> policies = request.policies() != null
                ? request.policies().stream()
                .map(this::toDomain)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return PolicySet.builder()
                .name(request.name())
                .channel(request.channel())
                .transactionCode(request.transactionCode())
                .algorithmCombination(request.algorithmCombination())
                .status(request.status() != null ? request.status() : "ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(request.createdBy())
                .version(1)
                .policies(policies)
                .build();
    }

    public Policy toDomain(PolicyRequest request) {
        if (request == null) {
            return null;
        }

        List<Rule> rules = request.rules() != null
                ? request.rules().stream()
                .map(this::toDomain)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return Policy.builder()
                .name(request.name())
                .algorithmCombinationRules(request.algorithmCombinationRules())
                .sequence(request.sequence())
                .rules(rules)
                .build();
    }

    public Rule toDomain(RuleRequest request) {
        if (request == null) {
            return null;
        }

        return Rule.builder()
                .name(request.name())
                .effect(request.effect())
                .decisionCode(request.decisionCode())
                .target(request.target())
                .whenAttribute(request.whenAttribute())
                .whenOperator(request.whenOperator())
                .whenValue(request.whenValue())
                .sequence(request.sequence())
                .build();
    }

    public PolicySetResponse toResponse(PolicySet policySet) {
        if (policySet == null) {
            return null;
        }

        List<PolicyResponse> policies = policySet.getPolicies() != null
                ? policySet.getPolicies().stream()
                .map(this::toResponse)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new PolicySetResponse(
                policySet.getId(),
                policySet.getName(),
                policySet.getChannel(),
                policySet.getTransactionCode(),
                policySet.getAlgorithmCombination(),
                policySet.getStatus(),
                policySet.getVersion(),
                policySet.getCreatedAt(),
                policySet.getUpdatedAt(),
                policySet.getCreatedBy(),
                policies
        );
    }

    public PolicyResponse toResponse(Policy policy) {
        if (policy == null) {
            return null;
        }

        List<RuleResponse> rules = policy.getRules() != null
                ? policy.getRules().stream()
                .map(this::toResponse)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new PolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getAlgorithmCombinationRules(),
                policy.getSequence(),
                rules
        );
    }

    public RuleResponse toResponse(Rule rule) {
        if (rule == null) {
            return null;
        }

        return new RuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getEffect(),
                rule.getDecisionCode(),
                rule.getTarget(),
                rule.getWhenAttribute(),
                rule.getWhenOperator(),
                rule.getWhenValue(),
                rule.getSequence()
        );
    }
}
