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
import org.tomitribe.inget.common.Header;
import org.tomitribe.inget.common.Utils;

import java.util.function.Consumer;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 */
public class Add201CreateResponses {

    public static String apply(final String source, CompilationUnit rootClassUnit) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration resourceClass = getClazz(unit);
        final String resourceName = Utils.getRootName(resourceClass);
        ClassOrInterfaceDeclaration rootClass = Utils.getClazz(rootClassUnit);
        final String rootClassName = Utils.getRootName(rootClass);

        resourceClass.getMethods().stream().forEach(new Consumer<MethodDeclaration>() {

            @Override
            public void accept(MethodDeclaration m) {
                if (!Utils.isPOST(m)) {
                    Utils.removeApiResponse(m, 201);
                    return;
                }
                if (Utils.isRootResource(rootClassName, resourceName)) {
                    Utils.addApiResponse(m, 201, "Created",
                            new Header("Location", "The resource to the created ", rootClassName));
                } else {
                    Utils.addApiResponse(m, 201, "Created", null);
                }


            }
        });

        return unit.toString();
    }
}
