package org.tomitribe.trapease.movie.rest.cmd;

import io.airlift.airline.Command;
import io.superbiz.video.rest.cmd.base.DefaultCommand;
import org.tomitribe.trapease.movie.rest.client.MovieClient;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;

@Command(name = "get-movies")
public class MoviesResourceClientGetMoviesCmd extends DefaultCommand {

    @Override
    public void run(
            final ClientConfiguration clientConfiguration) {
        System.out.println(new org.apache.johnzon.mapper.MapperBuilder().setPretty(true).build()
                .writeObjectAsString(new MovieClient(clientConfiguration).moviesresourceclient().getMovies()));
    }
}
