package io.superbiz.video.rest.cmd.base;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import org.tomitribe.trapease.movie.rest.cmd.MoviesResourceClientAddMovieCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesResourceClientCountCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesResourceClientDeleteMovieCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesResourceClientFindCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesResourceClientGetMoviesCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesResourceClientUpdateMovieCmd;

public class MainCli {

    private MainCli() {
    }

    public static void main(
            String... args) {
        final Cli.CliBuilder<Runnable> cliBuilder = Cli.builder("cmdline");
        cliBuilder.withDefaultCommand(Help.class);
        cliBuilder.withCommand(Help.class);
        cliBuilder.withGroup("movies-resource-client").withDefaultCommand(Help.class)
                .withCommand(MoviesResourceClientFindCmd.class).withCommand(MoviesResourceClientGetMoviesCmd.class)
                .withCommand(MoviesResourceClientAddMovieCmd.class)
                .withCommand(MoviesResourceClientDeleteMovieCmd.class)
                .withCommand(MoviesResourceClientUpdateMovieCmd.class).withCommand(MoviesResourceClientCountCmd.class);
        final Cli<Runnable> cli = cliBuilder.build();
        cli.parse(args).run();
    }
}
