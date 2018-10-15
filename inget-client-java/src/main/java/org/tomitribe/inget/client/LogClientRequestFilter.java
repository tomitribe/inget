package org.tomitribe.inget.client;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

@Priority(Priorities.USER)
public class LogClientRequestFilter implements ClientRequestFilter {

    private final ClientConfiguration config;

    public LogClientRequestFilter(ClientConfiguration config) {
        this.config = config;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        if(config.isVerbose()){
            String queryString = request.getUri().getQuery() == null ? "" : "?" + request.getUri().getQuery();
            System.out.println("> " + request.getMethod() + " " + request.getUri().getPath() + queryString + " HTTP 1.1");
            StringBuilder headers = new StringBuilder();
            request.getHeaders().forEach((k, v) -> {
                headers.append("> " + k + ": " + v.stream().findFirst().get() + "\n");
            });
            System.out.print(headers);
            System.out.print(">\n");
        }
    }
}
