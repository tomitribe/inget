package io.superbiz.video.model.rest.cmd;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.superbiz.video.model.rest.client.MovieClient;
import io.superbiz.video.model.rest.client.base.ClientConfiguration;
import io.superbiz.video.rest.cmd.base.DefaultCommand;

@Command(name = "add-movie")
public class MoviesResourceClientAddMovieCmd extends DefaultCommand {

    @Override
    public void run(
            final ClientConfiguration clientConfiguration) {
        final io.superbiz.video.model.Movie movie = io.superbiz.video.model.Movie.builder().id(id).title(title)
                .director(director).genre(genre).year(year).rating(rating).build();
        System.out.println(new org.apache.johnzon.mapper.MapperBuilder().setPretty(true).build()
                .writeObjectAsString(new MovieClient(clientConfiguration).moviesresourceclient().addMovie(movie)));
    }

    @Option(name = "--id")
    private java.lang.String id;

    @Option(name = "--title")
    private java.lang.String title;

    @Option(name = "--director")
    private java.lang.String director;

    @Option(name = "--genre")
    private java.lang.String genre;

    @Option(name = "--year")
    private int year;

    @Option(name = "--rating")
    private int rating;
}
