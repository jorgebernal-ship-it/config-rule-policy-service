package co.com.bancolombia.postgresql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "policy_set")
public class PolicySetEntity {
    @Id
    private UUID id;
    
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
