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


package org.tomitribe.inget.cmd;

import org.junit.After;
import org.junit.Test;
import org.tomitribe.inget.common.Authentication;
import org.tomitribe.inget.common.Configuration;
import org.tomitribe.inget.common.CustomTypeSolver;
import org.tomitribe.inget.test.Resources;

import static org.tomitribe.inget.test.Scenarios.assertFiles;

public class CmdGeneratorTest {

    @After
    public void after(){
        Configuration.clean();
    }

    @Test
    public void testCmdNoAuth() throws Exception {
        final Resources movies = Resources.name("movies-no-auth");

        Configuration.modelSources = movies.input().getAbsolutePath();
        Configuration.resourceSources = movies.input().getAbsolutePath();
        Configuration.generatedSources = movies.actual().getAbsolutePath();
        Configuration.clientSources = movies.input().getAbsolutePath();
        Configuration.modelPackage = "io.superbiz.video.model.model";
        Configuration.resourcePackage = "io.superbiz.video.model.rest";
        Configuration.clientName = "MovieClient";
        Configuration.resourceSuffix = "ResourceBean";
        Configuration.modelSuffix = "Model";
        Configuration.cmdLineName = "cmdline";
        Configuration.tempSource = movies.tempSource().getAbsolutePath();

        CustomTypeSolver.init();

        CmdGenerator.execute();

        assertFiles(movies.expected(".*\\.java$"), movies.actual(".*\\.java$"));
    }

    @Test
    public void testCmdBasic() throws Exception {
        final Resources movies = Resources.name("movies-basic-auth");

        Configuration.modelSources = movies.input().getAbsolutePath();
        Configuration.resourceSources = movies.input().getAbsolutePath();
        Configuration.generatedSources = movies.actual().getAbsolutePath();
        Configuration.clientSources = movies.input().getAbsolutePath();
        Configuration.modelPackage = "io.superbiz.video.model";
        Configuration.resourcePackage = "io.superbiz.video.rest";
        Configuration.clientName = "MovieClient";
        Configuration.resourceSuffix = "ResourceBean";
        Configuration.modelSuffix = "Model";
        Configuration.cmdLineName = "cmdline";
        Configuration.tempSource = movies.tempSource().getAbsolutePath();
        Configuration.authentication = Authentication.BASIC;

        CustomTypeSolver.init();

        CmdGenerator.execute();

        assertFiles(movies.expected(".*\\.java$"), movies.actual(".*\\.java$"));
    }

    @Test
    public void testCmdSignature() throws Exception {
        final Resources movies = Resources.name("movies-signature-auth");

        Configuration.modelSources = movies.input().getAbsolutePath();
        Configuration.resourceSources = movies.input().getAbsolutePath();
        Configuration.generatedSources = movies.actual().getAbsolutePath();
        Configuration.clientSources = movies.input().getAbsolutePath();
        Configuration.modelPackage = "io.superbiz.video.model.model";
        Configuration.resourcePackage = "io.superbiz.video.model.rest";
        Configuration.clientName = "MovieClient";
        Configuration.resourceSuffix = "ResourceBean";
        Configuration.modelSuffix = "Model";
        Configuration.cmdLineName = "cmdline";
        Configuration.tempSource = movies.tempSource().getAbsolutePath();
        Configuration.authentication = Authentication.SIGNATURE;

        CustomTypeSolver.init();

        CmdGenerator.execute();

        assertFiles(movies.expected(".*\\.java$"), movies.actual(".*\\.java$"));
    }

}