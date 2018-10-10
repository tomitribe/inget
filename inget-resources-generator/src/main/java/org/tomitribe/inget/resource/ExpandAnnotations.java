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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.Collection;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * Annotations of the form @Foo("single value") are expanded to
 * be their longer @Foo(value = "single value") to make them
 * easier to work with in the JavaParser API
 */
public class ExpandAnnotations {

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

    public static void convert(NodeList<AnnotationExpr> annotations) {
        annotations.stream()
                .filter(annotationExpr -> annotationExpr instanceof SingleMemberAnnotationExpr)
                .map(SingleMemberAnnotationExpr.class::cast)
                .forEach(singleMemberAnnotationExpr -> {
                    final int index = annotations.indexOf(singleMemberAnnotationExpr);
                    annotations.set(index, new NormalAnnotationExpr(
                            singleMemberAnnotationExpr.getName(),
                            new NodeList<>(new MemberValuePair("value", singleMemberAnnotationExpr.getMemberValue()))
                    ));
                });
    }
}
