package co.com.bancolombia.model.policystoragesthreemodel.gateways;

import co.com.bancolombia.model.policystoragesthreemodel.PolicyStorageSThreeModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyStorageSThreeModelRepository {

    Mono<PolicyStorageSThreeModel> save(PolicyStorageSThreeModel policyStorageSThreeModel);

    Flux<String> listFileNames(String folder);

    Mono<PolicyStorageSThreeModel> download(String objectKey, String fileName);

    Mono<Boolean> delete(String objectKey, String fileName);

    Mono<PolicyStorageSThreeModel> update(String objectKey, PolicyStorageSThreeModel policyStorageSThreeModel);
}
