package io.superbiz.video.rest.client;

import io.superbiz.video.rest.client.base.ClientConfiguration;
import io.superbiz.video.rest.client.base.MovieClientExceptionMapper;
import io.superbiz.video.rest.client.interfaces.MovieResourceBeanClient;
import javax.annotation.Generated;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@Generated("org.tomitribe.inget.client.ClientGenerator")
public class MovieClient {

    private MovieResourceBeanClient movieResourceBeanClient;

    public MovieClient(
            ClientConfiguration config) {
        RestClientBuilder builder = null;
        try {
            builder = RestClientBuilder.newBuilder().baseUrl(new java.net.URL(config.getUrl()))
                    .register(JohnzonProvider.class).register(MovieClientExceptionMapper.class);
        } catch (java.net.MalformedURLException e) {
            throw new javax.ws.rs.WebApplicationException("URL is not valid " + e.getMessage());
        }
        if (config.getSignature() != null) {
            builder.register(new io.superbiz.video.rest.client.base.SignatureAuthenticator(config));
        }
        if (config.getBasic() != null) {
            builder.register(new io.superbiz.video.rest.client.base.BasicAuthenticator(config));
        }
        movieResourceBeanClient = builder.build(MovieResourceBeanClient.class);
    }

    public MovieResourceBeanClient movie() {
        return this.movieResourceBeanClient;
    }
}
