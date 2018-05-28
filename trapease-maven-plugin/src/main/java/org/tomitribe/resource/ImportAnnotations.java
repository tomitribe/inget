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
package org.tomitribe.resource;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.function.Consumer;

import static org.tomitribe.common.Utils.getClazz;

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
