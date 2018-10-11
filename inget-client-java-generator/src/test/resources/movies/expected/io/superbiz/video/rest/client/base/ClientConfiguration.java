package io.superbiz.video.rest.client.base;

import java.net.URL;
import javax.annotation.Generated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Generated(value = "org.tomitribe.inget.model.ClientGenerator")
public class ClientConfiguration {

    private String url;

    private boolean verbose;

    private SignatureConfiguration signature;

    private BasicConfiguration basic;
}
