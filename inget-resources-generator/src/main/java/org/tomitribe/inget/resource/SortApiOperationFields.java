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
import com.github.javaparser.ast.expr.MemberValuePair;
import org.tomitribe.inget.common.Utils;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * Ensures the Operation fields are ordered
 *
 */
public class SortApiOperationFields {

    private SortApiOperationFields(){

    }

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        clazz.getMethods().stream()
                .filter(Utils::isResourceMethod)
                .map(Utils::getOperation)
                .filter(o -> o != null)
                .forEach(operation -> {
                    Utils.sortNodes(operation.getPairs(), MemberValuePair::getNameAsString,
                            "summary",
                            "description",
                            "parameters",
                            "method",
                            "responses",
                            "security",
                            "servers",
                            "operationId",
                            "extensions",
                            "deprecated",
                            "externalDocs",
                            "tags"
                    );
                });

        return unit.toString();
    }
}
