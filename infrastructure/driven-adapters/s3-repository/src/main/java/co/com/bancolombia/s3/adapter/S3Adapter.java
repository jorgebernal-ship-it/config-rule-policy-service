package co.com.bancolombia.s3.adapter;

import org.springframework.stereotype.Repository;
import co.com.bancolombia.s3.operations.S3Operations;
    import lombok.RequiredArgsConstructor;

@Repository
    @RequiredArgsConstructor
public class S3Adapter /* implements SomeGatewayFromDomain*/ {

   private final S3Operations s3Operations;
}
