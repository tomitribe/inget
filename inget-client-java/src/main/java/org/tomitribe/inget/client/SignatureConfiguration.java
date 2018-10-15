package org.tomitribe.inget.client;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Generated;
import java.util.List;

@Builder
@Getter
@Setter
@Generated(value = "org.tomitribe.inget.model.ClientGenerator")
public class SignatureConfiguration {

    private boolean signatureDetails;

    private String keyId;

    private String keyLocation;

    private String algorithm;

    private String header;

    private String prefix;

    private List<String> signedHeaders;
}
