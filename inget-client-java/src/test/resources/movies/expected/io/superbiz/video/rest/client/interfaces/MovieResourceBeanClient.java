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
package io.superbiz.video.rest.client.interfaces;

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
import movies.input.io.superbiz.video.model.CreateMovie;
import movies.input.io.superbiz.video.model.Movie;
import movies.input.io.superbiz.video.model.UpdateMovie;

@Path("movie/bean")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Generated("org.tomitribe.inget.client.ClientGenerator")
public interface MovieResourceBeanClient {

    @POST
    Movie create(
            final CreateMovie movie);

    @PUT
    @Path("{id}")
    Movie update(
            @PathParam("id")
            final String id,

            final UpdateMovie movie);

    @GET
    @Path("{id}")
    Movie read(
            @PathParam("id")
            final String id);

    @DELETE
    @Path("{id}")
    Response delete(
            @PathParam("id")
            final String id);
}
