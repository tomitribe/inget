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
 * If the @Tag annotation is not present, the subsequent checks will not succeed
 */
public class TagRequired {

    private static final String DEFAULT = "@Tag(name = \"Account\", description = \"This endpoint manages a single account.\")";

    public static String apply(String source, CompilationUnit rootClassUnit) {
        final ClassOrInterfaceDeclaration modelClass = Utils.getClazz(rootClassUnit);
        final String rootClassName = Utils.getRootName(modelClass);
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration resourceClass = getClazz(unit);
        final boolean isRootClass = Utils.isRootResource(rootClassName, resourceClass.getNameAsString());
        if (resourceClass.getAnnotationByName("Tag").isPresent()) return source;

        unit.addImport("io.swagger.v3.oas.annotations.tags.Tag");
        String annotationSource = "";
        if (isRootClass) {
            annotationSource = DEFAULT.replaceAll("Account", rootClassName).replaceAll("account", Utils.formatCamelCaseTo(rootClassName, " "));
        } else {
            final String plural = Utils.toPlural(rootClassName);
            annotationSource = DEFAULT.replaceAll("Account", plural).replaceAll("account", Utils.formatCamelCaseTo(plural, " "));
            annotationSource = annotationSource.replace("a single", "multiple");
        }
        resourceClass.addAnnotation(JavaParser.parseAnnotation(annotationSource));

        return unit.toString();
    }

}
