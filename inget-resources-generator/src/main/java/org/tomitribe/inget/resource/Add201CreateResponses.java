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
import org.tomitribe.inget.common.Header;
import org.tomitribe.inget.common.Utils;

import java.util.function.Consumer;

import static org.tomitribe.inget.common.Utils.getClazz;

public class Add201CreateResponses {

    private Add201CreateResponses() {

    }

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
