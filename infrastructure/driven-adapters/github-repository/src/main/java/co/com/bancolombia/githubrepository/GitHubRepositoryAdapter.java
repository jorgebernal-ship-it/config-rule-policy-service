package co.com.bancolombia.githubrepository;

import co.com.bancolombia.model.policymodel.PolicyModel;
import co.com.bancolombia.model.policymodel.configurationrule.RuleType;
import co.com.bancolombia.model.policymodel.gateways.PolicyModelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
public class GitHubRepositoryAdapter implements PolicyModelRepository {

    private final WebClient webClient;

    @Value("${github.owner}") private String owner;
    @Value("${github.repo}") private String repo;

    public GitHubRepositoryAdapter(WebClient.Builder webClientBuilder,
                                   @Value("${github.token}") String token) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    @Override
    public Mono<PolicyModel> saveInGit(PolicyModel rule) {
        String path = rule.getSpec().getTargetPath();
        String encodedContent = Base64.getEncoder()
                .encodeToString(rule.getSpec().getRegoContent().getBytes());

        GitHubContentRequest requestPayload = GitHubContentRequest.builder()
                .message("Subiendo regla de OPA: " + rule.getName())
                .content(encodedContent)
                .sha(rule.getSpec().getSha())
                .build();

        return webClient.put()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, path)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(GitHubContentResponse.class)
                .map(response -> {
                    return rule.toBuilder()
                            .spec(rule.getSpec().toBuilder().sha(response.getContent().getSha()).build())
                            .build();
                });
    }

    @Override
    public Mono<PolicyModel> save(PolicyModel rule) {
        return null;
    }

    @Override
    public Mono<PolicyModel> findById(String id) {
        return null;
    }

    @Override
    public Flux<PolicyModel> findByType(RuleType type) {
        return null;
    }

    @Override
    public Flux<PolicyModel> findByScope(String scope) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return null;
    }

    @Override
    public Mono<String> readFromConfigServer(String path) {
        return Mono.empty();
    }
}
