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
import com.github.javaparser.ast.comments.LineComment;

import static org.tomitribe.inget.common.Utils.getClazz;

public class AddMethodSeparators {

    private static final String separator = " ----------------------------------------------------------------------------------------";

    private AddMethodSeparators() {

    }

    public static String apply(String slurp) {
        final CompilationUnit unit = JavaParser.parse(slurp);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        for (final MethodDeclaration method : clazz.getMethods()) {
            method.setComment(new LineComment(separator));
        }

        return removeDuplicates(unit.toString());
    }

    private static String removeDuplicates(final String content) {
        final String comment = "\n    //" + separator;
        final String modified = content.replaceAll(comment + comment, comment);

        if (content.equals(modified)) {
            return content;
        }
        return removeDuplicates(modified);
    }
}
