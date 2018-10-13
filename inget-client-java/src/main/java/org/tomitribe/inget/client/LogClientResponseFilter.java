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
        printVerboseLabel("REQUEST", config.isVerbose());
        printRequiredValue("Method", request.getMethod());
        printRequiredValue("Location", request.getUri());
        printVerboseValue("Accept", request.getMediaType(), config.isVerbose());
        skipLine();
        printVerboseLabel("RESPONSE", config.isVerbose());
        printRequiredValue("Date", response.getDate());
        printRequiredValue("Status", response.getStatusInfo().getStatusCode() + " (" + response.getStatusInfo().getReasonPhrase() + ")");
        printVerboseValue("Content Type", response.getMediaType(), config.isVerbose());
        skipLine();
    }

    private void printVerboseValue(String label, Object value, boolean isVerbose) {
        if (value != null && isVerbose) {
            System.out.println(label + ": " + value);
        }
    }

    private void printVerboseLabel(String label, boolean isVerbose) {
        if (label != null && isVerbose) {
            System.out.println(label);
        }
    }

    private void printRequiredValue(String label, Object value) {
        System.out.println(label + ": " + value);
    }

    private void skipLine() {
        System.out.println("");
    }
}
