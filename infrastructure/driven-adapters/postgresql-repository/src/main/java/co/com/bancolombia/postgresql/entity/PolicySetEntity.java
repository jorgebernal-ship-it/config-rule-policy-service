package co.com.bancolombia.postgresql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("policy_set")
public class PolicySetEntity {
    @Id
    private String id;
    
    @Column("name")
    private String name;
    
    @Column("channel")
    private String channel;
    
    @Column("transaction_code")
    private String transactionCode;
    
    @Column("algorithm_combination")
    private String algorithmCombination;
    
    @Column("status")
    private String status;
    
    @Column("version")
    private Integer version;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
    
    @Column("created_by")
    private String createdBy;
}
