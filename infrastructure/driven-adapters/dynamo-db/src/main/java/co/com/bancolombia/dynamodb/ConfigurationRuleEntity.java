package co.com.bancolombia.dynamodb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@DynamoDbBean
@Setter
@NoArgsConstructor
public class ConfigurationRuleEntity {

    @Getter(onMethod_ = {@DynamoDbPartitionKey, @DynamoDbAttribute("id")})
    private String id;

    @Getter(onMethod_ = {@DynamoDbAttribute("type")})
    private String type;

    @Getter(onMethod_ = {@DynamoDbAttribute("name")})
    private String name;

    @Getter(onMethod_ = {@DynamoDbAttribute("scope")})
    private String scope;

    @Getter(onMethod_ = {@DynamoDbAttribute("status")})
    private String status;

    // spec se almacena como JSON string para evitar problemas con Map<String, Object>
    @Getter(onMethod_ = {@DynamoDbAttribute("spec")})
    private String spec;

    @Getter(onMethod_ = {@DynamoDbAttribute("createdAt")})
    private Instant createdAt;

    @Getter(onMethod_ = {@DynamoDbAttribute("updatedAt")})
    private Instant updatedAt;

    @Getter(onMethod_ = {@DynamoDbAttribute("createdBy")})
    private String createdBy;

    @Getter(onMethod_ = {@DynamoDbAttribute("version")})
    private Integer version;
}
