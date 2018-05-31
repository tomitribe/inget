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

package org.tomitribe.client;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.TypeParameter;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.apache.commons.lang3.text.WordUtils;
import org.tomitribe.client.base.ClientTemplates;
import org.tomitribe.common.Configuration;
import org.tomitribe.common.ImportManager;
import org.tomitribe.common.Reformat;
import org.tomitribe.common.RemoveDuplicateImports;
import org.tomitribe.common.Utils;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientGenerator {
    public static void execute() throws IOException {
        final List<File> files = Utils.getModel();

        createBaseClientClasses();
        CompilationUnit genericClientUnit = createResourceClient();
        genericClientUnit.addImport(ImportManager.getImport("RestClientBuilder"));
        genericClientUnit.addImport(ImportManager.getImport("JohnzonProvider"));

        for (final File file : files) {
            final String modelClassSource = IO.slurp(file);
            final CompilationUnit modelClassUnit = JavaParser.parse(modelClassSource);
            final ClassOrInterfaceDeclaration modelClass = Utils.getClazz(modelClassUnit);

            if (modelClass != null) {
                Optional<AnnotationExpr> resourceAnnotation = modelClass.getAnnotationByName("Resource");
                if (!resourceAnnotation.isPresent()) {
                    continue;
                }

                final String modelClassName = Utils.getRootName(modelClass);

                List<File> relatedResources = Utils.getResources(modelClassName);

                for (File resource : relatedResources) {
                    generateClient(resource, Utils.getClazz(genericClientUnit));
                }
            }
        }
        save(genericClientUnit.getPackageDeclaration().get().getNameAsString(), Configuration.RESOURCE_SUFFIX + "Client", genericClientUnit);
    }

    private static void createBaseClientClasses() throws IOException {
        final String outputBasePackage = Configuration.RESOURCE_PACKAGE + ".client.base";
        createClientConfiguration(outputBasePackage);
    }

    private static CompilationUnit createResourceClient() throws IOException {
        final String outputBasePackage = Configuration.RESOURCE_PACKAGE + ".client";
        final String resourceClientName = Configuration.RESOURCE_SUFFIX + "Client";
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(outputBasePackage);
        newClassCompilationUnit.addClass(resourceClientName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(resourceClientName).get();

        ConstructorDeclaration constructor = newClass.addConstructor(Modifier.PUBLIC);
        constructor.addParameter("ClientConfiguration", "config");
        newClassCompilationUnit.addImport(Configuration.RESOURCE_PACKAGE + ".client.base.ClientConfiguration");
        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);

        return newClassCompilationUnit;
    }

    private static void createClientConfiguration(String outputBasePackage) throws IOException {
        final CompilationUnit content = JavaParser.parse(ClientTemplates.CLIENT_CONFIGURATION);
        content.setPackageDeclaration(outputBasePackage);
        Utils.addGeneratedAnnotation(content, Utils.getClazz(content), null);
        Utils.save("ClientConfiguration.java", outputBasePackage, content.toString());
    }

    private static void generateClient(File resource, ClassOrInterfaceDeclaration genericResourceClientClass) throws IOException {
        final String resourceClientSource = IO.slurp(resource);
        final CompilationUnit resourceClientUnit = JavaParser.parse(resourceClientSource);
        final ClassOrInterfaceDeclaration resourceClientClass = Utils.getClazz(resourceClientUnit);

        final String clientClassPackage = Configuration.RESOURCE_PACKAGE + ".client.interfaces";
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(clientClassPackage);
        final String clientName = resource.getName().replace(".java", "Client");
        newClassCompilationUnit.addClass(clientName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(clientName).get();
        newClass.setInterface(true);

        createResourceClientReference(clientName, clientClassPackage, genericResourceClientClass, newClass);

        List<AnnotationExpr> classAnnotations = resourceClientClass.getAnnotations()
                .stream()
                .filter(a -> Utils.isJaxRSAnnotation(a))
                .collect(Collectors.toList());

        newClass.setAnnotations(new NodeList<>(classAnnotations));

        resourceClientClass.getMethods().stream().forEach(m -> {
            MethodDeclaration newMethod = m.clone();

            final String type = Utils.getResponseImplementation(newMethod);
            if (type != null) {
                newMethod.setType(new TypeParameter(type));
            }

            List<AnnotationExpr> annotations = newMethod.getAnnotations().stream()
                    .filter(a -> Utils.isJaxRSAnnotation(a))
                    .collect(Collectors.toList());

            newMethod.setAnnotations(new NodeList<>(annotations));

            newMethod.getParameters().stream().forEach(p -> {
                List<AnnotationExpr> parameterAnnotations = p.getAnnotations().stream()
                        .filter(a -> Utils.isJaxRSAnnotation(a))
                        .collect(Collectors.toList());

                p.setAnnotations(new NodeList<>(parameterAnnotations));
            });

            newClass.addMember(newMethod);
        });

        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);
        Utils.addImports(resourceClientUnit, newClassCompilationUnit);
        Utils.addImports(newClassCompilationUnit, genericResourceClientClass.findCompilationUnit().get());
        Utils.addLicense(resourceClientUnit, newClassCompilationUnit);
        save(clientClassPackage, clientName, newClassCompilationUnit);
    }

    private static void createResourceClientReference(String clientName, String pkg, ClassOrInterfaceDeclaration genericClientClass, ClassOrInterfaceDeclaration resourceClientClass) {
        VariableDeclarator var = new VariableDeclarator(new TypeParameter(resourceClientClass.getNameAsString()), WordUtils.uncapitalize(resourceClientClass.getNameAsString()));
        FieldDeclaration reference = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), var);
        genericClientClass.getMembers().add(0, reference);
        genericClientClass.findCompilationUnit().get().addImport(pkg + "." + clientName);
        String name = clientName.replace(Configuration.RESOURCE_SUFFIX + "Client", "");
        MethodDeclaration referenceMethod = new MethodDeclaration();
        referenceMethod.setModifiers(EnumSet.of(Modifier.PUBLIC));
        referenceMethod.setName(name.toLowerCase());
        referenceMethod.setType(resourceClientClass.getNameAsString());
        referenceMethod.setBody(JavaParser.parseBlock("{ return this." + WordUtils.uncapitalize(resourceClientClass.getNameAsString()) + "; }"));
        genericClientClass.addMember(referenceMethod);

        ConstructorDeclaration constructor = genericClientClass.getConstructors().stream().findFirst().get();

        StringBuilder cBuilder = new StringBuilder();
        cBuilder.append(WordUtils.uncapitalize(resourceClientClass.getNameAsString()) + " = RestClientBuilder.newBuilder()");
        cBuilder.append(".baseUrl(config.getTagUrl())");
        cBuilder.append(".register(JohnzonProvider.class)");
        cBuilder.append(".build(" + resourceClientClass.getNameAsString() + ".class);");
        constructor.getBody().asBlockStmt().addStatement(JavaParser.parseStatement(cBuilder.toString()));

    }

    public static void save(String packageLocation, String className, CompilationUnit classToBeSaved) throws IOException {
        if (classToBeSaved == null) {
            return;
        }

        String modified = Stream.of(classToBeSaved.toString())
                .map(RemoveDuplicateImports::apply)
                .map(Reformat::apply)
                .map(RemoveUnusedImports::removeUnusedImports)
                .findFirst().get();

        Utils.save(className + ".java", packageLocation, modified);
    }
}