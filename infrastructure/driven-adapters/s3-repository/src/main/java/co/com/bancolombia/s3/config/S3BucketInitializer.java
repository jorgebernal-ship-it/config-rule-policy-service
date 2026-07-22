package co.com.bancolombia.s3.config;

import co.com.bancolombia.s3.config.model.S3ConnectionProperties;
import co.com.bancolombia.s3.operations.S3Operations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3BucketInitializer {

    private final S3Operations s3Operations;
    private final S3ConnectionProperties s3ConnectionProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBucket() {
        String bucketName = s3ConnectionProperties.bucketName();
        log.info("Verificando existencia del bucket: {}", bucketName);

        s3Operations.ensureBucketExists(bucketName)
                .doOnSuccess(success -> log.info("Bucket {} está disponible", bucketName))
                .doOnError(error -> log.error("Error al verificar/crear bucket {}: {}", bucketName, error.getMessage()))
                .subscribe();
    }
}

