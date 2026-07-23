package co.com.bancolombia.githubrepository;

import co.com.bancolombia.model.policymodel.PolicyModel;
import co.com.bancolombia.model.policymodel.configurationrule.RuleType;
import co.com.bancolombia.model.policymodel.gateways.PolicyModelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Primary
public class ConfigServerRepositoryAdapter implements PolicyModelRepository {

    private final WebClient configWebClient;
    private final GitHubRepositoryAdapter gitHubAdapter;

    public ConfigServerRepositoryAdapter(WebClient.Builder builder,
                                         @Value("${config.server.url}") String configServerUrl,
                                         GitHubRepositoryAdapter gitHubAdapter) {
        this.configWebClient = builder.baseUrl(configServerUrl).build();
        this.gitHubAdapter = gitHubAdapter;
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
    public Mono<PolicyModel> saveInGit(PolicyModel rule) {
        return gitHubAdapter.saveInGit(rule);
    }

    @Override
    public Mono<String> readFromConfigServer(String fileName) {
        // Formato estándar: /{application}/{profile}/{label}/{path}
        return configWebClient.get()
                .uri("/pdp-service/default/main/{fileName}", fileName)
                .retrieve()
                .bodyToMono(String.class);
    }
}
