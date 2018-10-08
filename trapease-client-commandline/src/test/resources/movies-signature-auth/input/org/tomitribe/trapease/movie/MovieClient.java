package org.tomitribe.trapease.movie.rest.client;

import javax.annotation.Generated;
import javax.ws.rs.*;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.trapease.movie.rest.client.base.MovieClientExceptionMapper;
import org.tomitribe.trapease.movie.rest.client.interfaces.MoviesResourceClient;

@Generated("org.tomitribe.client.ClientGenerator")
public class MovieClient {

    private MoviesResourceClient moviesResourceClient;

    public MovieClient(
            ClientConfiguration config) {
        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(config.getUrl())
                .register(JohnzonProvider.class).register(MovieClientExceptionMapper.class);
        if (config.getSignature() != null) {
            builder.register(new org.tomitribe.trapease.movie.rest.client.base.SignatureAuthenticator(config));
        }
        if (config.getBasic() != null) {
            builder.register(new org.tomitribe.trapease.movie.rest.client.base.BasicAuthenticator(config));
        }
        moviesResourceClient = builder.build(MoviesResourceClient.class);
    }

    public MoviesResourceClient movies() {
        return this.moviesResourceClient;
    }
}
