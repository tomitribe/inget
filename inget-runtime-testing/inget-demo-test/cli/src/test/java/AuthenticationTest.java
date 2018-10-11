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
import org.apache.openejb.arquillian.common.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.inget.movie.model.Movie;
import org.tomitribe.inget.movie.rest.MoviesResource;
import org.tomitribe.inget.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.inget.movie.rest.client.base.SignatureAuthenticator;
import org.tomitribe.inget.movie.rest.client.base.SignatureConfiguration;
import org.tomitribe.inget.movie.services.MoviesService;

import javax.ws.rs.client.ClientRequestFilter;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AuthenticationTest extends Command {
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
    public void testBasicAuthentication(final @ArquillianResource URL base) throws Exception {
        System.setProperty("user.home", Files.createTempDir().getAbsolutePath());
        cmd("--verbose --username john --password 12345678 movies add-movie --title \"The Terminator\" --director \"James Cameron\" --genre Action --year 1984 --rating 8", base.toString());
        File file = new File(System.getProperty("user.home") + File.separator + ".cmdline", ".cmdlineconfig");
        assertTrue(outLogs.toString().contains("Basic am9objoxMjM0NTY3OA=="));

        outLogs.reset();

        assertEquals("", outLogs.toString());
        // Command without basic config and url
        cmd("--verbose movies add-movie --title \"The Terminator2\" --director \"James Cameron\" --genre Action --year 1984 --rating 8");
        assertTrue(outLogs.toString().contains("Basic am9objoxMjM0NTY3OA=="));
        file.deleteOnExit();
    }
}