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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.function.Consumer;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * If a class annotation isn't fully imported, the checks
 * will not work because `getAnnotationByName("Api")` will
 * not return cases where the annotation declaration in source
 * is `@io.swagger.annotations.Api("foo")`
 *
 * We therefore make sure all annotation names are shortened
 * to the simplename and an import statement is added for the
 * full class name.
 *
 * Note the regular expression used cannot tell the difference
 * between a package or an outer class.  It will attempt to
 * import org.foo.OuterClass.InnerAnnotation
 *
 * Only way to solve that is to actually load classes which makes
 * this not really a source code tool, but a byte code loading tool.
 * So for the moment it is left as a known limitation.
 */
public class ImportAnnotations {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        final Consumer<AnnotationExpr> addImport = importer(unit);

        // Convert annotations on the class
        clazz.getAnnotations().stream()
                .forEach(addImport);

        // Convert annotations on each method
        clazz.getMethods().stream()
                .flatMap(method -> method.getAnnotations().stream())
                .forEach(addImport);

        // Convert annotations on each method's params
        clazz.getMethods().stream()
                .flatMap(method -> method.getParameters().stream())
                .flatMap(parameter -> parameter.getAnnotations().stream())
                .forEach(addImport);


        return unit.toString();
    }

    public static Consumer<AnnotationExpr> importer(final CompilationUnit unit) {
        return annotation -> {
            final String name = annotation.getNameAsString();
            if (name.contains(".")) {
                annotation.setName(name.replaceAll(".*\\.", ""));
                unit.addImport(name);
            }
        };
    }
}
