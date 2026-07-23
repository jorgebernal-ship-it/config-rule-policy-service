package co.com.bancolombia.model.policymodel;

import co.com.bancolombia.model.policymodel.configurationrule.RuleSpec;
import co.com.bancolombia.model.policymodel.configurationrule.RuleStatus;
import co.com.bancolombia.model.policymodel.configurationrule.RuleType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PolicyModel {
    private String id;
    private RuleType type;
    private String name;
    private String scope;
    private RuleStatus status;
    private RuleSpec spec;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private Integer version;
}

