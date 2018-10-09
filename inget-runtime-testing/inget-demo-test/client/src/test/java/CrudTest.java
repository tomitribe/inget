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

import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.inget.movie.model.Movie;
import org.tomitribe.inget.movie.rest.MoviesResource;
import org.tomitribe.inget.movie.rest.client.MovieClient;
import org.tomitribe.inget.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.inget.movie.rest.client.base.SignatureAuthenticator;
import org.tomitribe.inget.movie.rest.client.base.SignatureConfiguration;
import org.tomitribe.inget.movie.services.MoviesService;

import javax.ws.rs.client.ClientRequestFilter;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class CrudTest {
    @Deployment
    public static WebArchive webApp() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(Movie.class)
                .addClass(MoviesService.class)
                .addClass(MoviesResource.class)
                .addClass(ClientConfiguration.class)
                .addClass(SignatureConfiguration.class)
                .addClass(SignatureAuthenticator.class)
                .addClass(ClientRequestFilter.class)
                .addClass(JohnzonProvider.class);
    }

    @Test
    public void testCreate(final @ArquillianResource URL base) throws Exception {
        Movie movie = new Movie("abc", "cde", "Action", 9, 1984);

        ClientConfiguration config = ClientConfiguration.builder()
                .url(base)
                .verbose(true)
                .build();

        MovieClient movieClient = new MovieClient(config);

        movie = movieClient.movies().addMovie(movie);
        Movie movieFound = movieClient.movies().find(movie.getId());

        assertNotNull(movieFound);
    }

    @Test
    public void testUpdate(final @ArquillianResource URL base) throws Exception {
        Movie movie = new Movie("abc", "cde", "Action", 9, 1984);

        ClientConfiguration config = ClientConfiguration.builder()
                .url(base)
                .verbose(true)
                .build();

        MovieClient movieClient = new MovieClient(config);

        movie = movieClient.movies().addMovie(movie);
        Movie movieFound = movieClient.movies().find(movie.getId());

        assertNotNull(movieFound);

        movieFound.setRating(10);
        movie = movieClient.movies().updateMovie(movieFound.getId(), movieFound);

        assertEquals(10, movie.getRating());
    }

    @Test
    public void testDelete(final @ArquillianResource URL base) throws Exception {
        Movie movie = new Movie("abc", "cde", "Action", 9, 1984);

        ClientConfiguration config = ClientConfiguration.builder()
                .url(base)
                .verbose(true)
                .build();

        MovieClient movieClient = new MovieClient(config);

        movie = movieClient.movies().addMovie(movie);
        movieClient.movies().deleteMovie(movie.getId());

        movie = movieClient.movies().find(movie.getId());
        assertNull(movie);
    }

}