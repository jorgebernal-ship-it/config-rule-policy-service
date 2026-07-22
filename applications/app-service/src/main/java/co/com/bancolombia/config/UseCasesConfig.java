package co.com.bancolombia.config;

import co.com.bancolombia.model.policymodel.gateways.ConfigurationRuleGateway;
import co.com.bancolombia.usecase.configurationrule.CreateConfigurationRuleUseCase;
import co.com.bancolombia.usecase.configurationrule.DeleteConfigurationRuleUseCase;
import co.com.bancolombia.usecase.configurationrule.GetConfigurationRuleRawUseCase;
import co.com.bancolombia.usecase.configurationrule.GetConfigurationRuleUseCase;
import co.com.bancolombia.usecase.configurationrule.UpdateConfigurationRuleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public CreateConfigurationRuleUseCase createConfigurationRuleUseCase(ConfigurationRuleGateway gateway) {
        return new CreateConfigurationRuleUseCase(gateway);
    }

    @Bean
    public GetConfigurationRuleUseCase getConfigurationRuleUseCase(ConfigurationRuleGateway gateway) {
        return new GetConfigurationRuleUseCase(gateway);
    }

    @Bean
    public GetConfigurationRuleRawUseCase getConfigurationRuleRawUseCase(ConfigurationRuleGateway gateway) {
        return new GetConfigurationRuleRawUseCase(gateway);
    }

    @Bean
    public UpdateConfigurationRuleUseCase updateConfigurationRuleUseCase(ConfigurationRuleGateway gateway) {
        return new UpdateConfigurationRuleUseCase(gateway);
    }

    @Bean
    public DeleteConfigurationRuleUseCase deleteConfigurationRuleUseCase(ConfigurationRuleGateway gateway) {
        return new DeleteConfigurationRuleUseCase(gateway);
    }
}
