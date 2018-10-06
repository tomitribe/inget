import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Generated;
import java.util.List;

@Builder
@Getter
@Setter
@Generated(value = "org.tomitribe.model.ModelGenerator")
public class SignatureConfiguration {

    private String keyId;

    private String keyLocation;

    private String algorithm;

    private String header;

    private String prefix;

    private List<String> signedHeaders;
}
