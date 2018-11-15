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
package org.tomitribe.inget.model;

import org.junit.Test;
import org.tomitribe.inget.common.Configuration;
import org.tomitribe.inget.test.Resources;

import static org.tomitribe.inget.test.Scenarios.assertFiles;

public class OperationUpdateTest {

    @Test
    public void testOnClass() throws Exception {
        final Resources resources = Resources.here().input("/*\n" +
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
                "@Model(operation = Model.Operation.UPDATE)\n" +
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

        Configuration.clean();
        Configuration.modelSources = resources.input().getAbsolutePath();
        Configuration.resourceSources = resources.input().getAbsolutePath();
        Configuration.generatedSources = resources.actual().getAbsolutePath();
        Configuration.modelPackage = "io.superbiz.video.model";
        Configuration.modelSuffix = "Model";
        Configuration.tempSource = resources.tempSource().getAbsolutePath();

        // do the magic
        ModelGenerator.execute();

        // Generation.saveResults("OperationUpdateTest/testOnClass", "expected", resources.actual(".*\\.java$"));

        // check the magic
        assertFiles(resources.expected(".*\\.java$"), resources.actual(".*\\.java$"));
    }

    @Test
    public void testOnField() throws Exception {
        final Resources resources = Resources.here().input("/*\n" +
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
                "    @Model(operation = Model.Operation.UPDATE)\n" +
                "    private String title;\n" +
                "    private String director;\n" +
                "    private String genre;\n" +
                "    @Model(operation = Model.Operation.UPDATE)\n" +
                "    private int year;\n" +
                "    private int rating;\n" +
                "}\n");

        Configuration.clean();
        Configuration.modelSources = resources.input().getAbsolutePath();
        Configuration.resourceSources = resources.input().getAbsolutePath();
        Configuration.generatedSources = resources.actual().getAbsolutePath();
        Configuration.modelPackage = "io.superbiz.video.model";
        Configuration.modelSuffix = "Model";
        Configuration.tempSource = resources.tempSource().getAbsolutePath();

        // do the magic
        ModelGenerator.execute();

        // Generation.saveResults("OperationUpdateTest/testOnField", "expected", resources.actual(".*\\.java$"));

        // check the magic
        assertFiles(resources.expected(".*\\.java$"), resources.actual(".*\\.java$"));
    }
}
