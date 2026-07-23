package co.com.bancolombia.githubrepository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitHubContentRequest {
    private final String message;
    private final String content; // Debe ir en Base64
    private final String sha;     // Obligatorio si el archivo ya existe
}
