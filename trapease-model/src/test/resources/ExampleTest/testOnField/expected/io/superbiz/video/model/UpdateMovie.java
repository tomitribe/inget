package io.superbiz.video.model;

import javax.annotation.Generated;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Update", toBuilder = true)
@Generated("org.tomitribe.model.ModelClassGenerator")
public class UpdateMovie {

    private String director;

    private String genre;

    private int rating;
}
