/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,


 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.superbiz.video.model;

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
    private final MovieFilter filters;

    @Schema(description = "The total number of items for the search request. It may be higher than the number of items returned because of the pagination.", required = true)
    private final Long total;
}
