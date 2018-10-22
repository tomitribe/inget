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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.tomitribe.inget.common.Utils;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * Ensures the order of annotations on the Resource methods
 * is consistent across all the classes
 */
public class SortMethodAnnotations {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        for (final MethodDeclaration method : clazz.getMethods()) {
            Utils.sortNodes(method.getAnnotations(), AnnotationExpr::getNameAsString,
                    "GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH",
                    "Path",
                    "Produces",
                    "Consumes",
                    "Operation",
                    "ApiResponses");
        }

        return unit.toString();
    }

}
