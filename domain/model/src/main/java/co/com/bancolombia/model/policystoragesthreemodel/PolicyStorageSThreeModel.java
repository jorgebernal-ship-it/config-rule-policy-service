package co.com.bancolombia.model.policystoragesthreemodel;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PolicyStorageSThreeModel {
	private String bucketName;
	private String objectKey;
	private String fileName;
	private String contentType;
	private Long sizeInBytes;
	private byte[] fileBytes;
}
