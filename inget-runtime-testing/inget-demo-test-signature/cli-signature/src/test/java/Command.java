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
import org.junit.After;
import org.junit.Before;
import org.tomitribe.inget.movie.rest.cmd.base.MainCli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Command {
    protected ByteArrayOutputStream outLogs = null;
    protected ByteArrayOutputStream errLogs = null;

    @Before
    public void resetLogs() {
        outLogs = new ByteArrayOutputStream();
        errLogs = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outLogs));
        System.setErr(new PrintStream(errLogs));
    }

    @After
    public void restoreStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    protected static void cmd(final String cmd, final String url) {
        List<String> params = new ArrayList<>(Arrays.asList(cmd.split(" (?=(([^'\"]*['\"]){2})*[^'\"]*$)")));
        params = params.stream().map(p -> p.replaceAll("\"", "")).collect(Collectors.toList());
        if (url != null) {
            params.add(0, "--url");
            params.add(1, url.toString());
        }
        MainCli.main(params.toArray(new String[]{}));
    }

    protected static void cmd(final String cmd) {
        cmd(cmd, null);
    }
}
