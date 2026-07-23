package co.com.bancolombia.api;

import co.com.bancolombia.model.policymodel.PolicyModel;
import co.com.bancolombia.usecase.pocvalidation.PocValidationUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class RuleHandlerGithub {
    private final PocValidationUseCase useCase;

    public Mono<ServerResponse> saveRule(ServerRequest request) {
        return request.bodyToMono(PolicyModel.class)
                .flatMap(useCase::createOrUpdateRule)
                .flatMap(rule -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(rule));
    }

    public Mono<ServerResponse> getRegoFile(ServerRequest request) {
        String fileName = request.pathVariable("file");
        return useCase.getRegoFile(fileName)
                .flatMap(regoContent -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(regoContent))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
