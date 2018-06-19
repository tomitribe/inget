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
import org.tomitribe.common.Utils;

import static org.tomitribe.common.Utils.getClazz;

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
