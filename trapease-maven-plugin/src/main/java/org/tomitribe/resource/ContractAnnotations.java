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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.Collection;

import static org.tomitribe.common.Utils.getClazz;

/**
 * Annotations of the form @Foo(value = "single value") are contracted to
 * be their shorter @Foo("single value") to make them easier to read
 */
public class ContractAnnotations {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        convert(clazz.getAnnotations());

        clazz.getMethods().stream()
                .forEach(method -> convert(method.getAnnotations()));

        clazz.getMethods().stream()
                .map(MethodDeclaration::getParameters)
                .flatMap(Collection::stream)
                .forEach(parameter -> convert(parameter.getAnnotations()));

        return unit.toString();
    }

    public static void convert(final NodeList<AnnotationExpr> annotations) {
        annotations.stream()
                .filter(annotationExpr -> annotationExpr instanceof NormalAnnotationExpr)
                .map(NormalAnnotationExpr.class::cast)
                .filter(normalAnnotationExpr -> normalAnnotationExpr.getPairs().size() == 1)
                .filter(normalAnnotationExpr -> "value".equals(normalAnnotationExpr.getPairs().get(0).getNameAsString()))
                .forEach(normalAnnotationExpr -> {
                    final int index = annotations.indexOf(normalAnnotationExpr);
                    annotations.set(index, new SingleMemberAnnotationExpr(
                                    normalAnnotationExpr.getName(),
                                    normalAnnotationExpr.getPairs().get(0).getValue())
                    );
                });
    }
}
