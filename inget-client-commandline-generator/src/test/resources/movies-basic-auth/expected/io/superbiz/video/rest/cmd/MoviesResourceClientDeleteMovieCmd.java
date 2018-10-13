package io.superbiz.video.rest.cmd;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.superbiz.video.rest.client.MovieClient;
import io.superbiz.video.rest.cmd.base.DefaultCommand;
import org.tomitribe.inget.client.ClientConfiguration;

@Command(name = "delete-movie")
public class MoviesResourceClientDeleteMovieCmd extends DefaultCommand {

    @Override
    public void run(
            final ClientConfiguration clientConfiguration) {
        new MovieClient(clientConfiguration).moviesresourceclient().deleteMovie(id);
    }

    @Arguments(required = true)
    private long id;
}
