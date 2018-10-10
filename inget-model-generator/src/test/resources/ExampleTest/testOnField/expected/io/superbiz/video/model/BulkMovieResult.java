package io.superbiz.video.model;

import io.superbiz.video.model.base.bulk.Failure;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.annotation.Generated;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
@Generated("org.tomitribe.inget.model.ModelClassGenerator")
@Schema(description = "The result of the bulk operation.")
public class BulkMovieResult {

    @Schema(description = "The movies that failed in the bulk operation.")
    private List<Failure> movies;
}
