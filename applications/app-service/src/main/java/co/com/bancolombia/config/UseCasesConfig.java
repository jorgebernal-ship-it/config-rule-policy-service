package co.com.bancolombia.config;

import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import co.com.bancolombia.usecase.configurationrule.ConfigurationRuleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public ConfigurationRuleUseCase configurationRuleUseCase(ConfigurationRuleGateway gateway) {
        return new ConfigurationRuleUseCase(gateway);
    }
}
