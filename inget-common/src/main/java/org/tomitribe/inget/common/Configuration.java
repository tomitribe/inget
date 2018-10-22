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

package org.tomitribe.inget.common;

import java.io.File;

public class Configuration {

    private Configuration() {
        // Utility class
    }

    public static String tempSource;
    public static String modelSuffix;
    public static String resourceSources;
    public static String modelSources;
    public static String generatedSources;
    public static String mainClass = "org.tomitribe.model.ModelGenerator";
    public static String modelPackage;
    public static String resourcePackage;
    public static String resourceSuffix;
    public static String clientName;
    public static String clientSources;
    public static String cmdPackage;
    public static String cmdLineName;
    public static Authentication authentication;

    public static String getModelPath() {
        return modelSources + File.separator + Utils.transformPackageToPath(modelPackage);
    }

    public static String getResourcePath() {
        return resourceSources + File.separator + Utils.transformPackageToPath(resourcePackage);
    }

    public static String getClientPath() {
        return clientSources + File.separator + Utils.transformPackageToPath(resourcePackage) + File.separator + "client";
    }

    public static String getClientPackage() {
        return Configuration.resourcePackage + ".client";
    }

    public static void clean() {
        tempSource = null;
        modelSuffix = null;
        resourceSources = null;
        modelSources = null;
        generatedSources = null;
        modelPackage = null;
        resourcePackage = null;
        resourceSuffix = null;
        clientName = null;
        clientSources = null;
        cmdPackage = null;
        authentication = null;
    }
}
