package co.com.bancolombia.postgresql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import io.r2dbc.postgresql.codec.Json;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule")
public class RuleEntity {
    @Id
    private UUID id;
    
    @Column("policy_id")
    private UUID policyId;
    
    @Column("name")
    private String name;
    
    @Column("effect")
    private String effect;
    
    @Column("decision_code")
    private String decisionCode;
    
    @Column("target")
    private Json target;
    
    @Column("when_attribute")
    private String whenAttribute;
    
    @Column("when_operator")
    private String whenOperator;
    
    @Column("when_value")
    private String whenValue;
    
    @Column("sequence")
    private Integer sequence;
}
