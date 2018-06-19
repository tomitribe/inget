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
import org.tomitribe.common.Utils;

import static org.tomitribe.common.Utils.getClazz;

/**
 * The create methods for the major entities such as Account,
 * Group, Role, etc all return 201 on a successful create.
 *
 * They also return 409 if the item exists.  Here we ensure that
 * all POSTs that return 201 also have a 409.
 */
public class Response409onCreateConflict {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        // Ensure create method returning 201 also return 409
        clazz.getMethods().stream()
                .filter(m -> Utils.isMethodCreate(m) || Utils.isMethodBulkCreate(m))
                .forEach(method -> Utils.addApiResponse(method, 409, "Conflict", null));

        // Ensure that a non create method do NOT return 409
        clazz.getMethods().stream()
                .filter(m -> !(Utils.isMethodCreate(m) || Utils.isMethodBulkCreate(m)))
                .forEach(m -> Utils.removeApiResponse(m, 409));

        return unit.toString();
    }

}
