package org.tomitribe.trapease.movie.rest.cmd;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.tomitribe.trapease.movie.rest.client.MovieClient;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.trapease.movie.rest.cmd.base.TrapeaseCommand;

@Command(name = "delete-movie")
public class MoviesResourceClientDeleteMovieCmd extends TrapeaseCommand {

    @Override
    public void run(
            final ClientConfiguration clientConfiguration) {
        new MovieClient(clientConfiguration).moviesresourceclient().deleteMovie(id);
    }

    @Arguments(required = true)
    private long id;
}
