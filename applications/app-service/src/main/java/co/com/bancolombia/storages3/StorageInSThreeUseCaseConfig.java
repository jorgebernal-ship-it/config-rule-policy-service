package co.com.bancolombia.storages3;

import co.com.bancolombia.model.policystoragesthreemodel.gateways.PolicyStorageSThreeModelRepository;
import co.com.bancolombia.usecase.storageinstres.StorageInSThreeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageInSThreeUseCaseConfig {

    @Bean
    public StorageInSThreeUseCase storageInSThreeUseCase(PolicyStorageSThreeModelRepository policyStorageSThreeModelRepository) {
        return new StorageInSThreeUseCase(policyStorageSThreeModelRepository);
    }
}
