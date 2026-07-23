package co.com.bancolombia.githubrepository;

import lombok.Data;

@Data
public class GitHubContentResponse {
    private ContentInfo content;

    @Data
    public static class ContentInfo {
        private String sha;
    }
}
