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
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.tomitribe.inget.common.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.tomitribe.inget.common.Utils.getClazz;
import static org.tomitribe.inget.common.Utils.isMethodReadAll;
import static org.tomitribe.inget.common.Utils.toPlural;

/**
 */
public class OperationRequired {

    private static final Map<String, String> methods = new HashMap<>();


    static {
        methods.put("readAll", "@Operation(summary = \"Read all accounts.\")");
    }

    public static String apply(String source, CompilationUnit rootClassUnit) {
        ClassOrInterfaceDeclaration rootClass = Utils.getClazz(rootClassUnit);
        final String rootClassName = Utils.getRootName(rootClass);
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        for (final MethodDeclaration method : clazz.getMethods()) {
            String annotationSource = Optional.ofNullable(getOperation(method))
                    .orElse(null);

            if (annotationSource != null) {
                annotationSource =
                        annotationSource
                                .replaceAll("accounts", toPlural(rootClassName))
                                .replaceAll("Account", rootClassName)
                                .replaceAll("account", rootClassName);

                Optional<AnnotationExpr> existingAnnotation = method.getAnnotationByName("Operation");
                if (existingAnnotation.isPresent()) {
                    method.getAnnotations().remove(existingAnnotation.get());
                }
                method.addAnnotation(JavaParser.parseAnnotation(annotationSource));
                unit.addImport("io.swagger.v3.oas.annotations.Operation");
            }
        }

        return unit.toString();
    }

    private static String getOperation(MethodDeclaration method) {
        if (isMethodReadAll(method)) {
            return methods.get("findAll");
        }

        return null;
    }
}
