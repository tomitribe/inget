import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Generated;
import java.net.URL;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Generated(value = "org.tomitribe.inget.model.ClientGenerator")
public class ClientConfiguration {

    private URL url;

    private boolean verbose;

    private SignatureConfiguration signature;

    private BasicConfiguration basic;
}
