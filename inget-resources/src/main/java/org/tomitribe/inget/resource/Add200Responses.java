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
import org.tomitribe.inget.common.Utils;

import java.util.function.Consumer;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 */
public class Add200Responses {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        clazz.getMethods().stream().forEach(new Consumer<MethodDeclaration>() {

            @Override
            public void accept(MethodDeclaration m) {
                if (Utils.isPOST(m)) {
                    Utils.removeApiResponse(m, 200);
                    return;
                }
                Utils.addApiResponse(m, 200, "Success", null);
            }

        });

        return unit.toString();
    }
}
