package org.tomitribe.trapease.movie.rest.cmd.base;

import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import javax.annotation.Generated;
import org.tomitribe.trapease.movie.rest.client.base.BasicConfiguration;
import org.tomitribe.trapease.movie.rest.client.base.ClientConfiguration;

@Generated("org.tomitribe.cmd.CmdGenerator")
public abstract class TrapeaseCommand implements Runnable {

    @Option(name = {
            "-l", "--url" }, type = OptionType.GLOBAL)
    private URL url;

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
        ClientConfiguration clientConfiguration = ClientConfiguration.builder().url(url).verbose(verbose).build();
        BasicConfiguration basicConfiguration = null;
        if (username != null && password != null) {
            basicConfiguration = BasicConfiguration.builder().header("Authorization").prefix("Basic").username(username)
                    .password(password).build();
            clientConfiguration = clientConfiguration.builder().basic(basicConfiguration).build();
        }
        return clientConfiguration;
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
            conf.put("general.url", url.toString());
        }
        if (username != null) {
            conf.put("basic.username", username);
        }
        if (password != null) {
            conf.put("basic.password", password);
        }
    }

    private void readValueConfigurationValueIfNotProvided(
            Properties conf) throws Exception {
        if (url == null && conf.containsKey("general.url")) {
            url = new URL(String.valueOf(conf.get("general.url")));
        }
        if (username == null && conf.containsKey("basic.username")) {
            username = conf.getProperty("basic.username");
        }
        if (password == null && conf.containsKey("basic.password")) {
            password = conf.getProperty("basic.password");
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
