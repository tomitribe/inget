package org.tomitribe.inget.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

public class LogClientResponseFilter implements ClientResponseFilter {

    private final ClientConfiguration config;

    public LogClientResponseFilter(ClientConfiguration config) {
        this.config = config;
    }

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
        if(config.isVerbose()){
            printLabel("REQUEST");
            printValue("Method", request.getMethod());
            printValue("Location", request.getUri());
            printValue("Accept", request.getMediaType());
            skipLine();
            printLabel("RESPONSE");
            printValue("Date", response.getDate());
            printValue("Status", response.getStatusInfo().getStatusCode() + " (" + response.getStatusInfo().getReasonPhrase() + ")");
            printValue("Content Type", response.getMediaType());
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
