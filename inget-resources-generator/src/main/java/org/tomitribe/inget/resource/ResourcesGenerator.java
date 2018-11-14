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
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.tomitribe.inget.common.Configuration;
import org.tomitribe.inget.common.ImportManager;
import org.tomitribe.inget.common.Reformat;
import org.tomitribe.inget.common.RemoveDuplicateImports;
import org.tomitribe.inget.common.Utils;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ResourcesGenerator {

    private ResourcesGenerator() {

    }

    public static void execute() throws IOException {
        final List<File> files = Utils.getModel();

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

                relatedResources = generateResources(modelClassName, modelClassUnit, relatedResources);

                for (File resource : relatedResources) {
                    applyGenerationInResource(modelClassUnit, resource);
                }
            }
        }
    }

    private static List<File> generateResources(String modelClassName, CompilationUnit modelClassUnit, List<File> relatedResources) throws IOException {
        if (relatedResources.size() == 2) {
            return relatedResources;
        }

        final String rootResourceName = modelClassName + Configuration.resourceSuffix;
        Optional<File> rootResource = relatedResources.stream()
                .filter(f -> f.getName().equals(rootResourceName + ".java"))
                .findFirst();
        if (!rootResource.isPresent()) {
            generateResource(rootResourceName, modelClassUnit);
        }

        final String listResourceName = Utils.toPlural(modelClassName) + Configuration.resourceSuffix;
        Optional<File> listResource = relatedResources.stream()
                .filter(f -> f.getName().equals(listResourceName + ".java"))
                .findFirst();

        if (!listResource.isPresent()) {
            generateResource(listResourceName, modelClassUnit);
        }

        return Utils.getResources(modelClassName);
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

    private static void generateResource(final String resourceName, final CompilationUnit modelClassUnit) throws IOException {
        String resourceClassPackage = modelClassUnit.getPackageDeclaration().get().getNameAsString();
        resourceClassPackage = resourceClassPackage.replace(Configuration.modelPackage, Configuration.resourcePackage);
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(resourceClassPackage);
        newClassCompilationUnit.addClass(resourceName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(resourceName).get();
        newClass.setInterface(true);
        String path = Utils.formatCamelCaseTo(resourceName.replace("Resource", ""), "/");
        newClass.addSingleMemberAnnotation("Path", "\"" + path + "\"");
        newClassCompilationUnit.addImport(ImportManager.getImport("Path"));

        newClass.addSingleMemberAnnotation("Consumes", "MediaType.APPLICATION_JSON");
        newClass.addSingleMemberAnnotation("Produces", "MediaType.APPLICATION_JSON");
        newClassCompilationUnit.addImport(ImportManager.getImport("Consumes"));
        newClassCompilationUnit.addImport(ImportManager.getImport("Produces"));
        newClassCompilationUnit.addImport(ImportManager.getImport("MediaType"));
        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null, ResourcesGenerator.class);
        Utils.addLicense(modelClassUnit, newClassCompilationUnit);
        save(resourceClassPackage, resourceName, newClassCompilationUnit);
    }

    static void applyGenerationInResource(CompilationUnit modelClassUnit, File resource) throws IOException {
        String resourceSource = IO.slurp(resource);
        final CompilationUnit resourceUnit = JavaParser.parse(resourceSource);
        // Perform transformations
        final String modified = Stream.of(resourceSource)
                .map(s -> MethodGenerator.apply(resourceUnit, modelClassUnit))
                .map(ImportAnnotations::apply)
                .map(ExpandAnnotations::apply)

//                   REST API requirements
                .map(s -> TagRequired.apply(s, modelClassUnit))
                .map(s -> OperationRequired.apply(s, modelClassUnit))
                .map(Add200Responses::apply)
                .map(s -> Add201CreateResponses.apply(s, modelClassUnit))
                .map(s -> CheckContentInResponses.apply(s, modelClassUnit, resourceUnit))
                .map(Response409onCreateConflict::apply)
                .map(Response404onIdReferences::apply)
                .map(ParametersMustBeFinal::apply)

//                   Beautify the source
                .map(SortMethodAnnotations::apply)
                .map(SortApiOperationFields::apply)
                .map(ContractAnnotations::apply)
                .map(RemoveDuplicateImports::apply)
                .map(AddMethodSeparators::apply)
                .map(Reformat::apply)
                .map(RemoveUnusedImports::removeUnusedImports)
                .findFirst().get();

        CompilationUnit modifiedUnit = JavaParser.parse(modified);
        ClassOrInterfaceDeclaration modifiedClazz = Utils.getClazz(modifiedUnit);
        if (modifiedClazz.getMethods().size() > 0) {
            IO.copy(IO.read(modified), resource);
        } else {
            if (resource.exists() && IO.slurp(resource)
                    .contains("@Generated(\"org.tomitribe.inget.resource.ResourcesGenerator\")")) {
                resource.delete();
            }
        }


    }
}