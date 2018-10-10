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
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.Collection;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * Simple check that ensures all parameters are final
 */
public class ParametersMustBeFinal {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        clazz.getMethods().stream()
                .map(MethodDeclaration::getParameters)
                .flatMap(Collection::stream)
                .forEach(parameter -> parameter.setFinal(true));

        return unit.toString();
    }
}
