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
package org.tomitribe.inget.common;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All of the rules will add any required imports when adding
 * an annotation.  This may result in duplicate imports and will
 * result in unsorted imports.  Here we remove duplicates and
 * sort all remaining imports.
 *
 * This should run AFTER all rules that potentially add imports.
 */
public class RemoveDuplicateImports {

    private RemoveDuplicateImports() {
        // utility class
    }

    public static String apply(String source) {
        final CompilationUnit unit = JavaParser.parse(source);

        // Remove duplicates and sort
        final List<ImportDeclaration> imports = unit.getImports().stream()
                .distinct()
                .sorted(Comparator.comparing(ImportDeclaration::getNameAsString))
                .collect(Collectors.toList());

        unit.setImports(new NodeList<>(imports));

        return unit.toString();
    }

}
