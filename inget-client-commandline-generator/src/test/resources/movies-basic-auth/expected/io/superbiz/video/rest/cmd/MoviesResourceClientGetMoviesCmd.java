package io.superbiz.video.rest.cmd;

import io.airlift.airline.Command;
import io.superbiz.video.rest.client.MovieClient;
import io.superbiz.video.rest.cmd.base.DefaultCommand;
import org.tomitribe.inget.client.ClientConfiguration;

@Command(name = "get-movies")
public class MoviesResourceClientGetMoviesCmd extends DefaultCommand {

    @Override
    public void run(
            final ClientConfiguration clientConfiguration) {
        System.out.println(new org.apache.johnzon.mapper.MapperBuilder().setPretty(true).build()
                .writeObjectAsString(new MovieClient(clientConfiguration).moviesresourceclient().getMovies()));
    }
}
