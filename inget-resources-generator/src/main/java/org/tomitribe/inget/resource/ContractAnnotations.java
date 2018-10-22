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
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.Collection;

import static org.tomitribe.inget.common.Utils.getClazz;

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
