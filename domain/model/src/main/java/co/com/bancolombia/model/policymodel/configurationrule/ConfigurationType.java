package co.com.bancolombia.model.policymodel.configurationrule;

public enum ConfigurationType {
    ROUTING("routing"),
    PIP_SOURCE("pip-source"),
    POST_ACTION("post-action"),
    EVENT("event"),
    POLICY_REGO("policy-rego");

    private final String value;

    ConfigurationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConfigurationType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Configuration type cannot be null");
        }
        for (ConfigurationType type : ConfigurationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown configuration type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
