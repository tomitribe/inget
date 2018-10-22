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

import org.tomitribe.inget.api.Model;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.JarLocation;
import org.tomitribe.util.Strings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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

        // Remove the old "expected" files
        CleanOnExit.delete(dir);

        // Add the new "expected" files
        for (final Map.Entry<String, File> entry : results.entrySet()) {
            final File dest = new File(dir, entry.getKey());
            Files.mkparent(dest);
            IO.copy(entry.getValue(), dest);
        }
    }

    public static void saveContent(final String name, final String location, final Map<String, String> results) throws IOException {
        final URL resource = Resources.class.getClassLoader().getResource("root.txt");
        assertNotNull(resource);
        final File file = Urls.toFile(resource);

        final File module = parentOf("target", file);

        final File dir = Files.file(module, "src", "test", "resources", name, location);
        Files.mkdirs(dir);

        saveContent(results, dir);
    }

    public static void saveContent(final Map<String, String> results, final File dir) throws IOException {
        for (final Map.Entry<String, String> entry : results.entrySet()) {
            final File dest = new File(dir, entry.getKey());
            Files.mkparent(dest);
            IO.copy(IO.read(entry.getValue()), dest);
        }
    }

    private static File parentOf(final String dir, final File file) {
        if (file.getName().equals(dir)) return file.getParentFile();
        return parentOf(dir, file.getParentFile());
    }


    public static void main(String[] args) throws IOException {
        final File thisClazz = JarLocation.jarLocation(Generation.class);
        final File project = parentOf("inget-testing", thisClazz);

        final File srcTest = Files.file(project, "inget-model", "src", "test");

        for (final Model.Operation operation : Model.Operation.values()) {
            final String read = Strings.camelCase(operation.name().toLowerCase(), "_");
            final String testName = "Operation" + read + "Test";

            final HashMap<String, String> tests = new HashMap<>();
            tests.put("java/org/tomitribe/model/" + testName + ".java", "" +
                    "/*\n" +
                    " * Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                    " * contributor license agreements.  See the NOTICE file distributed with\n" +
                    " * this work for additional information regarding copyright ownership.\n" +
                    " * The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                    " * (the \"License\"); you may not use this file except in compliance with\n" +
                    " * the License.  You may obtain a copy of the License at\n" +
                    " *\n" +
                    " *     http://www.apache.org/licenses/LICENSE-2.0\n" +
                    " *\n" +
                    " *  Unless required by applicable law or agreed to in writing, software\n" +
                    " *  distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    " *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    " *  See the License for the specific language governing permissions and\n" +
                    " *  limitations under the License.\n" +
                    " */\n" +
                    "package org.tomitribe.model;\n" +
                    "\n" +
                    "import org.junit.Test;\n" +
                    "import org.tomitribe.inget.common.Configuration;\n" +
                    "import org.tomitribe.inget.test.Resources;\n" +
                    "import org.tomitribe.inget.test.Generation;\n" +
                    "\n" +
                    "import static org.tomitribe.inget.test.Scenarios.assertFiles;\n" +
                    "\n" +
                    "public class " + testName + " {\n" +
                    "\n" +
                    "    @Test\n" +
                    "    public void testOnClass() throws Exception {\n" +
                    "        final Resources resources = Resources.name(\"" + testName + "/testOnClass\");\n" +
                    "\n" +
                    "        Configuration.clean();\n" +
                    "        Configuration.MODEL_SOURCES = resources.input().getAbsolutePath();\n" +
                    "        Configuration.RESOURCE_SOURCES = resources.input().getAbsolutePath();\n" +
                    "        Configuration.GENERATED_SOURCES = resources.actual().getAbsolutePath();\n" +
                    "        Configuration.MODEL_PACKAGE = \"io.superbiz.video.model\";\n" +
                    "        Configuration.MODEL_SUFFIX = \"Model\";\n" +
                    "        Configuration.TEMP_SOURCE = resources.tempSource().getAbsolutePath();\n" +
                    "\n" +
                    "        // do the magic\n" +
                    "        ModelGenerator.execute();\n" +
                    "\n" +
                    "        Generation.saveResults(\"" + testName + "/testOnClass\", \"expected\", resources.actual(\".*\\\\.java$\"));\n" +
                    "\n" +
                    "        // check the magic\n" +
                    "        assertFiles(resources.expected(\".*\\\\.java$\"), resources.actual(\".*\\\\.java$\"));\n" +
                    "    }\n" +
                    "\n" +
                    "    @Test\n" +
                    "    public void testOnField() throws Exception {\n" +
                    "        final Resources resources = Resources.name(\"" + testName + "/testOnField\");\n" +
                    "\n" +
                    "        Configuration.clean();\n" +
                    "        Configuration.MODEL_SOURCES = resources.input().getAbsolutePath();\n" +
                    "        Configuration.RESOURCE_SOURCES = resources.input().getAbsolutePath();\n" +
                    "        Configuration.GENERATED_SOURCES = resources.actual().getAbsolutePath();\n" +
                    "        Configuration.MODEL_PACKAGE = \"io.superbiz.video.model\";\n" +
                    "        Configuration.MODEL_SUFFIX = \"Model\";\n" +
                    "        Configuration.TEMP_SOURCE = resources.tempSource().getAbsolutePath();\n" +
                    "\n" +
                    "        // do the magic\n" +
                    "        ModelGenerator.execute();\n" +
                    "\n" +
                    "        Generation.saveResults(\"" + testName + "/testOnField\", \"expected\", resources.actual(\".*\\\\.java$\"));\n" +
                    "\n" +
                    "        // check the magic\n" +
                    "        assertFiles(resources.expected(\".*\\\\.java$\"), resources.actual(\".*\\\\.java$\"));\n" +
                    "    }\n" +
                    "}\n");


            tests.put("resources/" + testName + "/testOnField/input/io/superbiz/video/model/MovieModel.java", "" +
                    "/*\n" +
                    " * Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                    " * contributor license agreements.  See the NOTICE file distributed with\n" +
                    " * this work for additional information regarding copyright ownership.\n" +
                    " * The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                    " * (the \"License\"); you may not use this file except in compliance with\n" +
                    " * the License.  You may obtain a copy of the License at\n" +
                    " *\n" +
                    " *      http://www.apache.org/licenses/LICENSE-2.0\n" +
                    " *\n" +
                    " * Unless required by applicable law or agreed to in writing, software\n" +
                    " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    " * See the License for the specific language governing permissions and\n" +
                    " * limitations under the License.\n" +
                    " */\n" +
                    "package io.superbiz.video.model;\n" +
                    "\n" +
                    "import org.tomitribe.api.Filter;\n" +
                    "import org.tomitribe.api.Model;\n" +
                    "import org.tomitribe.api.Resource;\n" +
                    "\n" +
                    "@Model\n" +
                    "@Resource\n" +
                    "class MovieModel {\n" +
                    "    @Model(id = true, operation = Model.Operation.READ)\n" +
                    "    private String id;\n" +
                    "    @Model(operation = Model.Operation." + operation.name() + ")\n" +
                    "    private String title;\n" +
                    "    private String director;\n" +
                    "    private String genre;\n" +
                    "    @Model(operation = Model.Operation." + operation.name() + ")\n" +
                    "    private int year;\n" +
                    "    private int rating;\n" +
                    "}\n");


            tests.put("resources/" + testName + "/testOnClass/input/io/superbiz/video/model/MovieModel.java", "" +
                    "/*\n" +
                    " * Licensed to the Apache Software Foundation (ASF) under one or more\n" +
                    " * contributor license agreements.  See the NOTICE file distributed with\n" +
                    " * this work for additional information regarding copyright ownership.\n" +
                    " * The ASF licenses this file to You under the Apache License, Version 2.0\n" +
                    " * (the \"License\"); you may not use this file except in compliance with\n" +
                    " * the License.  You may obtain a copy of the License at\n" +
                    " *\n" +
                    " *      http://www.apache.org/licenses/LICENSE-2.0\n" +
                    " *\n" +
                    " * Unless required by applicable law or agreed to in writing, software\n" +
                    " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    " * See the License for the specific language governing permissions and\n" +
                    " * limitations under the License.\n" +
                    " */\n" +
                    "package io.superbiz.video.model;\n" +
                    "\n" +
                    "import org.tomitribe.api.Filter;\n" +
                    "import org.tomitribe.api.Model;\n" +
                    "import org.tomitribe.api.Resource;\n" +
                    "\n" +
                    "@Model(operation = Model.Operation." + operation.name() + ")\n" +
                    "@Resource\n" +
                    "class MovieModel {\n" +
                    "    @Model(id = true, operation = Model.Operation.READ)\n" +
                    "    private String id;\n" +
                    "    private String title;\n" +
                    "    private String director;\n" +
                    "    private String genre;\n" +
                    "    private int year;\n" +
                    "    private int rating;\n" +
                    "}\n");

            saveContent(tests, srcTest);
        }
    }
}
