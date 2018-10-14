package io.superbiz.video.rest.cmd.base;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import io.superbiz.video.rest.cmd.MoviesResourceClientAddMovieCmd;
import io.superbiz.video.rest.cmd.MoviesResourceClientCountCmd;
import io.superbiz.video.rest.cmd.MoviesResourceClientDeleteMovieCmd;
import io.superbiz.video.rest.cmd.MoviesResourceClientFindCmd;
import io.superbiz.video.rest.cmd.MoviesResourceClientGetMoviesCmd;
import io.superbiz.video.rest.cmd.MoviesResourceClientUpdateMovieCmd;

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
        try {
            cli.parse(args).run();
        } catch (Exception e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }
}
