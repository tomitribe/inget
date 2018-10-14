package org.tomitribe.inget.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class LogClientRequestFilter implements ClientRequestFilter {

    private final ClientConfiguration config;

    public LogClientRequestFilter(ClientConfiguration config) {
        this.config = config;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        if(config.isVerbose()){
            printLabel("REQUEST");
            printValue("Method", request.getMethod());
            printValue("Location", request.getUri());
            printValue("Accept", request.getMediaType());
            StringBuilder headers = new StringBuilder();
            request.getHeaders().forEach((k, v) -> {
                headers.append(k + ": " + v + "\n         ");
            });
            printValue("Headers", headers);
            skipLine();
        }
    }

    private void printValue(String label, Object value) {
        if (value != null) {
            System.out.println(label + ": " + value);
        }
    }

    private void printLabel(String label) {
        if (label != null) {
            System.out.println(label);
        }
    }

    private void skipLine() {
        System.out.println("");
    }
}
