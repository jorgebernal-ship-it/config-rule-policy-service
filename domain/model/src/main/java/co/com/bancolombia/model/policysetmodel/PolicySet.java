package co.com.bancolombia.model.policysetmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PolicySet {
    private String id;
    private String name;
    private String channel;
    private String transactionCode;
    private String algorithmCombination;
    private String status;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private List<Policy> policies;
}
