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
import org.tomitribe.inget.common.Utils;

import static org.tomitribe.inget.common.Utils.getClazz;

/**
 * The create methods for the major entities such as Account,
 * Group, Role, etc all return 201 on a successful create.
 *
 * They also return 409 if the item exists.  Here we ensure that
 * all POSTs that return 201 also have a 409.
 */
public class Response409onCreateConflict {

    public static String apply(final String source) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        // Ensure create method returning 201 also return 409
        clazz.getMethods().stream()
                .filter(m -> Utils.isMethodCreate(m) || Utils.isMethodBulkCreate(m))
                .forEach(method -> Utils.addApiResponse(method, 409, "Conflict", null));

        // Ensure that a non create method do NOT return 409
        clazz.getMethods().stream()
                .filter(m -> !(Utils.isMethodCreate(m) || Utils.isMethodBulkCreate(m)))
                .forEach(m -> Utils.removeApiResponse(m, 409));

        return unit.toString();
    }

}
