package co.com.bancolombia.s3.adapter;

import co.com.bancolombia.model.policystoragesthreemodel.PolicyStorageSThreeModel;
import co.com.bancolombia.model.policystoragesthreemodel.RegoObjectAlreadyExistsException;
import co.com.bancolombia.model.policystoragesthreemodel.S3ObjectNotFoundException;
import co.com.bancolombia.s3.config.model.S3ConnectionProperties;
import co.com.bancolombia.s3.operations.S3Operations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3AdapterTest {

    @Mock
    private S3Operations s3Operations;

    @Mock
    private S3ConnectionProperties s3ConnectionProperties;

    @InjectMocks
    private S3Adapter s3Adapter;

    @Test
    void saveShouldUploadFileInBucketRootPreservingReceivedExtension() {
        byte[] payload = "package finance\nallow=true".getBytes();
        PolicyStorageSThreeModel request = PolicyStorageSThreeModel.builder()
                .fileName("allow-finance.rego")
                .fileBytes(payload)
                .build();
        when(s3ConnectionProperties.bucketName()).thenReturn("policies-bucket");
        when(s3Operations.objectExists(eq("policies-bucket"), eq("allow-finance.rego")))
                .thenReturn(Mono.just(false));
        when(s3Operations.uploadObject(eq("policies-bucket"), eq("allow-finance.rego"), eq(payload)))
                .thenReturn(Mono.just(true));

        PolicyStorageSThreeModel result = s3Adapter.save(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getBucketName()).isEqualTo("policies-bucket");
        assertThat(result.getObjectKey()).isEqualTo("allow-finance.rego");
        assertThat(result.getFileName()).isEqualTo("allow-finance.rego");
        assertThat(result.getContentType()).isEqualTo("application/octet-stream");
        assertThat(result.getSizeInBytes()).isEqualTo((long) payload.length);
        verify(s3Operations).objectExists("policies-bucket", "allow-finance.rego");
        verify(s3Operations).uploadObject("policies-bucket", "allow-finance.rego", payload);
    }

    @Test
    void saveShouldUploadRegoFileInsideProvidedFolder() {
        byte[] payload = "package finance\nallow=true".getBytes();
        PolicyStorageSThreeModel request = PolicyStorageSThreeModel.builder()
                .objectKey("policies/finance")
                .fileName("allow-finance.yaml")
                .fileBytes(payload)
                .build();
        when(s3ConnectionProperties.bucketName()).thenReturn("policies-bucket");
        when(s3Operations.objectExists(eq("policies-bucket"), eq("policies/finance/allow-finance.yaml")))
                .thenReturn(Mono.just(false));
        when(s3Operations.uploadObject(eq("policies-bucket"), eq("policies/finance/allow-finance.yaml"), eq(payload)))
                .thenReturn(Mono.just(true));

        PolicyStorageSThreeModel result = s3Adapter.save(request).block();

        assertThat(result).isNotNull();
        assertThat(result.getObjectKey()).isEqualTo("policies/finance/allow-finance.yaml");
        assertThat(result.getFileName()).isEqualTo("allow-finance.yaml");
        verify(s3Operations).objectExists("policies-bucket", "policies/finance/allow-finance.yaml");
        verify(s3Operations).uploadObject("policies-bucket", "policies/finance/allow-finance.yaml", payload);
    }

    @Test
    void saveShouldFailWhenObjectAlreadyExists() {
        PolicyStorageSThreeModel request = PolicyStorageSThreeModel.builder()
                .objectKey("policies/finance")
                .fileName("allow-finance.json")
                .fileBytes("any".getBytes())
                .build();
        when(s3ConnectionProperties.bucketName()).thenReturn("policies-bucket");
        when(s3Operations.objectExists(eq("policies-bucket"), eq("policies/finance/allow-finance.json")))
                .thenReturn(Mono.just(true));

        assertThatThrownBy(() -> s3Adapter.save(request).block())
                .isInstanceOf(RegoObjectAlreadyExistsException.class)
                .hasMessageContaining("policies/finance/allow-finance.json");
    }

    @Test
    void downloadShouldFailWhenObjectKeyDoesNotExist() {
        when(s3ConnectionProperties.bucketName()).thenReturn("policies-bucket");
        when(s3Operations.objectExists("policies-bucket", "policies/finance/missing.json"))
                .thenReturn(Mono.just(false));

        assertThatThrownBy(() -> s3Adapter.download("policies/finance/missing.json", null).block())
                .isInstanceOf(S3ObjectNotFoundException.class)
                .hasMessageContaining("objectKey=policies/finance/missing.json");
    }
}


