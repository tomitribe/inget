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
import org.tomitribe.inget.client.BasicConfiguration;
import org.tomitribe.inget.client.ClientConfiguration;

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
        BasicConfiguration basicConfiguration = null;
        if (username != null && password != null) {
            basicConfiguration = BasicConfiguration.builder().header("Authorization").prefix("Basic").username(username)
                    .password(password).build();
            builder.basic(basicConfiguration);
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
        if (username != null) {
            conf.put("basic.username", username);
        }
        if (password != null) {
            conf.put("basic.password", java.util.Base64.getEncoder().encodeToString(password.getBytes()));
        }
    }

    private void readValueConfigurationValueIfNotProvided(
            Properties conf) throws Exception {
        if (url == null && conf.containsKey("general.url")) {
            url = (String) conf.get("general.url");
        }
        if (username == null && conf.containsKey("basic.username")) {
            username = conf.getProperty("basic.username");
        }
        if (password == null && conf.containsKey("basic.password")) {
            password = new String(java.util.Base64.getDecoder().decode(conf.getProperty("basic.password").getBytes()));
        }
    }

    protected abstract void run(
            final ClientConfiguration clientConfiguration);

    @Option(name = {
            "-u", "--username" }, type = OptionType.GLOBAL)
    private String username;

    @Option(name = {
            "-p", "--password" }, type = OptionType.GLOBAL)
    private String password;
}
