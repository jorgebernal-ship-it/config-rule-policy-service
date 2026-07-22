package co.com.bancolombia.usecase.configurationrule;

import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.exceptions.InvalidSpecException;

import java.util.Map;

public final class SpecValidator {

    private static final String CONTENT_KEY = "content";

    private SpecValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validateSpec(ConfigurationType type, Map<String, Object> spec) {
        if (spec == null) {
            throw new InvalidSpecException("Spec cannot be null");
        }

        if (type == ConfigurationType.POLICY_REGO) {
            Object content = spec.get(CONTENT_KEY);
            if (content == null || !(content instanceof String) || ((String) content).isBlank()) {
                throw new InvalidSpecException(
                        "For type 'policy-rego', spec.content must exist and cannot be blank"
                );
            }
        }
    }
}
