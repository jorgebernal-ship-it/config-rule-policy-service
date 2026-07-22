package co.com.bancolombia.model.policymodel.configurationrule;

public enum Status {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Status fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        for (Status status : Status.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
