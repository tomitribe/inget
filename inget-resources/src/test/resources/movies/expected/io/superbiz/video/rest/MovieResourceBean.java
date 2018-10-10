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
package io.superbiz.video.rest;

import io.superbiz.video.model.CreateMovie;
import io.superbiz.video.model.Movie;
import io.superbiz.video.model.UpdateMovie;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Generated;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("movie/bean")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Generated("org.tomitribe.inget.resource.ResourcesGenerator")
@Tag(name = "Movie", description = "This endpoint manages a single movie.")
public interface MovieResourceBean {

    // ----------------------------------------------------------------------------------------

    @POST
    @Operation(summary = "Create a new Movie.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", headers = {
                    @Header(name = "Location", description = "The resource to the created Movie.") }, content = @Content(schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "409", description = "Conflict") })
    @Generated("org.tomitribe.inget.resource.MethodGenerator")
    Response create(
            @RequestBody(description = "The new Movie", required = true)
            final CreateMovie movie);

    // ----------------------------------------------------------------------------------------

    @PUT
    @Path("{id}")
    @Operation(summary = "Update Movie by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "404", description = "Not Found") })
    @Generated("org.tomitribe.inget.resource.MethodGenerator")
    Response update(
            @Parameter(description = "The Movie id", required = true)
            @PathParam("id")
            final String id,

            @RequestBody(description = "The updated data for the existing Movie", required = true)
            final UpdateMovie movie);

    // ----------------------------------------------------------------------------------------

    @GET
    @Path("{id}")
    @Operation(summary = "Read Movie by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "404", description = "Not Found") })
    @Generated("org.tomitribe.inget.resource.MethodGenerator")
    Response read(
            @Parameter(description = "The Movie id", required = true)
            @PathParam("id")
            final String id);

    // ----------------------------------------------------------------------------------------

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not Found") })
    @Generated("org.tomitribe.inget.resource.MethodGenerator")
    Response delete(
            @Parameter(description = "The Movie id", required = true)
            @PathParam("id")
            final String id);
}
