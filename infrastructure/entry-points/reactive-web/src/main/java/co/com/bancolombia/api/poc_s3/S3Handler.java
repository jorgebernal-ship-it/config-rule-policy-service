package co.com.bancolombia.api.poc_s3;

import co.com.bancolombia.model.policystoragesthreemodel.ContentFormat;
import co.com.bancolombia.model.policystoragesthreemodel.PolicyStorageSThreeModel;
import co.com.bancolombia.model.policystoragesthreemodel.RegoObjectAlreadyExistsException;
import co.com.bancolombia.model.policystoragesthreemodel.S3ObjectAmbiguousException;
import co.com.bancolombia.model.policystoragesthreemodel.S3ObjectNotFoundException;
import co.com.bancolombia.usecase.storageinstres.StorageInSThreeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class S3Handler {

    private final StorageInSThreeUseCase storageInSThreeUseCase;

    public Mono<ServerResponse> create(ServerRequest serverRequest) {
        return serverRequest.multipartData()
                .flatMap(parts -> {
                    FilePart filePart = (FilePart) parts.getFirst("file");
                    if (filePart == null) {
                        return Mono.error(new IllegalArgumentException("El campo 'file' es requerido"));
                    }
                    String customKey  = getFormField(parts, "objectKey");
                    String bucketName = getFormField(parts, "bucketName");
                    String rawName    = filePart.filename();

                    return DataBufferUtils.join(filePart.content())
                            .map(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return PolicyStorageSThreeModel.builder()
                                        .bucketName(bucketName)
                                        .objectKey(hasText(customKey) ? customKey : null)
                                        .fileName(rawName)
                                        .contentType(filePart.headers().getContentType() != null
                                                ? filePart.headers().getContentType().toString()
                                                : null)
                                        .sizeInBytes((long) bytes.length)
                                        .fileBytes(bytes)
                                        .build();
                            });
                })
                .flatMap(storageInSThreeUseCase::save)
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(this::handleKnownErrors);
    }

    public Mono<ServerResponse> list(ServerRequest serverRequest) {
        String folder = serverRequest.queryParam("objectKey").orElse(null);
        return storageInSThreeUseCase.listFileNames(folder)
                .collectList()
                .flatMap(fileNames -> ServerResponse.ok().bodyValue(Map.of("files", fileNames)))
                .onErrorResume(this::handleKnownErrors);
    }

    public Mono<ServerResponse> download(ServerRequest serverRequest) {
        String objectKey = serverRequest.queryParam("objectKey").orElse(null);
        String fileName = serverRequest.queryParam("fileName").orElse(null);
        ContentFormat contentFormat;
        try {
            contentFormat = ContentFormat.fromValue(serverRequest.queryParam("contentFormat").orElse(null));
        } catch (IllegalArgumentException e) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(Map.of("message", e.getMessage()));
        }

        return storageInSThreeUseCase.download(objectKey, fileName)
                .flatMap(file -> {
                    byte[] bytes = file.getFileBytes() != null ? file.getFileBytes() : new byte[0];
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("bucketName", file.getBucketName());
                    response.put("objectKey", file.getObjectKey());
                    response.put("fileName", file.getFileName());
                    response.put("contentType", resolveContentType(file.getContentType()));
                    response.put("sizeInBytes", file.getSizeInBytes());
                    if (contentFormat == ContentFormat.TEXT) {
                        response.put("contentEncoding", "text");
                        response.put("content", new String(bytes, StandardCharsets.UTF_8));
                    } else {
                        response.put("contentEncoding", "base64");
                        response.put("content", Base64.getEncoder().encodeToString(bytes));
                    }

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(this::handleKnownErrors);
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        return serverRequest.multipartData()
                .flatMap(parts -> {
                    FilePart filePart = (FilePart) parts.getFirst("file");
                    if (filePart == null) {
                        return Mono.error(new IllegalArgumentException("El campo 'file' es requerido"));
                    }
                    String objectKey = getFormField(parts, "objectKey");
                    if (!hasText(objectKey)) {
                        return Mono.error(new IllegalArgumentException("El campo 'objectKey' es requerido"));
                    }
                    String bucketName = getFormField(parts, "bucketName");
                    String rawName = filePart.filename();

                    return DataBufferUtils.join(filePart.content())
                            .map(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return PolicyStorageSThreeModel.builder()
                                        .bucketName(bucketName)
                                        .fileName(rawName)
                                        .contentType(filePart.headers().getContentType() != null
                                                ? filePart.headers().getContentType().toString()
                                                : null)
                                        .sizeInBytes((long) bytes.length)
                                        .fileBytes(bytes)
                                        .build();
                            })
                            .flatMap(model -> storageInSThreeUseCase.update(objectKey, model));
                })
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(this::handleKnownErrors);
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        String objectKey = serverRequest.queryParam("objectKey").orElse(null);
        String fileName = serverRequest.queryParam("fileName").orElse(null);

        return storageInSThreeUseCase.delete(objectKey, fileName)
                .flatMap(deleted -> deleted
                        ? ServerResponse.ok().bodyValue(Map.of("message", "Archivo eliminado"))
                        : ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue(Map.of("message", "No fue posible eliminar el archivo")))
                .onErrorResume(this::handleKnownErrors);
    }

    public Mono<ServerResponse> listenGETOtherUseCase(ServerRequest serverRequest) {
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        return ServerResponse.ok().bodyValue("");
    }

    private Mono<ServerResponse> handleKnownErrors(Throwable ex) {
        if (ex instanceof RegoObjectAlreadyExistsException || ex instanceof S3ObjectAmbiguousException) {
            return ServerResponse.status(HttpStatus.CONFLICT).bodyValue(Map.of("message", ex.getMessage()));
        }
        if (ex instanceof S3ObjectNotFoundException) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(Map.of("message", ex.getMessage()));
        }
        if (ex instanceof IllegalArgumentException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(Map.of("message", ex.getMessage()));
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(Map.of("message", "Error procesando solicitud en S3"));
    }

    private String resolveContentType(String contentType) {
        return hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private String getFormField(MultiValueMap<String, Part> parts, String fieldName) {
        Part part = parts.getFirst(fieldName);
        if (part instanceof FormFieldPart formField) {
            return formField.value();
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
