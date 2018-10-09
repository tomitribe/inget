package org.tomitribe.trapease.movie.rest.client.base;

import javax.annotation.Generated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Generated(value = "org.tomitribe.model.ModelGenerator")
public class BasicConfiguration {

    private String username;

    private String password;

    private String header;

    private String prefix;
}
