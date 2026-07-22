package co.com.bancolombia.dynamodb;

import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.configurationrule.Status;
import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ConfigurationRuleRepositoryAdapter implements ConfigurationRuleGateway {

    private static final String TABLE_NAME = "configuration_rule";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final DynamoDbEnhancedAsyncClient enhancedClient;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<ConfigurationRule> save(ConfigurationRule rule) {
        log.info("Saving configuration rule with id: {}", rule.getId());

        return Mono.fromCompletionStage(() -> {
            DynamoDbAsyncTable<ConfigurationRuleEntity> table = getTable();
            ConfigurationRuleEntity entity = toEntity(rule);
            return table.putItem(entity);
        })
        .doOnSuccess(v -> log.info("Successfully saved configuration rule with id: {}", rule.getId()))
        .thenReturn(rule);
    }

    @Override
    public Mono<ConfigurationRule> findById(String id) {
        log.info("Finding configuration rule by id: {}", id);

        return Mono.fromCompletionStage(() -> {
            DynamoDbAsyncTable<ConfigurationRuleEntity> table = getTable();
            Key key = Key.builder().partitionValue(id).build();
            return table.getItem(key);
        })
        .flatMap(entity -> {
            if (entity == null) {
                log.info("Configuration rule not found with id: {}", id);
                return Mono.empty();
            }
            log.info("Configuration rule found with id: {}", id);
            return Mono.just(toDomain(entity));
        });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        log.info("Deleting configuration rule with id: {}", id);

        return Mono.fromCompletionStage(() -> {
            DynamoDbAsyncTable<ConfigurationRuleEntity> table = getTable();
            Key key = Key.builder().partitionValue(id).build();
            return table.deleteItem(key);
        })
        .doOnSuccess(v -> log.info("Successfully deleted configuration rule with id: {}", id))
        .then();
    }

    private DynamoDbAsyncTable<ConfigurationRuleEntity> getTable() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(ConfigurationRuleEntity.class));
    }

    private ConfigurationRuleEntity toEntity(ConfigurationRule rule) {
        ConfigurationRuleEntity entity = new ConfigurationRuleEntity();
        entity.setId(rule.getId());
        entity.setType(rule.getType().getValue());
        entity.setName(rule.getName());
        entity.setScope(rule.getScope());
        entity.setStatus(rule.getStatus().getValue());
        entity.setSpec(serializeSpec(rule.getSpec()));
        entity.setCreatedAt(rule.getCreatedAt());
        entity.setUpdatedAt(rule.getUpdatedAt());
        entity.setCreatedBy(rule.getCreatedBy());
        entity.setVersion(rule.getVersion());
        return entity;
    }

    private ConfigurationRule toDomain(ConfigurationRuleEntity entity) {
        return ConfigurationRule.builder()
                .id(entity.getId())
                .type(ConfigurationType.fromValue(entity.getType()))
                .name(entity.getName())
                .scope(entity.getScope())
                .status(Status.fromValue(entity.getStatus()))
                .spec(deserializeSpec(entity.getSpec()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .version(entity.getVersion())
                .build();
    }

    private String serializeSpec(Map<String, Object> spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize spec: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> deserializeSpec(String spec) {
        if (spec == null || spec.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(spec, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize spec: " + e.getMessage(), e);
        }
    }
}
