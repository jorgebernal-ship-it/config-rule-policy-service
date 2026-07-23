package co.com.bancolombia.usecase.pocvalidation;

import co.com.bancolombia.model.policymodel.PolicyModel;
import co.com.bancolombia.model.policymodel.gateways.PolicyModelRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class PocValidationUseCase {

    private final PolicyModelRepository repository;

    public Mono<PolicyModel> createOrUpdateRule(PolicyModel rule) {
        // 1. Guarda en GitHub vía WebClient reactivo
        return repository.saveInGit(rule);
        // envío de eventos a tu RabbitMQ (ej: .flatMap(r -> sender.send(r)))
    }

    public Mono<String> getRegoFile(String fileName) {
        // 2. Lee desde Spring Cloud Config Server en texto plano
        return repository.readFromConfigServer(fileName);
    }
}
