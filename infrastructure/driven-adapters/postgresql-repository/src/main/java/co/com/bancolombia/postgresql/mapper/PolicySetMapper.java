package co.com.bancolombia.postgresql.mapper;

import co.com.bancolombia.model.policysetmodel.Policy;
import co.com.bancolombia.model.policysetmodel.PolicySet;
import co.com.bancolombia.model.policysetmodel.Rule;
import co.com.bancolombia.postgresql.entity.PolicyEntity;
import co.com.bancolombia.postgresql.entity.PolicySetEntity;
import co.com.bancolombia.postgresql.entity.RuleEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class PolicySetMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    public PolicySetEntity toPolicySetEntity(PolicySet policySet) {
        if (policySet == null) {
            return null;
        }

        return PolicySetEntity.builder()
                .id(policySet.getId())
                .name(policySet.getName())
                .channel(policySet.getChannel())
                .transactionCode(policySet.getTransactionCode())
                .algorithmCombination(policySet.getAlgorithmCombination())
                .status(policySet.getStatus())
                .version(policySet.getVersion())
                .createdAt(policySet.getCreatedAt())
                .updatedAt(policySet.getUpdatedAt())
                .createdBy(policySet.getCreatedBy())
                .build();
    }

    public PolicySet toPolicySet(PolicySetEntity entity) {
        if (entity == null) {
            return null;
        }

        return PolicySet.builder()
                .id(entity.getId())
                .name(entity.getName())
                .channel(entity.getChannel())
                .transactionCode(entity.getTransactionCode())
                .algorithmCombination(entity.getAlgorithmCombination())
                .status(entity.getStatus())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .policies(Collections.emptyList())
                .build();
    }

    public PolicyEntity toPolicyEntity(Policy policy) {
        if (policy == null) {
            return null;
        }

        return PolicyEntity.builder()
                .id(policy.getId())
                .policySetId(policy.getPolicySetId())
                .name(policy.getName())
                .algorithmCombinationRules(policy.getAlgorithmCombinationRules())
                .sequence(policy.getSequence())
                .build();
    }

    public Policy toPolicy(PolicyEntity entity) {
        if (entity == null) {
            return null;
        }

        return Policy.builder()
                .id(entity.getId())
                .policySetId(entity.getPolicySetId())
                .name(entity.getName())
                .algorithmCombinationRules(entity.getAlgorithmCombinationRules())
                .sequence(entity.getSequence())
                .rules(Collections.emptyList())
                .build();
    }

    public RuleEntity toRuleEntity(Rule rule, ObjectMapper objectMapper) {
        if (rule == null) {
            return null;
        }

        Json targetJson = null;
        if (rule.getTarget() != null && !rule.getTarget().isEmpty()) {
            try {
                String jsonString = objectMapper.writeValueAsString(rule.getTarget());
                targetJson = Json.of(jsonString);
            } catch (JsonProcessingException e) {
                log.error("Error serializing target to JSON: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Failed to serialize target", e);
            }
        }

        return RuleEntity.builder()
                .id(rule.getId())
                .policyId(rule.getPolicyId())
                .name(rule.getName())
                .effect(rule.getEffect())
                .decisionCode(rule.getDecisionCode())
                .target(targetJson)
                .whenAttribute(rule.getWhenAttribute())
                .whenOperator(rule.getWhenOperator())
                .whenValue(rule.getWhenValue())
                .sequence(rule.getSequence())
                .build();
    }

    public Rule toRule(RuleEntity entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> targetMap = Collections.emptyMap();
        if (entity.getTarget() != null) {
            try {
                targetMap = objectMapper.readValue(entity.getTarget().asString(), MAP_TYPE);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing target from JSON: {}", e.getMessage(), e);
            }
        }

        return Rule.builder()
                .id(entity.getId())
                .policyId(entity.getPolicyId())
                .name(entity.getName())
                .effect(entity.getEffect())
                .decisionCode(entity.getDecisionCode())
                .target(targetMap)
                .whenAttribute(entity.getWhenAttribute())
                .whenOperator(entity.getWhenOperator())
                .whenValue(entity.getWhenValue())
                .sequence(entity.getSequence())
                .build();
    }
}
