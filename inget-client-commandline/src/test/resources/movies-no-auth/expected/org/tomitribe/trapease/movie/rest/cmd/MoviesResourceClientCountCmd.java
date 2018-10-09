package org.tomitribe.trapease.movie.rest.cmd;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.tomitribe.trapease.movie.rest.client.MovieClient;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.trapease.movie.rest.cmd.base.DefaultCommand;

@Command(name = "count")
public class MoviesResourceClientCountCmd extends DefaultCommand {

    @Override
    public void run(
            final ClientConfiguration clientConfiguration) {
        System.out.println(new org.apache.johnzon.mapper.MapperBuilder().setPretty(true).build().writeObjectAsString(
                new MovieClient(clientConfiguration).moviesresourceclient().count(field, searchTerm)));
    }

    @Option(name = "--field")
    private java.lang.String field;

    @Option(name = "--search-term")
    private java.lang.String searchTerm;
}
