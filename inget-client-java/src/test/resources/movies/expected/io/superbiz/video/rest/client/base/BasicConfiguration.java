package io.superbiz.video.rest.client.base;

import javax.annotation.Generated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Generated(value = "org.tomitribe.inget.model.ClientGenerator")
public class BasicConfiguration {

    private String username;

    private String password;

    private String header;

    private String prefix;
}
