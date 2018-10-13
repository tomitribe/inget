package org.tomitribe.inget.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class ClientConfiguration {

    private String url;

    private boolean verbose;

    private SignatureConfiguration signature;

    private BasicConfiguration basic;
}