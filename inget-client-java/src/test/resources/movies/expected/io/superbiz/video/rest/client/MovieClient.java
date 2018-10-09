package io.superbiz.video.rest.client;

import io.superbiz.video.rest.client.base.ClientConfiguration;
import io.superbiz.video.rest.client.base.MovieClientExceptionMapper;
import javax.annotation.Generated;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@Generated("org.tomitribe.inget.client.ClientGenerator")
public class MovieClient {

    public MovieClient(
            ClientConfiguration config) {
        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUrl(config.getUrl())
                .register(JohnzonProvider.class).register(MovieClientExceptionMapper.class);
    }
}
