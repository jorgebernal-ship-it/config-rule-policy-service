package co.com.bancolombia.api.poc_postgres;

import co.com.bancolombia.api.poc_postgres.dto.PolicySetRequest;
import co.com.bancolombia.api.poc_postgres.dto.PolicySetResponse;
import co.com.bancolombia.api.poc_postgres.mapper.PolicySetDTOMapper;
import co.com.bancolombia.usecase.policyset.PolicySetUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicySetHandler {

    private final PolicySetUseCase policySetUseCase;
    private final PolicySetDTOMapper mapper;

    public Mono<ServerResponse> createPolicySet(ServerRequest request) {
        return request.bodyToMono(PolicySetRequest.class)
                .doOnNext(dto -> log.info("Creating PolicySet with name: {}", dto.name()))
                .map(mapper::toDomain)
                .flatMap(policySetUseCase::create)
                .map(mapper::toResponse)
                .doOnSuccess(response -> log.info("PolicySet created with id: {}", response.id()))
                .flatMap(response -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> getPolicySetById(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Getting PolicySet with id: {}", id);

        return policySetUseCase.findById(id)
                .map(mapper::toResponse)
                .doOnSuccess(response -> log.info("PolicySet retrieved with id: {}", response.id()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> getPolicySetByChannelAndTxCode(ServerRequest request) {
        String channel = request.queryParam("channel")
                .orElseThrow(() -> new IllegalArgumentException("channel query parameter is required"));
        String transactionCode = request.queryParam("transactionCode")
                .orElseThrow(() -> new IllegalArgumentException("transactionCode query parameter is required"));

        log.info("Getting PolicySet with channel: {} and transactionCode: {}", channel, transactionCode);

        return policySetUseCase.findByChannelAndTransactionCode(channel, transactionCode)
                .map(mapper::toResponse)
                .doOnSuccess(response -> log.info("PolicySet retrieved with channel: {} and txCode: {}",
                        channel, transactionCode))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> updatePolicySet(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.bodyToMono(PolicySetRequest.class)
                .doOnNext(dto -> log.info("Updating PolicySet with id: {}", id))
                .map(mapper::toDomain)
                .flatMap(policySet -> policySetUseCase.update(id, policySet))
                .map(mapper::toResponse)
                .doOnSuccess(response -> log.info("PolicySet updated with id: {}, version: {}",
                        response.id(), response.version()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> deletePolicySet(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Deleting PolicySet with id: {}", id);

        return policySetUseCase.delete(id)
                .doOnSuccess(v -> log.info("PolicySet deleted with id: {}", id))
                .then(ServerResponse.noContent().build())
                .onErrorResume(this::handleError);
    }

    private Mono<ServerResponse> handleError(Throwable throwable) {
        log.error("Error processing PolicySet request: {}", throwable.getMessage(), throwable);

        return switch (throwable) {
            case IllegalArgumentException ex ->
                    ServerResponse.status(400)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(400, "Bad Request", ex.getMessage()));
            default ->
                    ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(500, "Internal Server Error",
                                    "An unexpected error occurred: " + throwable.getMessage()));
        };
    }

    private java.util.Map<String, Object> buildErrorResponse(int status, String error, String message) {
        return java.util.Map.of(
                "timestamp", java.time.Instant.now().toString(),
                "status", status,
                "error", error,
                "message", message
        );
    }
}
