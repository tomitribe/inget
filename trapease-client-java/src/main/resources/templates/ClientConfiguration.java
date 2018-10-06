package org.tomitribe.trapease.movie.rest.client.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Generated;
import java.net.URL;

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
