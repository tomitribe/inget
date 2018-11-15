/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

import io.airlift.airline.Option;
import io.airlift.airline.OptionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

public abstract class DefaultCommand implements Runnable {

    @Option(name = {
            "-l", "--url"}, type = OptionType.GLOBAL)
    private String url;

    @Option(name = {
            "-v", "--verbose"}, type = OptionType.GLOBAL)
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
        ClientConfiguration.ClientConfigurationBuilder builder =
                ClientConfiguration.builder().url(url).verbose(verbose);

    }

    private void manageConfiguration() throws Exception {
        Properties conf = new Properties();
        File folder = new File(System.getProperty("user.home") + File.separator + ".%cmdLineName%");
        File file = new File(folder, ".%cmdLineName%config");
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

    private void updateConfigWithNewValue(Properties conf) {
        if (url != null) {
            conf.put("general.url", url);
        }
    }

    private void readValueConfigurationValueIfNotProvided(Properties conf) throws Exception {
        if (url == null && conf.containsKey("general.url")) {
            url = (String) conf.get("general.url");
        }
    }


    protected abstract void run(
            final ClientConfiguration clientConfiguration);
}
