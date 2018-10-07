import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import org.tomitribe.trapease.movie.rest.client.base.BasicConfiguration;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;
import org.tomitribe.trapease.movie.rest.client.base.SignatureConfiguration;

import java.net.URL;

public abstract class TrapeaseCommand implements Runnable {

    @Option(name = {
            "-l", "--url"}, type = OptionType.GLOBAL)
    private URL url;

    @Option(name = {
            "-v", "--verbose"}, type = OptionType.GLOBAL)
    private boolean verbose;

    @Option(name = {
            "-k", "--key-id"}, type = OptionType.GLOBAL)
    private String keyId;

    @Option(name = {
            "-n", "--key-location"}, type = OptionType.GLOBAL)
    private String keyLocation;

    @Option(name = {
            "-u", "--username"}, type = OptionType.GLOBAL)
    private String username;

    @Option(name = {
            "-p", "--password"}, type = OptionType.GLOBAL)
    private String password;

    @Override
    public final void run() {
        SignatureConfiguration signatureConfiguration = null;
        BasicConfiguration basicConfiguration = null;
        if (keyId != null || keyLocation != null) {
            signatureConfiguration = SignatureConfiguration.builder()
                    .keyId(keyId)
                    .keyLocation(keyLocation)
                    .header("Authorization")
                    .prefix("Signature").build();
        }

        if (username != null && password != null) {
            basicConfiguration = BasicConfiguration.builder()
                    .header("Authorization")
                    .prefix("Basic")
                    .username(username)
                    .password(password)
                    .build();
        }

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder().url(url).verbose(verbose)
                .signature(signatureConfiguration)
                .basic(basicConfiguration)
                .build();

        run(clientConfiguration);
    }

    protected abstract void run(
            final ClientConfiguration clientConfiguration);
}
