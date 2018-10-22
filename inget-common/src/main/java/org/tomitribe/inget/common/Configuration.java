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

    public static String TEMP_SOURCE;
    public static String MODEL_SUFFIX;
    public static String RESOURCE_SOURCES;
    public static String MODEL_SOURCES;
    public static String GENERATED_SOURCES;
    public static String MAIN_CLASS = "org.tomitribe.model.ModelGenerator";
    public static String MODEL_PACKAGE;
    public static String RESOURCE_PACKAGE;
    public static String RESOURCE_SUFFIX;
    public static String CLIENT_NAME;
    public static String CLIENT_SOURCES;
    public static String CMD_PACKAGE;
    public static String CMD_LINE_NAME;
    public static Authentication AUTHENTICATION;

    public static String getModelPath() {
        return MODEL_SOURCES + File.separator + Utils.transformPackageToPath(MODEL_PACKAGE);
    }

    public static String getResourcePath() {
        return RESOURCE_SOURCES + File.separator + Utils.transformPackageToPath(RESOURCE_PACKAGE);
    }

    public static String getClientPath() {
        return CLIENT_SOURCES + File.separator + Utils.transformPackageToPath(RESOURCE_PACKAGE) + File.separator + "client";
    }

    public static String getClientPackage() {
        return Configuration.RESOURCE_PACKAGE + ".client";
    }

    public static void clean() {
        TEMP_SOURCE = null;
        MODEL_SUFFIX = null;
        RESOURCE_SOURCES = null;
        MODEL_SOURCES = null;
        GENERATED_SOURCES = null;
        MODEL_PACKAGE = null;
        RESOURCE_PACKAGE = null;
        RESOURCE_SUFFIX = null;
        CLIENT_NAME = null;
        CLIENT_SOURCES = null;
        CMD_PACKAGE = null;
        AUTHENTICATION = null;
    }
}
