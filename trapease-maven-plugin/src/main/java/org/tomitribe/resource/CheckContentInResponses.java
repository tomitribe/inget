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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import org.tomitribe.common.Utils;

import java.util.Map;
import java.util.function.Consumer;

import static org.tomitribe.common.Utils.getClazz;
import static org.tomitribe.common.Utils.pairs;

/**
 */
public class CheckContentInResponses {

    public static String apply(final String source, final CompilationUnit modelClassUnit, CompilationUnit resourceClassUnit) {
        final CompilationUnit unit = JavaParser.parse(source);
        final ClassOrInterfaceDeclaration clazz = getClazz(unit);

        clazz.getMethods().stream().forEach(new Consumer<MethodDeclaration>() {

            @Override
            public void accept(MethodDeclaration m) {
                if (m.getType() == null || m.getType().equals("void")) {
                    return;
                }

                final NormalAnnotationExpr apiResponses = Utils.getAnnotation(m, "ApiResponses");
                final MemberValuePair value = pairs(apiResponses).get("value");
                final NodeList<NormalAnnotationExpr> annotations = Utils.arrayValue(value.getValue());

                annotations.stream().forEach(applyContent(m, unit, modelClassUnit, resourceClassUnit));

            }
        });

        return unit.toString();
    }

    private static Consumer<NormalAnnotationExpr> applyContent(final MethodDeclaration m, final CompilationUnit unit,
                                                               final CompilationUnit modelClassUnit,
                                                               final CompilationUnit resourceClassUnit) {
        return new Consumer<NormalAnnotationExpr>() {
            @Override
            public void accept(NormalAnnotationExpr responseAnnotation) {
                if (Utils.has(responseAnnotation, "responseCode", "\"200\"")
                        || Utils.has(responseAnnotation, "responseCode", "\"201\"")) {

                    ClassOrInterfaceDeclaration modelClass = Utils.getClazz(modelClassUnit);
                    final String modelClassName = Utils.getRootName(modelClass);
                    ;
                    final String modelClassPackage = modelClassUnit.getPackageDeclaration().get().getName().toString();

                    final Map<String, MemberValuePair> pairs = Utils.pairs(responseAnnotation);
                    final MemberValuePair code = pairs.get("content");
                    if (code == null) {
                        unit.addImport("io.swagger.v3.oas.annotations.media.Content");
                        unit.addImport("io.swagger.v3.oas.annotations.media.Schema");

                        if (Utils.isBulkMethod(m)) {
                            final String bulkClassName = "Bulk" + modelClassName + "Result";
                            unit.addImport(modelClassPackage + "." + bulkClassName);
                            responseAnnotation.addPair("content", "@Content(schema = @Schema(implementation = " + bulkClassName + ".class))");
                        } else {
                            String className = modelClassName;
                            ClassOrInterfaceDeclaration resourceClass = Utils.getClazz(resourceClassUnit);

                            if (!Utils.isRootResource(modelClassName, resourceClass.getNameAsString())) {
                                className = Utils.toPlural(modelClassName);
                            }
                            unit.addImport(modelClassPackage + "." + className);
                            responseAnnotation.addPair("content", "@Content(schema = @Schema(implementation = " + className + ".class))");
                        }

                    }
                }
            }
        };
    }
}
