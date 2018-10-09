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
package org.tomitribe.trapease.movie.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.annotation.Generated;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.tomitribe.trapease.movie.model.BulkMovieResult;
import org.tomitribe.trapease.movie.model.CreateMovie;
import org.tomitribe.trapease.movie.model.MovieResult;
import org.tomitribe.trapease.movie.model.UpdateMovie;

@Path("movie/bean")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Generated("org.tomitribe.resource.ResourcesGenerator")
@Tag(name = "Movies", description = "This endpoint manages multiple movies.")
public interface MovieResourceBean {

    // ----------------------------------------------------------------------------------------

    @POST
    @Operation(summary = "Bulk create movies.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = BulkMovieResult.class))),
            @ApiResponse(responseCode = "409", description = "Conflict") })
    @Generated("org.tomitribe.resource.MethodGenerator")
    Response bulkCreate(
            @Parameter(description = "Set of CreateMovie to create", required = true)
            final List<CreateMovie> movies);

    // ----------------------------------------------------------------------------------------

    @PUT
    @Operation(summary = "Bulk update movies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = BulkMovieResult.class))) })
    @Generated("org.tomitribe.resource.MethodGenerator")
    Response bulkUpdate(
            @Parameter(description = "Set of UpdateMovie to update", required = true)
            final List<UpdateMovie> movies);

    // ----------------------------------------------------------------------------------------

    @DELETE
    @Operation(summary = "Bulk delete movies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = BulkMovieResult.class))) })
    @Generated("org.tomitribe.resource.MethodGenerator")
    Response bulkDelete(
            @Parameter(description = "Set of Movie ids to delete", required = true)
            final List<String> ids);

    // ----------------------------------------------------------------------------------------

    @GET
    @Operation(summary = "Read all Movies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = MovieResult.class))) })
    @Generated("org.tomitribe.resource.MethodGenerator")
    Response readAll(
            @QueryParam("title")
            final String title);
}
