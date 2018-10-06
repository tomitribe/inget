import java.io.IOException;
import java.util.Base64;
import javax.annotation.Generated;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

@Generated(value = "org.tomitribe.model.ModelGenerator")
public class BasicAuthenticator implements ClientRequestFilter {

    private final org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration config;

    private final org.tomitribe.trapease.movie.rest.client.base.BasicConfiguration basicConfig;

    public BasicAuthenticator(
            org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration config) {
        this.config = config;
        this.basicConfig = config.getBasic();
    }

    @Override
    public void filter(
            final ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION,
                generateBasicAuth(basicConfig.getUsername(), basicConfig.getPassword()));
    }

    private String generateBasicAuth(
            String username,
            String password) {
        String value = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));

        if(config.isVerbose()){
            System.out.println(value);
        }
        return value;
    }
}
