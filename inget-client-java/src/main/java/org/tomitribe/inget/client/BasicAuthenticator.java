package org.tomitribe.inget.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Base64;

public class BasicAuthenticator implements ClientRequestFilter {

    private final ClientConfiguration config;

    private final BasicConfiguration basicConfig;

    public BasicAuthenticator(
            ClientConfiguration config) {
        this.config = config;
        this.basicConfig = config.getBasic();
    }

    @Override
    public void filter(
            final ClientRequestContext requestContext) throws IOException {
        String token = generateBasicAuth(basicConfig.getUsername(), basicConfig.getPassword());
        requestContext.getHeaders().add(basicConfig.getHeader(), token);

        if (config.isVerbose()) {
            System.out.println("AUTHENTICATION");
            System.out.println(HttpHeaders.AUTHORIZATION + ": " + token);
            System.out.println("");
        }
    }

    private String generateBasicAuth(
            String username,

            String password) {
        String value = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        return value;
    }
}
