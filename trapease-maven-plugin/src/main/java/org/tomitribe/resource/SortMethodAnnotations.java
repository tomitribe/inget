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
package org.tomitribe.resource;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.tomitribe.common.Utils;

import static org.tomitribe.common.Utils.getClazz;

/**
 * Ensures the order of annotations on the Resource methods
 * is consistent across all the classes
 */
public class SortMethodAnnotations {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        for (final MethodDeclaration method : clazz.getMethods()) {
            Utils.sortNodes(method.getAnnotations(), AnnotationExpr::getNameAsString,
                    "GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH",
                    "Path",
                    "Produces",
                    "Consumes",
                    "Operation",
                    "ApiResponses");
        }

        return unit.toString();
    }

}
