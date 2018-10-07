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
package org.tomitribe.model;

import org.junit.Test;
import org.tomitribe.common.Configuration;
import org.tomitribe.trapease.test.Resources;
import org.tomitribe.trapease.test.Generation;

import static org.tomitribe.trapease.test.Scenarios.assertFiles;

public class OperationBulkDeleteTest {

    @Test
    public void testOnClass() throws Exception {
        final Resources resources = Resources.name("OperationBulkDeleteTest/testOnClass");

        Configuration.clean();
        Configuration.MODEL_SOURCES = resources.input().getAbsolutePath();
        Configuration.RESOURCE_SOURCES = resources.input().getAbsolutePath();
        Configuration.GENERATED_SOURCES = resources.actual().getAbsolutePath();
        Configuration.MODEL_PACKAGE = "io.superbiz.video.model";
        Configuration.MODEL_SUFFIX = "Model";
        Configuration.TEMP_SOURCE = resources.tempSource().getAbsolutePath();

        // do the magic
        ModelGenerator.execute();

        Generation.saveResults("OperationBulkDeleteTest/testOnClass", "expected", resources.actual(".*\\.java$"));

        // check the magic
        assertFiles(resources.expected(".*\\.java$"), resources.actual(".*\\.java$"));
    }

    @Test
    public void testOnField() throws Exception {
        final Resources resources = Resources.name("OperationBulkDeleteTest/testOnField");

        Configuration.clean();
        Configuration.MODEL_SOURCES = resources.input().getAbsolutePath();
        Configuration.RESOURCE_SOURCES = resources.input().getAbsolutePath();
        Configuration.GENERATED_SOURCES = resources.actual().getAbsolutePath();
        Configuration.MODEL_PACKAGE = "io.superbiz.video.model";
        Configuration.MODEL_SUFFIX = "Model";
        Configuration.TEMP_SOURCE = resources.tempSource().getAbsolutePath();

        // do the magic
        ModelGenerator.execute();

        Generation.saveResults("OperationBulkDeleteTest/testOnField", "expected", resources.actual(".*\\.java$"));

        // check the magic
        assertFiles(resources.expected(".*\\.java$"), resources.actual(".*\\.java$"));
    }
}
