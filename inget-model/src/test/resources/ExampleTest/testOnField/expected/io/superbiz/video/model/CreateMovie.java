package io.superbiz.video.model;

import javax.annotation.Generated;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Create", toBuilder = true)
@Generated("org.tomitribe.inget.model.ModelClassGenerator")
public class CreateMovie {

    private String director;

    private String genre;

    private int rating;
}
