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
package org.tomitribe.inget.resource;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.tomitribe.inget.common.Utils;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 */
public class Response404onIdReferences {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        clazz.getMethods().stream()
                .filter(Utils::hasPathParameter)
                .forEach(method -> Utils.addApiResponse(method, 404, "Not Found", null));

        clazz.getMethods().stream()
                .filter(method -> !Utils.hasPathParameter(method))
                .forEach(method -> Utils.removeApiResponse(method, 404));

        return unit.toString();
    }
}
