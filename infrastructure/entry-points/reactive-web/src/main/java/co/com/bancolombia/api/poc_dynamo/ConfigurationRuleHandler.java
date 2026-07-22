package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.ConfigurationRuleRequest;
import co.com.bancolombia.api.dto.ConfigurationRuleResponse;
import co.com.bancolombia.api.mapper.ConfigurationRuleMapper;
import co.com.bancolombia.usecase.configurationrule.ConfigurationRuleUseCase;
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
public class ConfigurationRuleHandler {

    private final ConfigurationRuleUseCase useCase;

    public Mono<ServerResponse> createConfigurationRule(ServerRequest request) {
        return request.bodyToMono(ConfigurationRuleRequest.class)
                .doOnNext(dto -> log.info("Creating configuration rule with type: {}", dto.type()))
                .map(ConfigurationRuleMapper::toDomain)
                .flatMap(useCase::create)
                .map(ConfigurationRuleMapper::toResponseDTO)
                .doOnSuccess(response -> log.info("Configuration rule created with id: {}", response.id()))
                .flatMap(response -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> getConfigurationRule(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Getting configuration rule with id: {}", id);

        return useCase.getById(id)
                .map(ConfigurationRuleMapper::toResponseDTO)
                .doOnSuccess(response -> log.info("Configuration rule retrieved with id: {}", response.id()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> getConfigurationRuleRaw(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Getting raw content for configuration rule with id: {}", id);

        return useCase.getRawContent(id)
                .doOnSuccess(content -> log.info("Raw content retrieved for rule id: {}, size: {} bytes", id, content.length()))
                .flatMap(content -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(content))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> updateConfigurationRule(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.bodyToMono(ConfigurationRuleRequest.class)
                .doOnNext(dto -> log.info("Updating configuration rule with id: {}", id))
                .map(ConfigurationRuleMapper::toDomain)
                .flatMap(rule -> useCase.update(id, rule))
                .map(ConfigurationRuleMapper::toResponseDTO)
                .doOnSuccess(response -> log.info("Configuration rule updated with id: {}, new version: {}",
                        response.id(), response.version()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::handleError);
    }

    public Mono<ServerResponse> deleteConfigurationRule(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Deleting configuration rule with id: {}", id);

        return useCase.delete(id)
                .doOnSuccess(v -> log.info("Configuration rule deleted with id: {}", id))
                .then(ServerResponse.noContent().build())
                .onErrorResume(this::handleError);
    }

    private Mono<ServerResponse> handleError(Throwable throwable) {
        log.error("Error processing request: {}", throwable.getMessage(), throwable);

        return switch (throwable) {
            case co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException ex ->
                    ServerResponse.status(404)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(404, "Not Found", ex.getMessage()));
            case co.com.bancolombia.model.policymodel.exceptions.RawContentNotAvailableException ex ->
                    ServerResponse.status(404)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(404, "Not Found", ex.getMessage()));
            case co.com.bancolombia.model.policymodel.exceptions.InvalidSpecException ex ->
                    ServerResponse.status(400)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(400, "Bad Request", ex.getMessage()));
            case IllegalArgumentException ex ->
                    ServerResponse.status(400)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(400, "Bad Request", ex.getMessage()));
            default ->
                    ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(buildErrorResponse(500, "Internal Server Error", "An unexpected error occurred"));
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
