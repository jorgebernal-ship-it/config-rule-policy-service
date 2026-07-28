package co.com.bancolombia.postgresql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("policy")
public class PolicyEntity {
    @Id
    private String id;
    
    @Column("policy_set_id")
    private String policySetId;
    
    @Column("name")
    private String name;
    
    @Column("algorithm_combination_rules")
    private String algorithmCombinationRules;
    
    @Column("sequence")
    private Integer sequence;
}
