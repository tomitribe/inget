package org.tomitribe.trapease.movie.rest.client.base;

import java.util.List;
import javax.annotation.Generated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Generated(value = "org.tomitribe.model.ModelGenerator")
public class SignatureConfiguration {

    private String keyId;

    private String keyLocation;

    private String algorithm;

    private String header;

    private String prefix;

    private List<String> signedHeaders;
}
