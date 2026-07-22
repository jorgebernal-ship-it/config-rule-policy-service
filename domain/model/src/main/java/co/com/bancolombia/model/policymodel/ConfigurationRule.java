package co.com.bancolombia.model.policymodel;

import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.configurationrule.Status;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ConfigurationRule {
    private final String id;
    private final ConfigurationType type;
    private final String name;
    private final String scope;
    private final Status status;
    private final Map<String, Object> spec;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String createdBy;
    private final int version;
}
