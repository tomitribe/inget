package io.superbiz.video.model;

import io.superbiz.video.model.base.filter.DefaultFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import javax.annotation.Generated;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode
@Generated(value = "org.tomitribe.model.ModelGenerator")
@Schema(description = "The list of movies available for a given search request with associated metadata.")
public class MovieResult {

    @Schema(description = "The list of items for the given page. The list may be a partial list when pagination is used (default)", required = true)
    private final Collection<Movie> items;

    @Schema(description = "Contains the elements that can be used for filtering: labels, by default.", required = true)
    private final DefaultFilter filters;

    @Schema(description = "The total number of items for the search request. It may be higher than the number of items returned because of the pagination.", required = true)
    private final Long total;
}
