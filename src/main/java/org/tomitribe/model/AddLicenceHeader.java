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
package org.tomitribe.model;

public class AddLicenceHeader {

    private static final String TOMITRIBE_LICENCE = "/*\n" +
            " * Tomitribe Confidential\n" +
            " *\n" +
            " * Copyright Tomitribe Corporation. 2018\n" +
            " *\n" +
            " * The source code for this program is not published or otherwise divested \n" +
            " * of its trade secrets, irrespective of what has been deposited with the \n" +
            " * U.S. Copyright Office.\n" +
            " */\n";

    public static String apply(final String source) {
        final int packageDeclaration = source.indexOf("package com.tomitribe.tribestream");

        return TOMITRIBE_LICENCE + source.substring(packageDeclaration);

    }
}
