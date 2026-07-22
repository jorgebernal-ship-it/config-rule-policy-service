package co.com.bancolombia.usecase.storageinstres;

import co.com.bancolombia.model.policystoragesthreemodel.PolicyStorageSThreeModel;
import co.com.bancolombia.model.policystoragesthreemodel.gateways.PolicyStorageSThreeModelRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class StorageInSThreeUseCase {

    private final PolicyStorageSThreeModelRepository policyStorageSThreeModelRepository;


    public Mono<PolicyStorageSThreeModel> save(PolicyStorageSThreeModel policyStorageSThreeModel) {
        return policyStorageSThreeModelRepository.save(policyStorageSThreeModel);
    }

    public Flux<String> listFileNames(String folder) {
        return policyStorageSThreeModelRepository.listFileNames(folder);
    }

    public Mono<PolicyStorageSThreeModel> download(String objectKey, String fileName) {
        return policyStorageSThreeModelRepository.download(objectKey, fileName);
    }

    public Mono<Boolean> delete(String objectKey, String fileName) {
        return policyStorageSThreeModelRepository.delete(objectKey, fileName);
    }

    public Mono<PolicyStorageSThreeModel> update(String objectKey, PolicyStorageSThreeModel policyStorageSThreeModel) {
        return policyStorageSThreeModelRepository.update(objectKey, policyStorageSThreeModel);
    }
}
