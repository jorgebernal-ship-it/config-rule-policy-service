package co.com.bancolombia.model.policystoragesthreemodel;

public class RegoObjectAlreadyExistsException extends RuntimeException {

    public RegoObjectAlreadyExistsException(String bucketName, String objectKey) {
        super("El archivo ya existe en S3. bucket=" + bucketName + ", objectKey=" + objectKey);
    }
}

