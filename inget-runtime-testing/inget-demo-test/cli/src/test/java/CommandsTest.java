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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CommandsTest extends Command{
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
        cmd("movies add-movie --title \"The Terminator\" --director \"James Cameron\" --genre Action --year 1984 --rating 8", base);

        MovieClient movieClient = new MovieClient(ClientConfiguration.builder().url(base).verbose(true).build());
        List<Movie> movies = movieClient.movies().getMovies();
        Movie movie = movies.stream().filter(m -> m.getTitle().equalsIgnoreCase("The Terminator")).findFirst().get();

        assertNotNull(movie);
        assertEquals("Action", movie.getGenre());
        assertEquals("James Cameron", movie.getDirector());
        assertEquals(1984, movie.getYear());
        assertEquals(8, movie.getRating());
    }

    @Test
    public void testUpdate(final @ArquillianResource URL base) throws Exception {
        cmd("movies add-movie --title \"The Terminator\" --director \"James Cameron\" --genre Action --year 1984 --rating 8", base);
        assertTrue(outLogs.toString().contains("\"director\":\"James Cameron\""));
        resetLogs();

        MovieClient movieClient = new MovieClient(ClientConfiguration.builder().url(base).verbose(true).build());
        List<Movie> movies = movieClient.movies().getMovies();
        Movie movie = movies.stream().filter(m -> m.getTitle().equalsIgnoreCase("The Terminator")).findFirst().get();

        assertNotNull(movie);
        assertEquals("Action", movie.getGenre());
        assertEquals("James Cameron", movie.getDirector());
        assertEquals(1984, movie.getYear());
        assertEquals(8, movie.getRating());

        cmd("movies update-movie "+ movie.getId() + " --title \"The Terminator 2\" --director \"James Cameron\" --genre Action --year 1990 --rating 8", base);
        assertTrue(outLogs.toString().contains("\"title\":\"The Terminator 2\""));

        movies = movieClient.movies().getMovies();
        movie = movies.stream().filter(m -> m.getTitle().equalsIgnoreCase("The Terminator 2")).findFirst().get();

        assertNotNull(movie);
        assertEquals(1990, movie.getYear());
    }
}