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

package org.tomitribe.inget.client;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.TypeParameter;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.apache.commons.lang3.text.WordUtils;
import org.tomitribe.inget.common.Configuration;
import org.tomitribe.inget.common.ImportManager;
import org.tomitribe.inget.common.Reformat;
import org.tomitribe.inget.common.RemoveDuplicateImports;
import org.tomitribe.inget.common.Utils;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientGenerator {

    private ClientGenerator() {
        // no-op
    }

    public static void execute() throws IOException {
        createClientExceptions(Configuration.resourcePackage + ".client.base");
        CompilationUnit genericClientUnit = createResourceClient();
        ClassOrInterfaceDeclaration genericClientClass = Utils.getClazz(genericClientUnit);
        genericClientUnit.addImport(ImportManager.getImport("RestClientBuilder"));
        genericClientUnit.addImport(ImportManager.getImport("JohnzonProvider"));

        ConstructorDeclaration constructor = genericClientClass.getConstructors().stream().findFirst().get();
        constructor.getBody().asBlockStmt().addStatement(JavaParser.parseStatement("RestClientBuilder builder = null;"));

        StringBuilder cBuilder = new StringBuilder();
        cBuilder.append("try {");
        cBuilder.append("builder = RestClientBuilder.newBuilder()" +
                ".baseUrl(new java.net.URL(config.getUrl()))\n" +
                ".register(JohnzonProvider.class)");
        cBuilder.append(".register(" + Configuration.clientName + "ExceptionMapper.class);");
        cBuilder.append(" } catch (java.net.MalformedURLException e) {");
        cBuilder.append("throw new javax.ws.rs.WebApplicationException(\"URL is not valid \" + e.getMessage());");
        cBuilder.append("}");

        constructor.getBody().asBlockStmt().addStatement(JavaParser.parseStatement(cBuilder.toString()));

        registerFilters(genericClientClass);

        Map<String, String> relatedResources = Utils.getResources();

        Iterator<Map.Entry<String, String>> it = relatedResources.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> resource = it.next();
            generateClient(resource.getKey(), resource.getValue(), genericClientClass);
        }
        save(genericClientUnit.getPackageDeclaration().get().getNameAsString(), Configuration.clientName, genericClientUnit);
    }

    private static void addCxfLogInterceptor(ClassOrInterfaceDeclaration clazz) {
        CompilationUnit unit = clazz.findCompilationUnit().get();
        unit.addImport(ImportManager.getImport("OutInterceptors"));
        unit.addImport(ImportManager.getImport("NoOpInterceptor"));
        clazz.addAnnotation(JavaParser.parseAnnotation("@OutInterceptors(classes = NoOpInterceptor.class)"));
    }

    private static CompilationUnit createResourceClient() throws IOException {
        final String outputBasePackage = Configuration.getClientPackage();
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(outputBasePackage);
        newClassCompilationUnit.addClass(Configuration.clientName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(Configuration.clientName).get();

        ConstructorDeclaration constructor = newClass.addConstructor(Modifier.PUBLIC);
        constructor.addParameter("ClientConfiguration", "config");
        newClassCompilationUnit.addImport(ImportManager.getImport("ClientConfiguration"));
        newClassCompilationUnit.addImport(
                Configuration.resourcePackage + ".client.base." + Configuration.clientName + "ExceptionMapper");
        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null, ClientGenerator.class);

        return newClassCompilationUnit;
    }

    private static void createClientExceptions(final String outputBasePackage) throws IOException {
        final CompilationUnit clientException = new CompilationUnit(outputBasePackage);
        clientException.addClass(Configuration.clientName + "Exception", Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration clientExceptionClass =
                clientException.getClassByName(Configuration.clientName + "Exception").get();
        clientExceptionClass.addExtendedType(RuntimeException.class);
        Utils.addGeneratedAnnotation(clientException, Utils.getClazz(clientException), null, ClientGenerator.class);
        save(outputBasePackage, Configuration.clientName + "Exception", clientException);

        final CompilationUnit entityNotFoundException = new CompilationUnit(outputBasePackage);
        entityNotFoundException.addClass("EntityNotFoundException", Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration entityNotFoundExceptionClass =
                entityNotFoundException.getClassByName("EntityNotFoundException").get();
        entityNotFoundExceptionClass.addExtendedType(clientExceptionClass.getNameAsString());
        Utils.addGeneratedAnnotation(entityNotFoundException, Utils.getClazz(entityNotFoundException), null, ClientGenerator.class);
        save(outputBasePackage, "EntityNotFoundException", entityNotFoundException);

        final CompilationUnit exceptionMapper = new CompilationUnit(outputBasePackage);
        exceptionMapper.addClass(Configuration.clientName + "ExceptionMapper", Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration exceptionMapperClass =
                exceptionMapper.getClassByName(Configuration.clientName + "ExceptionMapper").get();
        exceptionMapperClass.addImplementedType("ResponseExceptionMapper");
        exceptionMapperClass.getImplementedTypes()
                .get(0)
                .setTypeArguments(new TypeParameter(clientExceptionClass.getNameAsString()));
        exceptionMapper.addImport("org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper");

        exceptionMapperClass.addMarkerAnnotation("Provider");
        exceptionMapper.addImport("javax.ws.rs.ext.Provider");

        exceptionMapper.addImport("javax.ws.rs.core.Response");
        final MethodDeclaration toThrowable = exceptionMapperClass.addMethod("toThrowable", Modifier.PUBLIC);
        toThrowable.addAnnotation(Override.class);
        toThrowable.setType(clientExceptionClass.getNameAsString());
        toThrowable.addParameter(
                new Parameter(EnumSet.of(Modifier.FINAL), new TypeParameter("Response"), new SimpleName("response")));
        final BlockStmt toThrowableBody = new BlockStmt();
        final SwitchStmt switchStmt = new SwitchStmt();
        switchStmt.setSelector(new MethodCallExpr(new NameExpr("response"), "getStatus"));
        switchStmt.getEntries()
                .add(new SwitchEntryStmt(new IntegerLiteralExpr(404),
                        new NodeList<>(new ReturnStmt(
                                new ObjectCreationExpr(null, JavaParser.parseClassOrInterfaceType(
                                        entityNotFoundExceptionClass.getNameAsString()),
                                        new NodeList<>())))));
        switchStmt.getEntries().addLast(new SwitchEntryStmt());
        toThrowableBody.addStatement(switchStmt);
        toThrowableBody.addStatement(new ReturnStmt(new NullLiteralExpr()));
        toThrowable.setBody(toThrowableBody);

        Utils.addGeneratedAnnotation(exceptionMapper, Utils.getClazz(exceptionMapper), null, ClientGenerator.class);
        save(outputBasePackage, Configuration.clientName + "ExceptionMapper", exceptionMapper);
    }

    private static void generateClient(String fileName, String resourceContent, ClassOrInterfaceDeclaration genericResourceClientClass) throws IOException {
        final CompilationUnit resourceClientUnit = JavaParser.parse(resourceContent);
        final ClassOrInterfaceDeclaration resourceClientClass = Utils.getClazz(resourceClientUnit);
        final String clientClassPackage = Configuration.resourcePackage + ".client.interfaces";
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(clientClassPackage);
        final String clientName = fileName.replace(".java", "Client");
        newClassCompilationUnit.addClass(clientName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(clientName).get();
        newClass.setInterface(true);

        createResourceClientReference(clientName, clientClassPackage, genericResourceClientClass, newClass);

        List<AnnotationExpr> classAnnotations = resourceClientClass.getAnnotations()
                .stream()
                .filter(a -> Utils.isJaxRSAnnotation(a))
                .collect(Collectors.toList());

        newClass.setAnnotations(new NodeList<>(classAnnotations));
        addCxfLogInterceptor(newClass);

        resourceClientClass.getMethods().stream().forEach(m -> {
            if (m.getModifiers().contains(Modifier.PRIVATE)) {
                return;
            }

            MethodDeclaration newMethod = m.clone();
            newMethod.removeBody();

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

        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null, ClientGenerator.class);
        Utils.addImports(resourceClientUnit, newClassCompilationUnit);
        Utils.addImports(newClassCompilationUnit, genericResourceClientClass.findCompilationUnit().get());
        Utils.addLicense(resourceClientUnit, newClassCompilationUnit);
        save(clientClassPackage, clientName, newClassCompilationUnit);
    }

    private static void registerFilters(ClassOrInterfaceDeclaration genericClientClass){
        ConstructorDeclaration constructor = genericClientClass.getConstructors().stream().findFirst().get();
        StringBuilder authentication = new StringBuilder();
        authentication.append("if(config.getSignature() != null){");
        authentication.append("builder.register(new " + ImportManager.getImport("SignatureAuthenticator") + "(config));");
        authentication.append("}");
        constructor.getBody().asBlockStmt().addStatement(JavaParser.parseStatement(authentication.toString()));
        authentication = new StringBuilder();
        authentication.append("if(config.getBasic() != null){");
        authentication.append("builder.register(new " + ImportManager.getImport("BasicAuthenticator") + "(config));");
        authentication.append("}");
        constructor.getBody().asBlockStmt().addStatement(JavaParser.parseStatement(authentication.toString()));

        String logClientResponseFilter = "builder.register(new " + ImportManager.getImport("LogClientResponseFilter") + "(config));";
        constructor.getBody().asBlockStmt().addStatement(logClientResponseFilter);

        String logClientRequestFilter = "builder.register(new " + ImportManager.getImport("LogClientRequestFilter") + "(config));";
        constructor.getBody().asBlockStmt().addStatement(logClientRequestFilter);
    }

    private static void createResourceClientReference(String clientName, String pkg, ClassOrInterfaceDeclaration genericClientClass,
                                                      ClassOrInterfaceDeclaration resourceClientClass) {

        VariableDeclarator var = new VariableDeclarator(new TypeParameter(resourceClientClass.getNameAsString()),
                WordUtils.uncapitalize(resourceClientClass.getNameAsString()));
        FieldDeclaration reference = new FieldDeclaration(EnumSet.of(Modifier.PRIVATE), var);
        genericClientClass.getMembers().add(0, reference);
        genericClientClass.findCompilationUnit().get().addImport(pkg + "." + clientName);
        final String replaceValue = Configuration.resourceSuffix == null ?
                "Client" : Configuration.resourceSuffix + "Client";
        String name = clientName.replace(replaceValue, "");
        MethodDeclaration referenceMethod = new MethodDeclaration();
        referenceMethod.setModifiers(EnumSet.of(Modifier.PUBLIC));
        referenceMethod.setName(name.toLowerCase());
        referenceMethod.setType(resourceClientClass.getNameAsString());
        referenceMethod.setBody(JavaParser.parseBlock("{ return this." + WordUtils.uncapitalize(resourceClientClass.getNameAsString()) + "; }"));
        genericClientClass.addMember(referenceMethod);

        ConstructorDeclaration constructor = genericClientClass.getConstructors().stream().findFirst().get();

        StringBuilder builder = new StringBuilder();
        builder.append(WordUtils.uncapitalize(resourceClientClass.getNameAsString()) + " = builder.build(" + resourceClientClass.getNameAsString() + ".class);");
        constructor.getBody().asBlockStmt().addStatement(JavaParser.parseStatement(builder.toString()));

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
