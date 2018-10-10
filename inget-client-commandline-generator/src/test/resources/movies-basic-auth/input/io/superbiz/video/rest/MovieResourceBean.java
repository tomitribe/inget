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

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.status;

@ApplicationScoped
public class MovieResourceBean implements MovieResource {
    private AtomicInteger idGenerator = new AtomicInteger(0);
    private Map<String, Movie> movies = new ConcurrentHashMap<>();

    @Context
    private UriInfo uriInfo;

    @Override
    public Response create(final CreateMovie movie) {
        final String movieId = idGenerator.incrementAndGet() + "";
        final Movie newMovie =
                Movie.builder()
                     .id(movieId)
                     .title(movie.getTitle())
                     .director(movie.getDirector())
                     .genre(movie.getGenre())
                     .year(movie.getYear())
                     .rating(movie.getRating())
                     .build();

        movies.put(movieId, newMovie);

        final URI createdURI = uriInfo.getBaseUriBuilder()
                                      .path("movie/{id}")
                                      .resolveTemplate("id", newMovie.getId())
                                      .build();

        return Response.created(createdURI).build();
    }

    @Override
    public Response update(final String id, final UpdateMovie movie) {
        final Optional<Movie> updatedMovie =
                Optional.ofNullable(movies.get(id))
                        .map(updateMovie -> updateMovie.toBuilder()
                                                       .title(movie.getTitle())
                                                       .director(movie.getDirector())
                                                       .genre(movie.getGenre())
                                                       .year(movie.getYear())
                                                       .rating(movie.getRating())
                                                       .build())
                        .map(a -> movies.replace(id, a))
                        .map(a -> movies.get(id));

        return updatedMovie.map(Response::ok).orElse(status(NOT_FOUND)).build();
    }

    @Override
    public Response read(final String id) {
        return Optional.ofNullable(movies.get(id))
                       .map(Response::ok)
                       .orElse(status(NOT_FOUND))
                       .build();
    }

    @Override
    public Response delete(final String id) {
        return Optional.ofNullable(movies.get(id))
                       .map(movie -> movies.remove(id))
                       .map(movie -> status(NO_CONTENT))
                       .orElse(status(NOT_FOUND))
                       .build();
    }
}
