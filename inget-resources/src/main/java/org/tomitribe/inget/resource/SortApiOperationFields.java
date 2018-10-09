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
import com.github.javaparser.ast.expr.MemberValuePair;
import org.tomitribe.inget.common.Utils;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * Ensures the Operation fields are ordered
 *
 */
public class SortApiOperationFields {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        clazz.getMethods().stream()
                .filter(Utils::isResourceMethod)
                .map(Utils::getOperation)
                .filter(o -> o != null)
                .forEach(operation -> {
                    Utils.sortNodes(operation.getPairs(), MemberValuePair::getNameAsString,
                            "summary",
                            "description",
                            "parameters",
                            "method",
                            "responses",
                            "security",
                            "servers",
                            "operationId",
                            "extensions",
                            "deprecated",
                            "externalDocs",
                            "tags"
                    );
                });

        return unit.toString();
    }
}
