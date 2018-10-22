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
package org.tomitribe.inget.test;

import org.tomitribe.util.IO;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class Scenarios {

    public static void assertFiles(final Map<String, File> expected, final Map<String, File> actual) throws IOException {
        for (final String name : expected.keySet()) {
            final File expectedFile = expected.get(name);
            final File actualFile = actual.remove(name);

            assertNotNull(expectedFile);
            assertNotNull("Missing " + name, actualFile);
            assertEquals("Incorrect Contents: " + name, IO.slurp(expectedFile), IO.slurp(actualFile));
        }

        final Set<String> unexpected = actual.keySet();
        if (unexpected.size() > 0) {
            fail("Unexpected Files: \n" + Join.join("\n", unexpected));
        }
    }
}
