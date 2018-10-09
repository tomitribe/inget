package org.tomitribe.trapease.movie.rest.client;

import javax.annotation.Generated;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.trapease.movie.rest.client.base.MovieClientExceptionMapper;

@Generated("org.tomitribe.client.ClientGenerator")
public class MovieClient {

    public MovieClient(
            ClientConfiguration config) {
        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(config.getUrl())
                .register(JohnzonProvider.class).register(MovieClientExceptionMapper.class);
    }
}
