/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.openejb.arquillian.common.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.inget.client.ClientConfiguration;
import org.tomitribe.inget.client.SignatureAuthenticator;
import org.tomitribe.inget.client.SignatureConfiguration;
import org.tomitribe.inget.movie.model.Movie;
import org.tomitribe.inget.movie.rest.MoviesResource;
import org.tomitribe.inget.movie.rest.client.MovieClient;
import org.tomitribe.inget.movie.services.MoviesService;

import javax.ws.rs.client.ClientRequestFilter;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ConfigFileTest extends Command{
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
        System.setProperty("user.home", Files.createTempDir().getAbsolutePath());

        cmd("movies add-movie --title \"The Terminator\" --director \"James Cameron\" --genre Action --year 1984 --rating 8", base.toString());

        File file = new File(System.getProperty("user.home") + File.separator + ".cmdline", ".cmdlineconfig");
        Properties prop = new Properties();
        prop.load(new FileInputStream(file));

        assertEquals(base.toString(), prop.getProperty("general.url"));

        // Call without URL
        cmd("movies add-movie --title \"Kelly Slater & The Young Guns\" --director \"Kelly Slater\" --genre Surf --year 2004 --rating 9");

        MovieClient movieClient = new MovieClient(ClientConfiguration.builder().url(base.toString()).verbose(true).build());
        List<Movie> movies = movieClient.movies().getMovies();
        Movie movie = movies.stream().filter(m -> m.getTitle().equalsIgnoreCase("Kelly Slater & The Young Guns")).findFirst().get();

        assertNotNull(movie);
        assertEquals("Surf", movie.getGenre());
        assertEquals("Kelly Slater", movie.getDirector());
        assertEquals(2004, movie.getYear());
        assertEquals(9, movie.getRating());
        file.deleteOnExit();
    }
}