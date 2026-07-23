package co.com.bancolombia.api.config;

import co.com.bancolombia.model.policymodel.gateways.PolicyModelRepository;
import co.com.bancolombia.usecase.pocvalidation.PocValidationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "co.com.bancolombia")
public class UseCaseConfig {

    @Bean
    public PocValidationUseCase pocValidationUseCase(PolicyModelRepository repository) {
        return new PocValidationUseCase(repository);
    }
}
