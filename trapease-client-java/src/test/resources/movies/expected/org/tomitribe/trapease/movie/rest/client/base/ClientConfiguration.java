package org.tomitribe.trapease.movie.rest.client.base;

import java.net.URL;
import javax.annotation.Generated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Generated(value = "org.tomitribe.model.ModelGenerator")
public class ClientConfiguration {

    private URL url;

    private boolean verbose;

    private org.tomitribe.trapease.movie.rest.client.base.SignatureConfiguration signature;

    private org.tomitribe.trapease.movie.rest.client.base.BasicConfiguration basic;
}
