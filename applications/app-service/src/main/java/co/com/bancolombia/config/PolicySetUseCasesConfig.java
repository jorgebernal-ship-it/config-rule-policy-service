package co.com.bancolombia.config;

import co.com.bancolombia.model.policysetmodel.gateways.PolicySetRepository;
import co.com.bancolombia.usecase.policyset.PolicySetUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicySetUseCasesConfig {

    @Bean
    public PolicySetUseCase policySetUseCase(PolicySetRepository repository) {
        return new PolicySetUseCase(repository);
    }
}
