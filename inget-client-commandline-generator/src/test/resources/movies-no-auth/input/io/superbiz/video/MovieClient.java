package io.superbiz.video.rest.client;

import javax.annotation.Generated;
import javax.ws.rs.*;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import io.superbiz.video.rest.client.base.ClientConfiguration;
import io.superbiz.video.rest.client.base.MovieClientExceptionMapper;
import io.superbiz.video.rest.client.interfaces.MoviesResourceClient;

@Generated("org.tomitribe.client.ClientGenerator")
public class MovieClient {

    private MoviesResourceClient moviesResourceClient;

    public MovieClient(
            ClientConfiguration config) {
        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(config.getUrl())
                .register(JohnzonProvider.class).register(MovieClientExceptionMapper.class);
        if (config.getSignature() != null) {
            builder.register(new io.superbiz.video.rest.client.base.SignatureAuthenticator(config));
        }
        if (config.getBasic() != null) {
            builder.register(new io.superbiz.video.rest.client.base.BasicAuthenticator(config));
        }
        moviesResourceClient = builder.build(MoviesResourceClient.class);
    }

    public MoviesResourceClient movies() {
        return this.moviesResourceClient;
    }
}
