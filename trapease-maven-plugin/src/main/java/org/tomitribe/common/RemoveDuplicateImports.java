/*
 *
 *   Tomitribe Confidential
 *
 *  Copyright Tomitribe Corporation. 2018
 *
 *  The source code for this program is not published or otherwise divested
 *  of its trade secrets, irrespective of what has been deposited with the
 *  U.S. Copyright Office.
 *
 */
package org.tomitribe.common;

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
