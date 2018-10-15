package io.superbiz.video.rest.cmd.base;

import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import javax.annotation.Generated;
import org.tomitribe.inget.client.ClientConfiguration;
import org.tomitribe.inget.client.SignatureConfiguration;

@Generated("org.tomitribe.inget.cmd.CmdGenerator")
public abstract class DefaultCommand implements Runnable {

    @Option(name = {
            "-l", "--url" }, type = OptionType.GLOBAL)
    private String url;

    @Option(name = {
            "-v", "--verbose" }, type = OptionType.GLOBAL)
    private boolean verbose;

    @Override
    public final void run() {
        try {
            manageConfiguration();
        } catch (Exception e) {
            System.out.println("Error to manage configuration file: " + e.getMessage());
        }
        run(buildConfiguration());
    }

    private ClientConfiguration buildConfiguration() {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder().url(url)
                .verbose(verbose);
        SignatureConfiguration signatureConfiguration = null;
        if (keyId != null && keyLocation != null) {
            signatureConfiguration = SignatureConfiguration.builder().keyId(keyId).keyLocation(keyLocation)
                    .signatureDetails(signatureDetails).header("Authorization").prefix("Signature").build();
            builder.signature(signatureConfiguration);
        }
        return builder.build();
    }

    private void manageConfiguration() throws Exception {
        Properties conf = new Properties();
        File folder = new File(System.getProperty("user.home") + File.separator + ".cmdline");
        File file = new File(folder, ".cmdlineconfig");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        conf.load(new FileInputStream(file));
        updateConfigWithNewValue(conf);
        readValueConfigurationValueIfNotProvided(conf);
        OutputStream out = new FileOutputStream(file);
        conf.store(out, null);
    }

    private void updateConfigWithNewValue(
            Properties conf) {
        if (url != null) {
            conf.put("general.url", url);
        }
        if (keyId != null) {
            conf.put("signature.key-id", keyId);
        }
        if (keyLocation != null) {
            conf.put("signature.key-location", keyLocation);
        }
    }

    private void readValueConfigurationValueIfNotProvided(
            Properties conf) throws Exception {
        if (url == null && conf.containsKey("general.url")) {
            url = (String) conf.get("general.url");
        }
        if (keyId == null && conf.containsKey("signature.key-id")) {
            keyId = conf.getProperty("signature.key-id");
        }
        if (keyLocation == null && conf.containsKey("signature.key-location")) {
            keyLocation = conf.getProperty("signature.key-location");
        }
    }

    protected abstract void run(
            final ClientConfiguration clientConfiguration);

    @Option(name = {
            "-k", "--key-id" }, type = OptionType.GLOBAL)
    private String keyId;

    @Option(name = {
            "-n", "--key-location" }, type = OptionType.GLOBAL)
    private String keyLocation;

    @Option(name = {
            "-s", "--signature-details" }, type = OptionType.GLOBAL)
    private boolean signatureDetails;
}
