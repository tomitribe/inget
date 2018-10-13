import org.apache.johnzon.jaxrs.JohnzonProvider;
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
import org.tomitribe.inget.movie.services.MoviesService;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class LogClientResponseFilterTest extends Command {
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
                .addClass(ClientResponseFilter.class)
                .addClass(JohnzonProvider.class);
    }

    @Test
    public void testResponseVerboseLogs(final @ArquillianResource URL base) throws Exception {
        cmd("-v movies add-movie --title \"The Terminator\" --director \"James Cameron\" --genre Action --year 1984 --rating 8", base.toString());
        assertNotNull(outLogs.toString());
        assertTrue(outLogs.toString().contains("REQUEST"));
        assertTrue(outLogs.toString().contains("Method: POST"));
        assertTrue(outLogs.toString().contains("Location:"));
        assertTrue(outLogs.toString().contains("Accept: application/json"));

        assertTrue(outLogs.toString().contains("RESPONSE"));
        assertTrue(outLogs.toString().contains("Date"));
        assertTrue(outLogs.toString().contains("Status: 200 (OK)"));
        assertTrue(outLogs.toString().contains("Content Type: application/json"));
    }
}