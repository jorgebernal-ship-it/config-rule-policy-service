package co.com.bancolombia.model.policystoragesthreemodel;

public class S3ObjectAmbiguousException extends RuntimeException {

    public S3ObjectAmbiguousException(String fileName) {
        super("Existe mas de un archivo con el nombre: " + fileName + ". Use objectKey completo.");
    }
}

