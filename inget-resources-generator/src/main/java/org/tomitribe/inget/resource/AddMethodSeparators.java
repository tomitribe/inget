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
package org.tomitribe.inget.resource;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;

import static org.tomitribe.inget.common.Utils.getClazz;

public class AddMethodSeparators {

    private static final String separator = " ----------------------------------------------------------------------------------------";

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
