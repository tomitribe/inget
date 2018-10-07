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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.trapease.test;

import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class Generation {

    /**
     * This can save you time rubber-stamping current behavior.  Add something like this
     * to the test temporarily to save the current actual results as the expected.
     *
     * `Generation.saveResults("custom-package", "expected", movies.actual(".*\\.java$"));`
     *
     * Note if you use Intellij you'll need to referesh the files from disk afterwards or
     * Intellij will not see the updated file contents.
     */
    public static void saveResults(final String name, final String location, final Map<String, File> results) throws IOException {
        final URL resource = Resources.class.getClassLoader().getResource("root.txt");
        assertNotNull(resource);
        final File file = Urls.toFile(resource);

        final File module = parentOf("target", file);

        final File dir = Files.file(module, "src", "test", "resources", name, location);
        Files.mkdirs(dir);

        for (final Map.Entry<String, File> entry : results.entrySet()) {
            final File dest = new File(dir, entry.getKey());
            Files.mkparent(dest);
            IO.copy(entry.getValue(), dest);
        }
    }

    private static File parentOf(final String dir, final File file) {
        if (file.getName().equals(dir)) return file.getParentFile();
        return parentOf(dir, file.getParentFile());
    }
}
