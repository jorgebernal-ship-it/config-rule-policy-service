package co.com.bancolombia.s3.adapter;

import co.com.bancolombia.model.policystoragesthreemodel.PolicyStorageSThreeModel;
import co.com.bancolombia.model.policystoragesthreemodel.RegoObjectAlreadyExistsException;
import co.com.bancolombia.model.policystoragesthreemodel.S3ObjectAmbiguousException;
import co.com.bancolombia.model.policystoragesthreemodel.S3ObjectNotFoundException;
import co.com.bancolombia.model.policystoragesthreemodel.gateways.PolicyStorageSThreeModelRepository;
import co.com.bancolombia.s3.config.model.S3ConnectionProperties;
import co.com.bancolombia.s3.operations.S3Operations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class S3Adapter implements PolicyStorageSThreeModelRepository {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Operations s3Operations;
    private final S3ConnectionProperties s3ConnectionProperties;

    @Override
    public Mono<PolicyStorageSThreeModel> save(PolicyStorageSThreeModel model) {
        String bucketName = hasText(model.getBucketName()) ? model.getBucketName() : s3ConnectionProperties.bucketName();
        String objectKey  = normalizeObjectKey(model);
        byte[] fileBytes  = model.getFileBytes() != null ? model.getFileBytes() : new byte[0];
        long sizeInBytes  = fileBytes.length;

        return s3Operations.objectExists(bucketName, objectKey)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RegoObjectAlreadyExistsException(bucketName, objectKey));
                    }
                    return s3Operations.uploadObject(bucketName, objectKey, fileBytes)
                            .flatMap(saved -> saved
                                    ? Mono.just(model.toBuilder()
                                        .bucketName(bucketName)
                                        .objectKey(objectKey)
                                        .fileName(extractFileName(objectKey))
                                        .contentType(hasText(model.getContentType()) ? model.getContentType() : DEFAULT_CONTENT_TYPE)
                                        .sizeInBytes(sizeInBytes)
                                        .fileBytes(null)
                                        .build())
                                    : Mono.error(new IllegalStateException("No fue posible almacenar el archivo en S3")));
                });
    }

    @Override
    public Flux<String> listFileNames(String folder) {
        String bucketName = s3ConnectionProperties.bucketName();
        String normalizedFolder = normalizeFolder(folder);
        String prefix = hasText(normalizedFolder) ? normalizedFolder + "/" : null;

        return s3Operations.listBucketObjects(bucketName, prefix)
                .flatMapMany(Flux::fromIterable)
                .map(s3Object -> s3Object.key().replace('\\', '/'))
                .filter(key -> isInRequestedLevel(key, normalizedFolder))
                .map(this::extractFileName);
    }

    @Override
    public Mono<PolicyStorageSThreeModel> download(String objectKey, String fileName) {
        String bucketName = s3ConnectionProperties.bucketName();
        return resolveObjectKey(bucketName, objectKey, fileName)
                .flatMap(resolvedKey -> s3Operations.getObject(bucketName, resolvedKey)
                        .reduce(new ByteArrayOutputStream(), this::append)
                        .map(ByteArrayOutputStream::toByteArray)
                        .map(bytes -> PolicyStorageSThreeModel.builder()
                                .bucketName(bucketName)
                                .objectKey(resolvedKey)
                                .fileName(extractFileName(resolvedKey))
                                .contentType(resolveContentType(extractFileName(resolvedKey)))
                                .sizeInBytes((long) bytes.length)
                                .fileBytes(bytes)
                                .build()))
                .onErrorMap(S3Exception.class, ex -> ex.statusCode() == 404
                        ? new S3ObjectNotFoundException("objectKey=" + normalizeAbsoluteObjectKey(firstNonEmpty(objectKey, fileName)))
                        : ex);
    }

    @Override
    public Mono<Boolean> delete(String objectKey, String fileName) {
        String bucketName = s3ConnectionProperties.bucketName();
        return resolveObjectKey(bucketName, objectKey, fileName)
                .flatMap(resolvedKey -> s3Operations.deleteObject(bucketName, resolvedKey));
    }

    @Override
    public Mono<PolicyStorageSThreeModel> update(String objectKey, PolicyStorageSThreeModel model) {
        String bucketName = hasText(model.getBucketName()) ? model.getBucketName() : s3ConnectionProperties.bucketName();
        String resolvedKey = normalizeAbsoluteObjectKey(objectKey);
        byte[] fileBytes = model.getFileBytes() != null ? model.getFileBytes() : new byte[0];
        long sizeInBytes = fileBytes.length;

        return s3Operations.objectExists(bucketName, resolvedKey)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new S3ObjectNotFoundException("objectKey=" + resolvedKey));
                    }
                    return s3Operations.uploadObject(bucketName, resolvedKey, fileBytes)
                            .flatMap(saved -> saved
                                    ? Mono.just(model.toBuilder()
                                    .bucketName(bucketName)
                                    .objectKey(resolvedKey)
                                    .fileName(extractFileName(resolvedKey))
                                    .contentType(hasText(model.getContentType()) ? model.getContentType() : DEFAULT_CONTENT_TYPE)
                                    .sizeInBytes(sizeInBytes)
                                    .fileBytes(null)
                                    .build())
                                    : Mono.error(new IllegalStateException("No fue posible actualizar el archivo en S3")));
                });
    }

    private String normalizeObjectKey(PolicyStorageSThreeModel model) {
        String fileName = normalizeFileName(firstNonEmpty(
                model.getFileName(),
                "policy-" + UUID.randomUUID()
        ));
        String folder = normalizeFolder(model.getObjectKey());

        return hasText(folder) ? folder + "/" + fileName : fileName;
    }

    private String extractFileName(String objectKey) {
        int lastSlash = objectKey.lastIndexOf('/');
        return lastSlash >= 0 ? objectKey.substring(lastSlash + 1) : objectKey;
    }

    private String normalizeFileName(String rawFileName) {
        return extractFileName(rawFileName.replace('\\', '/')).trim();
    }

    private String normalizeFolder(String folder) {
        if (!hasText(folder)) {
            return "";
        }

        String normalizedFolder = folder.trim().replace('\\', '/');
        while (normalizedFolder.startsWith("/")) {
            normalizedFolder = normalizedFolder.substring(1);
        }
        while (normalizedFolder.endsWith("/")) {
            normalizedFolder = normalizedFolder.substring(0, normalizedFolder.length() - 1);
        }
        return normalizedFolder;
    }

    private Mono<String> resolveObjectKey(String bucketName, String objectKey, String fileName) {
        if (hasText(objectKey)) {
            String normalizedObjectKey = normalizeAbsoluteObjectKey(objectKey);
            return s3Operations.objectExists(bucketName, normalizedObjectKey)
                    .flatMap(exists -> exists
                            ? Mono.just(normalizedObjectKey)
                            : Mono.error(new S3ObjectNotFoundException("objectKey=" + normalizedObjectKey)));
        }
        if (!hasText(fileName)) {
            return Mono.error(new IllegalArgumentException("Debe enviar objectKey o fileName"));
        }

        String normalizedKey = normalizeAbsoluteObjectKey(fileName);
        return s3Operations.objectExists(bucketName, normalizedKey)
                .flatMap(exists -> exists
                        ? Mono.just(normalizedKey)
                        : Mono.error(new S3ObjectNotFoundException("fileName=" + normalizedKey)));
    }

    private boolean isInRequestedLevel(String fullKey, String folder) {
        if (!hasText(folder)) {
            return !fullKey.contains("/");
        }
        String prefix = folder + "/";
        if (!fullKey.startsWith(prefix)) {
            return false;
        }
        String remaining = fullKey.substring(prefix.length());
        return !remaining.isEmpty() && !remaining.contains("/");
    }

    private String normalizeAbsoluteObjectKey(String objectKey) {
        String normalized = objectKey.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private ByteArrayOutputStream append(ByteArrayOutputStream outputStream, ByteBuffer byteBuffer) {
        byte[] chunk = new byte[byteBuffer.remaining()];
        byteBuffer.get(chunk);
        outputStream.writeBytes(chunk);
        return outputStream;
    }

    private String resolveContentType(String fileName) {
        String detected = URLConnection.guessContentTypeFromName(fileName);
        return hasText(detected) ? detected : DEFAULT_CONTENT_TYPE;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (hasText(value)) return value;
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
