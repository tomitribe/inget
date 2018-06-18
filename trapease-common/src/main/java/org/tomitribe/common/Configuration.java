/*
 *
 *   Tomitribe Confidential
 *
 *  Copyright Tomitribe Corporation. 2018
 *
 *  The source code for this program is not published or otherwise divested
 *  of its trade secrets, irrespective of what has been deposited with the
 *  U.S. Copyright Office.
 *
 */

package org.tomitribe.common;

import java.io.File;

public class Configuration {

    public static String TEMP_SOURCE;
    public static String MODEL_SUFFIX;
    public static String RESOURCE_SOURCES;
    public static String MODEL_SOURCES;
    public static String GENERATED_SOURCES;
    public static String MAIN_CLASS = "org.org.tomitribe.model.ModelGenerator";
    public static String MODEL_PACKAGE;
    public static String RESOURCE_PACKAGE ;
    public static String RESOURCE_SUFFIX;
    public static String CMD_PACKAGE;
    public static String CLIENT_NAME;

    public static String getModelPath(){
        return MODEL_SOURCES + File.separator + Utils.transformPackageToPath(MODEL_PACKAGE);
    }

    public static String getResourcePath(){
        return RESOURCE_SOURCES + File.separator + Utils.transformPackageToPath(RESOURCE_PACKAGE);
    }
}
