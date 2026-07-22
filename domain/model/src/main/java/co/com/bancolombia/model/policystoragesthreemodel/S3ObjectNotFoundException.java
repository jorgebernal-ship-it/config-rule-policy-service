package co.com.bancolombia.model.policystoragesthreemodel;

public class S3ObjectNotFoundException extends RuntimeException {

    public S3ObjectNotFoundException(String criteria) {
        super("No se encontro archivo en S3 para: " + criteria);
    }
}

