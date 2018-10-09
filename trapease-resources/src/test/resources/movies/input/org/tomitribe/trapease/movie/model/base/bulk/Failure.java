package org.tomitribe.trapease.movie.model.base.bulk;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@Schema(description = "Failure details")
@Generated("org.tomitribe.model.ModelClassGenerator")
public class Failure {

    @Schema(description = "name of the entity that failed to be updated.", required = true)
    private final String name;

    @Schema(description = "message for the failure.", required = true)
    private final String message;

    @Schema(description = "code for the failure.", required = true)
    private final String code;
}
