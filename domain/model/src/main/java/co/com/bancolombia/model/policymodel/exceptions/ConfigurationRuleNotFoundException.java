package co.com.bancolombia.model.policymodel.exceptions;

public class ConfigurationRuleNotFoundException extends RuntimeException {
    
    public ConfigurationRuleNotFoundException(String id) {
        super("Configuration rule not found with id: " + id);
    }
}
