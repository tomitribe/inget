import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import org.tomitribe.trapease.movie.rest.cmd.MoviesAddMovieCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesCountCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesDeleteMovieCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesFindCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesGetMoviesCmd;
import org.tomitribe.trapease.movie.rest.cmd.MoviesUpdateMovieCmd;

public class TrapeaseCli {

    private TrapeaseCli() {
    }

    public static void main(
            String... args) {
        final Cli.CliBuilder<Runnable> cliBuilder = Cli.builder("trapease");
        cliBuilder.withDefaultCommand(Help.class);
        cliBuilder.withCommand(Help.class);
        cliBuilder.withGroup("movies").withDefaultCommand(Help.class).withCommand(MoviesFindCmd.class)
                .withCommand(MoviesGetMoviesCmd.class).withCommand(MoviesAddMovieCmd.class)
                .withCommand(MoviesDeleteMovieCmd.class).withCommand(MoviesUpdateMovieCmd.class)
                .withCommand(MoviesCountCmd.class);
        final Cli<Runnable> cli = cliBuilder.build();
        cli.parse(args).run();
    }
}
