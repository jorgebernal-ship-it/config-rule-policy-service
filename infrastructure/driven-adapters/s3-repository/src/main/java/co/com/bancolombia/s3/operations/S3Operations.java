package co.com.bancolombia.s3.operations;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3Operations {

    private final S3AsyncClient s3AsyncClient;


    public Mono<Boolean> uploadObject(String bucketName,String objectKey, byte[] fileContent) {
        return Mono.fromFuture(
                s3AsyncClient.putObject(configurePutObject(bucketName,objectKey),
                        AsyncRequestBody.fromBytes(fileContent)))
                .map(response -> response.sdkHttpResponse().isSuccessful());
    }

    public Mono<Boolean> uploadObject(String bucketName,String objectKey, String fileContent) {
        return Mono.fromFuture(
                s3AsyncClient.putObject(configurePutObject(bucketName,objectKey),
                        AsyncRequestBody.fromString(fileContent)))
                .map(response -> response.sdkHttpResponse().isSuccessful());
    }

    public Mono<Boolean> uploadObject(String bucketName,String objectKey, File fileContent) {
        return Mono.fromFuture(
                s3AsyncClient.putObject(configurePutObject(bucketName,objectKey),
                        AsyncRequestBody.fromFile(fileContent)))
                .map(response -> response.sdkHttpResponse().isSuccessful());
    }

    public Mono<List<S3Object>> listBucketObjects(String bucketName){
        return Mono.fromFuture(s3AsyncClient.listObjects(ListObjectsRequest
                .builder()
                .bucket(bucketName)
                .build()))
                .map(ListObjectsResponse::contents);
    }

    public Mono<List<S3Object>> listBucketObjects(String bucketName, String prefix){
        ListObjectsRequest.Builder builder = ListObjectsRequest.builder().bucket(bucketName);
        if (prefix != null && !prefix.trim().isEmpty()) {
            builder.prefix(prefix);
        }
        return Mono.fromFuture(s3AsyncClient.listObjects(builder.build()))
                .map(ListObjectsResponse::contents);
    }

    public Flux<ByteBuffer> getObject(String bucketName,String objectKey) {
        return Mono.fromFuture(s3AsyncClient.getObject(GetObjectRequest.builder()
                .key(objectKey)
                .bucket(bucketName)
                .build(), AsyncResponseTransformer.toPublisher()))
                .flatMapMany(Flux::from);
    }

    public Mono<Boolean> deleteObject(String bucketName,String objectKey) {
        return Mono.fromFuture(s3AsyncClient.deleteObject(DeleteObjectRequest.builder()
                .key(objectKey)
                .bucket(bucketName).build()))
                .map(response -> response.sdkHttpResponse().isSuccessful());
    }

    public Mono<Boolean> objectExists(String bucketName, String objectKey) {
        return Mono.fromFuture(s3AsyncClient.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()))
                .map(response -> true)
                .onErrorResume(NoSuchKeyException.class, e -> Mono.just(false))
                .onErrorResume(S3Exception.class, e -> e.statusCode() == 404 ? Mono.just(false) : Mono.error(e));
    }

    public Mono<Boolean> bucketExists(String bucketName) {
        return Mono.fromFuture(s3AsyncClient.headBucket(HeadBucketRequest.builder()
                .bucket(bucketName)
                .build()))
                .map(response -> true)
                .onErrorResume(NoSuchBucketException.class, e -> Mono.just(false))
                .onErrorResume(e -> Mono.just(false));
    }

    public Mono<Boolean> createBucket(String bucketName) {
        return Mono.fromFuture(s3AsyncClient.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build()))
                .map(response -> response.sdkHttpResponse().isSuccessful())
                .onErrorResume(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("BucketAlreadyExists")) {
                        return Mono.just(true);
                    }
                    return Mono.error(e);
                });
    }

    public Mono<Boolean> ensureBucketExists(String bucketName) {
        return bucketExists(bucketName)
                .flatMap(exists -> exists ? Mono.just(true) : createBucket(bucketName));
    }

    private PutObjectRequest configurePutObject(String bucketName,String objectKey) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

}
