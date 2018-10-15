package org.tomitribe.inget.client;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

@Priority(Priorities.USER)
public class LogClientResponseFilter implements ClientResponseFilter {

    private final ClientConfiguration config;

    public LogClientResponseFilter(ClientConfiguration config) {
        this.config = config;
    }

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
        if (config.isVerbose()) {
            System.out.println("< HTTP 1.1 " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
            StringBuilder headers = new StringBuilder();
            response.getHeaders().forEach((k, v) -> {
                headers.append("< " + k + ": " + v.stream().findFirst().get() + "\n");
            });
            System.out.println(headers);
        }

    }
}
