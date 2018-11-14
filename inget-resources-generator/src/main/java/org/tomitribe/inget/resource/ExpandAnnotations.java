/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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

    private ExpandAnnotations() {

    }

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
