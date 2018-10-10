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


package org.tomitribe.inget;

import org.junit.After;
import org.junit.Test;
import org.tomitribe.inget.client.ClientGenerator;
import org.tomitribe.inget.common.Configuration;
import org.tomitribe.inget.common.CustomTypeSolver;
import org.tomitribe.inget.test.Resources;

import static org.tomitribe.inget.test.Scenarios.assertFiles;

public class ClientGeneratorTest {

    @After
    public void after(){
        Configuration.clean();
    }

    @Test
    public void testMovies() throws Exception {
        final Resources movies = Resources.name("movies");

        Configuration.MODEL_SOURCES = movies.input().getAbsolutePath();
        Configuration.RESOURCE_SOURCES = movies.input().getAbsolutePath();
        Configuration.GENERATED_SOURCES = movies.actual().getAbsolutePath();
        Configuration.CLIENT_SOURCES = movies.input().getAbsolutePath();
        Configuration.MODEL_PACKAGE = "io.superbiz.video.model";
        Configuration.RESOURCE_PACKAGE = "io.superbiz.video.rest";
        Configuration.CLIENT_NAME = "MovieClient";
        Configuration.RESOURCE_SUFFIX = "ResourceBean";
        Configuration.MODEL_SUFFIX = "Model";
        Configuration.TEMP_SOURCE = movies.tempSource().getAbsolutePath();

        CustomTypeSolver.init();

        ClientGenerator.execute();

        assertFiles(movies.expected(".*\\.java$"), movies.actual(".*\\.java$"));
    }
}