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
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private Utils() {
    }

    public static ClassOrInterfaceDeclaration getClazz(final CompilationUnit unit) {
        return (ClassOrInterfaceDeclaration) unit.getTypes().stream()
                .filter(typeDeclaration -> typeDeclaration instanceof ClassOrInterfaceDeclaration)
                .findFirst().orElse(null);
    }

    // TODO move to an annotations static class
    public static Map<String, MemberValuePair> pairs(final NormalAnnotationExpr annotation) {
        final Map<String, MemberValuePair> map = new LinkedHashMap<>();
        for (final MemberValuePair pair : annotation.getPairs()) {
            map.put(pair.getNameAsString(), pair);
        }
        return map;
    }

    // TODO move static methods to a Methods static class
    public static boolean isResourceMethod(final MethodDeclaration method) {
        return httpMethod(method) != null;
    }

    public static boolean isGET(final MethodDeclaration method) {
        return "GET".equals(httpMethod(method));
    }


    public static boolean isPUT(final MethodDeclaration method) {
        return "PUT".equals(httpMethod(method));
    }


    public static boolean isPOST(final MethodDeclaration method) {
        return "POST".equals(httpMethod(method));
    }


    public static boolean isDELETE(final MethodDeclaration method) {
        return "DELETE".equals(httpMethod(method));
    }


    public static boolean isHEAD(final MethodDeclaration method) {
        return "HEAD".equals(httpMethod(method));
    }


    public static boolean isOPTIONS(final MethodDeclaration method) {
        return "OPTIONS".equals(httpMethod(method));
    }


    public static boolean isPATCH(final MethodDeclaration method) {
        return "PATCH".equals(httpMethod(method));
    }

    public static String httpMethod(final MethodDeclaration method) {
        final List<String> methods = Arrays.asList("POST", "PUT", "GET", "DELETE", "OPTIONS", "HEAD", "PATCH");
        for (final String m : methods) {
            if (method.getAnnotationByName(m).isPresent()) return m;
        }
        return null;
    }

    public static NormalAnnotationExpr getOperation(final MethodDeclaration method) {
        return getAnnotation(method, "Operation");
    }

    public static NormalAnnotationExpr getAnnotation(final MethodDeclaration method, final String annotationName) {
        final Optional<AnnotationExpr> annotationByName = method.getAnnotationByName(annotationName);

        if (annotationByName.isPresent() && annotationByName.get().isSingleMemberAnnotationExpr()) {
            SingleMemberAnnotationExpr singleAnnotation = annotationByName.get().asSingleMemberAnnotationExpr();
            NodeList nodeList = new NodeList<MemberValuePair>();
            nodeList.add(new MemberValuePair("value", singleAnnotation.getMemberValue()));
            return new NormalAnnotationExpr(annotationByName.get().getName(), nodeList);
        }

        return (NormalAnnotationExpr) annotationByName.orElse(null);
    }

    public static <N extends Node> void sortNodes(final NodeList<N> ns, final Function<N, String> classifier, final String... patterns) {
        ns.sort(Comparator.comparing(annotation -> sort(classifier.apply(annotation),
                patterns
        )));
    }

    private static int sort(final String name, final String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (name.matches(patterns[i])) return i;
        }
        return patterns.length + 1;
    }

    public static void addApiResponse(final MethodDeclaration method, final int code, final String message, Header header) {
        // Ensure the annotations are imported
        final CompilationUnit unit = method.findCompilationUnit().get();
        unit.addImport("io.swagger.v3.oas.annotations.responses.ApiResponses");
        unit.addImport("io.swagger.v3.oas.annotations.responses.ApiResponse");

        final NormalAnnotationExpr apiResponses = getAnnotation(method, "ApiResponses");

        String headerString = "";
        if (header != null) {
            unit.addImport("io.swagger.v3.oas.annotations.headers.Header");
            headerString = ", headers = {@Header(name = \"" + header.getName() + "\", description = \"" + header.getDescription() + "\")}";
        }

        final String response = "@ApiResponse(responseCode = \"" + code + "\", description = \"" + message + "\"" + headerString + ")";

        if (apiResponses == null) {
            method.addAnnotation(JavaParser.parseAnnotation("@ApiResponses( value = {" + response + "})"));
            return;
        }

        final MemberValuePair value = pairs(apiResponses).get("value");
        if (value == null) {
            apiResponses.addPair("value", "{" + response + "}");
            return;
        }

        final NodeList<NormalAnnotationExpr> annotations = arrayValue(value.getValue());

        final boolean has409 = annotations.stream().anyMatch(has("responseCode", "\"" + code + "\""));

        if (!has409) {
            annotations.add((NormalAnnotationExpr) JavaParser.parseAnnotation(response));
            final ArrayInitializerExpr value1 = asArray(annotations);
            value.setValue(value1);
        }
    }

    private static ArrayInitializerExpr asArray(NodeList<? extends Expression> annotations) {
        final NodeList<Expression> expressions = new NodeList<>();
        expressions.addAll(annotations);
        return new ArrayInitializerExpr(expressions);
    }

    public static Predicate<NormalAnnotationExpr> has(final String member, final String value) {
        return normalAnnotationExpr -> has(normalAnnotationExpr, member, value);
    }

    public static boolean has(NormalAnnotationExpr normalAnnotationExpr, final String member, final String value) {
        if (normalAnnotationExpr == null) return false;
        final Map<String, MemberValuePair> pairs = Utils.pairs(normalAnnotationExpr);
        final MemberValuePair code = pairs.get(member);
        return code != null && value.equals(code.getValue().toString());
    }

    public static NodeList<NormalAnnotationExpr> arrayValue(Expression expression) {
        final NodeList<NormalAnnotationExpr> annotations = new NodeList<>();
        if (expression instanceof ArrayInitializerExpr) {
            final ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) expression;
            for (final Expression exp2 : arrayInitializerExpr.getValues()) {
                annotations.add((NormalAnnotationExpr) exp2);
            }
        } else if (expression instanceof NormalAnnotationExpr) {
            annotations.add((NormalAnnotationExpr) expression);
        } else {
            throw new IllegalStateException("Unsupported Expression " + expression.getClass().getName());
        }
        return annotations;
    }

    public static void removeApiResponse(final MethodDeclaration method, final int code) {
        final NormalAnnotationExpr apiResponses = getAnnotation(method, "ApiResponses");
        if (apiResponses == null) return;

        final MemberValuePair value = pairs(apiResponses).get("value");
        if (value == null) return;

        final NodeList<NormalAnnotationExpr> annotations = arrayValue(value.getValue());

        final NormalAnnotationExpr match = annotations.stream()
                .filter(has("responseCode", "\"" + code + "\""))
                .findFirst().orElse(null);

        // nothing to remove
        if (match == null) return;

        // ok, we do have an ApiResponse to remove, let's get started

        // There's more than one ApiResponse, just return the one we don't want
        if (annotations.size() > 1) {
            value.remove(match);
            return;
        }

        // The only ApiResponse is the one we're removing, so remove the 'value' entirely
        apiResponses.remove(value);

        // If the wrapper ApiResponses doesn't have any fields anymore, remove it too
        if (apiResponses.getPairs().size() == 0) {
            method.getAnnotations().remove(apiResponses);
        }
    }

    public static boolean hasPathParameter(final MethodDeclaration method) {
        final NormalAnnotationExpr path = getAnnotation(method, "Path");
        if (path == null) return false;

        final MemberValuePair value = pairs(path).get("value");
        return value != null && value.toString().matches(".*\\{[^}]+\\}.*");
    }

    public static boolean isMethodReadAll(final MethodDeclaration method) {
        AnnotationExpr pathAnnotation = Utils.getAnnotation(method, "Path");
        boolean hasPathAnnotation = pathAnnotation != null;
        return Utils.isGET(method) && !hasPathAnnotation;
    }

    public static boolean isMethodRead(final MethodDeclaration method, final String idField) {
        NormalAnnotationExpr pathAnnotation = Utils.getAnnotation(method, "Path");
        boolean hasPathAnnotation = pathAnnotation != null;
        if (hasPathAnnotation) {
            Optional<MemberValuePair> valueId = pathAnnotation.getPairs()
                    .stream()
                    .filter(i -> i.getValue().toString().equals("\"{" + idField + "}\"") || i.getValue().toString().equals("\"{id}\""))
                    .findFirst();
            boolean hasValueId = valueId.isPresent();
            boolean isMethodFind = Utils.isGET(method) && hasPathAnnotation && hasValueId;
            if (isMethodFind) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMethodCreate(MethodDeclaration method) {
        AnnotationExpr pathAnnotation = Utils.getAnnotation(method, "Path");
        boolean hasPathAnnotation = pathAnnotation != null;
        return Utils.isPOST(method) && !hasPathAnnotation && !isBulkMethod(method);
    }

    public static boolean isMethodUpdate(MethodDeclaration method, String idField) {
        NormalAnnotationExpr pathAnnotation = Utils.getAnnotation(method, "Path");
        boolean hasPathAnnotation = pathAnnotation != null;
        if (hasPathAnnotation) {
            Optional<MemberValuePair> valueId = pathAnnotation.getPairs()
                    .stream()
                    .filter(i -> i.getValue().toString().equals("\"{" + idField + "}\"") || i.getValue().toString().equals("\"{id}\""))
                    .findFirst();
            boolean hasValueId = valueId.isPresent();
            boolean isMethodUpdate = Utils.isPUT(method) && hasPathAnnotation && hasValueId;
            if (isMethodUpdate) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMethodDelete(MethodDeclaration method, String idField) {
        NormalAnnotationExpr pathAnnotation = Utils.getAnnotation(method, "Path");
        boolean hasPathAnnotation = pathAnnotation != null;
        if (hasPathAnnotation) {
            Optional<MemberValuePair> valueId = pathAnnotation.getPairs()
                    .stream()
                    .filter(i -> i.getValue().toString().equals("\"{" + idField + "}\"") || i.getValue().toString().equals("\"{id}\""))
                    .findFirst();
            boolean hasValueId = valueId.isPresent();
            boolean isMethodDelete = Utils.isDELETE(method) && hasPathAnnotation && hasValueId;
            if (isMethodDelete) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMethodBulkCreate(MethodDeclaration method) {
        return Utils.isPOST(method) && Utils.isBulkMethod(method);
    }

    public static boolean isMethodBulkDelete(MethodDeclaration method) {
        return Utils.isDELETE(method) && Utils.isBulkMethod(method);
    }

    public static boolean isMethodBulkUpdate(MethodDeclaration method) {
        return Utils.isPUT(method) && Utils.isBulkMethod(method);
    }

    public static boolean isBulkMethod(MethodDeclaration m) {
        return m.getName().toString().startsWith("bulk");
    }

    public static boolean hasMethodInClass(ClassOrInterfaceDeclaration clazz, Predicate<MethodDeclaration> methodPredicate) {
        return clazz.getMethods().stream().anyMatch(methodPredicate);
    }

    public static ClassOrInterfaceDeclaration getExtendedClass(CompilationUnit classUnit, String extendedClassName) throws IOException {
        Optional<ImportDeclaration> classImport = classUnit.getImports().stream().filter(i -> i.getNameAsString().endsWith("." + extendedClassName)).findFirst();
        String extendedClassPath;
        if (classImport.isPresent()) {
            extendedClassPath = Configuration.MODEL_SOURCES + "/" + classImport.get().getNameAsString().replaceAll("\\.", "/");
            extendedClassPath += ".java";

        } else {
            extendedClassPath = Configuration.MODEL_SOURCES + "/" + classUnit.getPackageDeclaration().get().getNameAsString().replaceAll("\\.", "/");
            extendedClassPath += "/" + extendedClassName + ".java";
        }
        return getClazz(extendedClassPath);
    }

    public static ClassOrInterfaceDeclaration getClazz(String filePath) throws IOException {
        final String source = IO.slurp(new File(filePath));
        final CompilationUnit classUnit = JavaParser.parse(source);
        return getClazz(classUnit);
    }

    public static String getExample(FieldDeclaration field) {
        Optional<AnnotationExpr> fieldSchema = field.getAnnotationByName("Schema");
        if (!fieldSchema.isPresent()) {
            return null;
        }

        Map<String, MemberValuePair> pairs = Utils.pairs(fieldSchema.get().asNormalAnnotationExpr());
        MemberValuePair example = pairs.get("example");
        if (example == null) {
            return null;
        }
        return example.getValue().asStringLiteralExpr().getValue();
    }

    public static boolean isId(FieldDeclaration field) {
        Optional<AnnotationExpr> modelAnnotation = field.getAnnotationByName("Model");
        if (!modelAnnotation.isPresent()) {
            return false;
        }

        Map<String, MemberValuePair> pairs = Utils.pairs(modelAnnotation.get().asNormalAnnotationExpr());
        MemberValuePair id = pairs.get("id");
        return id != null && id.getValue().asBooleanLiteralExpr().getValue();
    }

    public static String getRootName(ClassOrInterfaceDeclaration rootClass) {
        return rootClass.getName().toString().replace(Configuration.MODEL_SUFFIX, "");
    }

    public static boolean isRootResource(final String rootClassName, final String resourceName) {
        final String expectedSingularResource = rootClassName + Configuration.RESOURCE_SUFFIX;
        return expectedSingularResource.equals(resourceName);
    }

    public static List<String> getClassOperations(ClassOrInterfaceDeclaration rootClass) {
        List<String> classOperations = null;
        Optional<AnnotationExpr> classModel = rootClass.getAnnotationByName("Model");
        if (classModel.isPresent()) {
            AnnotationExpr modelAnnotation = classModel.get();
            if (modelAnnotation.isNormalAnnotationExpr()) {
                NodeList<MemberValuePair> pairs = modelAnnotation.asNormalAnnotationExpr().getPairs();
                Optional<MemberValuePair> pair = pairs.stream().filter(p -> p.getNameAsString().equals("operation")).findFirst();
                if (pair.isPresent()) {
                    Expression value = pair.get().getValue();
                    if (value.isArrayInitializerExpr()) {
                        ArrayInitializerExpr values = value.asArrayInitializerExpr();
                        classOperations = values.getValues().stream()
                                .map(Node::toString)
                                .collect(Collectors.toList());
                    } else {
                        FieldAccessExpr field = value.asFieldAccessExpr();
                        classOperations = Collections.singletonList(field.toString());
                    }
                }
            }

        }
        return classOperations;
    }

    public static List<File> getResources(final String modelClassName) {
        final File apiSourcesDir = new File(Configuration.RESOURCE_SOURCES);
        final File sourceRootDir = new File(Configuration.GENERATED_SOURCES);
        List<File> src = Files.collect(apiSourcesDir, "(.*)" + Configuration.RESOURCE_SUFFIX + "\\.java")
                .stream()
                .filter(f -> f.getName().equals(modelClassName + Configuration.RESOURCE_SUFFIX + ".java") ||
                        f.getName().equals(Utils.toPlural(modelClassName) + Configuration.RESOURCE_SUFFIX + ".java"))
                .collect(Collectors.toList());
        List<File> generatedSources = Files.collect(sourceRootDir, "(.*)" + Configuration.RESOURCE_SUFFIX + "\\.java")
                .stream()
                .filter(f -> f.getName().equals(modelClassName + Configuration.RESOURCE_SUFFIX + ".java") ||
                        f.getName().equals(Utils.toPlural(modelClassName) + Configuration.RESOURCE_SUFFIX + ".java"))
                .collect(Collectors.toList());

        return Stream.concat(src.stream(), generatedSources.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public static Map<String, String> getResources() {
        final File srcFolder = new File(Configuration.RESOURCE_SOURCES);
        final File generatedFolder = new File(Configuration.GENERATED_SOURCES);
        List<File> src = Files.collect(srcFolder, "(.*)\\.java");
        List<File> generatedSources = Files.collect(generatedFolder, "(.*)\\.java");

        Map<String, File> collect = Stream.concat(src.stream(), generatedSources.stream())
                .distinct()
                .collect(Collectors.toMap(File::getName, f -> f));

        Map<String, String> resourcesMap = new HashMap<>();

        Iterator<Map.Entry<String, File>> it = collect.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, File> next = it.next();
            String content = null;
            try {
                content = IO.slurp(next.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (content != null && content.contains("javax.ws.rs") && content.contains("@Path")) {
                resourcesMap.put(next.getKey(), content);
            }
        }
        return resourcesMap;
    }

    public static List<File> getModel() {
        if (Configuration.MODEL_PACKAGE != null) {
            final File apiSourcesDir =
                    new File(Configuration.getModelPath());
            return Files.collect(apiSourcesDir, "(.*)" + Configuration.MODEL_SUFFIX + "\\.java");
        } else {
            return Collections.emptyList();
        }
    }

    public static List<File> getClient() {
        final File srcFolder = new File(Configuration.CLIENT_SOURCES);
        return Files.collect(srcFolder, "(.*)Client" + "\\.java");
    }

    public static String getIdName(ClassOrInterfaceDeclaration rootClass) {
        Optional<FieldDeclaration> idField = getId(rootClass);

        if (idField.isPresent()) {
            return idField.get().getVariables().stream().findFirst().get().getName().asString();
        }

        return "id";
    }

    public static Optional<FieldDeclaration> getId(ClassOrInterfaceDeclaration rootClass) {
        return rootClass.getFields().stream().filter(f -> {
            Optional<AnnotationExpr> modelOptional = f.getAnnotationByName("Model");
            if (modelOptional.isPresent()) {
                NormalAnnotationExpr modelAnnotation = modelOptional.get().asNormalAnnotationExpr();
                NodeList<MemberValuePair> pairs = modelAnnotation.getPairs();
                Optional<MemberValuePair> pair = pairs.stream().filter(p -> p.getName().asString().equals("id")).findFirst();
                if (pair.isPresent()) {
                    BooleanLiteralExpr booleanLiteralExpr = pair.get().getValue().asBooleanLiteralExpr();
                    return booleanLiteralExpr.getValue();
                }
            }
            return false;
        }).findFirst();
    }

    public static boolean isOperationPresent(final FieldDeclaration f, final String operation) {
        Optional<MemberValuePair> valuePair = f.getAnnotations()
                .stream()
                .filter(a -> a.getName().toString().equals("Model"))
                .map(a -> a.asNormalAnnotationExpr().getPairs())
                .flatMap(Collection::stream)
                .filter(a -> a.getName().toString().equals("operation"))
                .findFirst();

        if (valuePair.isPresent()) {
            if (valuePair.get().getValue().isFieldAccessExpr()) {
                return valuePair.get().getValue().toString().contains(operation);
            } else {
                NodeList<Expression> values = valuePair.get().getValue().asArrayInitializerExpr().getValues();
                return values.stream().map(Node::toString).anyMatch(v -> v.equals(operation));
            }

        }
        return false;
    }

    public static boolean hasOperations(final FieldDeclaration f) {
        return f.getAnnotations()
                .stream()
                .filter(a -> a.getName().toString().equals("Model"))
                .map(a -> a.asNormalAnnotationExpr().getPairs())
                .flatMap(Collection::stream)
                .anyMatch(a -> a.getName().toString().equals("operation"));
    }

    public static String toPlural(String singular) {
        if (singular == null) {
            return null;
        }

        if (!singular.endsWith("y")) {
            singular += "s";
            return singular;
        }

        String beforeTwoChars = singular.substring(0, singular.length() - 2);
        String twoLastChars = singular.substring(singular.length() - 2, singular.length());
        String beforeY = twoLastChars.substring(0, 1);
        String plural = "";
        if (beforeY.equals("a") || beforeY.equals("e") || beforeY.equals("i") || beforeY.equals("o") || beforeY.equals("u")) {
            plural += beforeTwoChars + twoLastChars + "s";
        } else {
            plural += beforeTwoChars + beforeY + "ies";
        }
        return plural;
    }

    public static void addGeneratedAnnotation(CompilationUnit unit, ClassOrInterfaceDeclaration clazz, MethodDeclaration method, final Class<?> generator) {
        final Name name = new Name("Generated");
        final Expression memberValue = new StringLiteralExpr(generator.getName());
        final SingleMemberAnnotationExpr expr = new SingleMemberAnnotationExpr(name, memberValue);

        unit.addImport(ImportManager.getImport("Generated"));
        if (clazz != null) {
            clazz.addAnnotation(expr);
        } else if (method != null) {
            method.addAnnotation(expr);
        }
    }

    public static String formatCamelCaseTo(String value, String separator) {
        final Matcher matcher = Pattern.compile("(?<=[a-z])[A-Z]").matcher(value);
        final StringBuffer modified = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(modified, separator + matcher.group());
        }
        matcher.appendTail(modified);
        return modified.toString().toLowerCase();
    }

    public static void addImports(CompilationUnit oldClassUnit, CompilationUnit newClassUnit) {
        oldClassUnit.getImports().forEach(newClassUnit::addImport);
    }

    public static void save(String fileName, String pkg, String content) throws IOException {
        Path path = Paths.get(Configuration.GENERATED_SOURCES + File.separator + transformPackageToPath(pkg));

        content = Stream.of(content)
                .map(RemoveDuplicateImports::apply)
                .map(Reformat::apply)
                .findFirst().get();

        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectories(path);
        }
        File newFile = new File(path.toAbsolutePath().toString(), fileName);

        IO.copy(IO.read(content), newFile);
    }

    public static String transformPackageToPath(String pkg) {
        return pkg.replaceAll("\\.", File.separator);
    }

    public static void addLicense(CompilationUnit rootUnit, CompilationUnit newClassUnit) {
        Optional<Comment> license = rootUnit.getComment();
        license.ifPresent(newClassUnit::setComment);
    }

    public static boolean isJaxRSAnnotation(AnnotationExpr a) {
        String importValue = ImportManager.getImport(a.getNameAsString());
        return importValue != null && importValue.contains("javax.ws.rs.");
    }

    public static String getResponseImplementation(MethodDeclaration m) {
        final NormalAnnotationExpr apiResponses = Utils.getAnnotation(m, "ApiResponses");
        final MemberValuePair value = pairs(apiResponses).get("value");
        final NodeList<NormalAnnotationExpr> annotations = Utils.arrayValue(value.getValue());
        Optional<NormalAnnotationExpr> responseOptional = annotations.stream()
                .filter(a -> Utils.has(a, "responseCode", "\"200\"") || Utils.has(a, "responseCode", "\"201\""))
                .findFirst();
        if (responseOptional.isPresent()) {
            NormalAnnotationExpr response = responseOptional.get();
            Map<String, MemberValuePair> responsePairs = pairs(response);
            Expression content = responsePairs.get("content").getValue();
            Map<String, MemberValuePair> contentPairs = Utils.pairs(content.asNormalAnnotationExpr());
            NormalAnnotationExpr schema = contentPairs.get("schema").getValue().asNormalAnnotationExpr();
            Map<String, MemberValuePair> schemaPairs = Utils.pairs(schema);
            Expression implementation = schemaPairs.get("implementation").getValue();
            return implementation.asClassExpr().getTypeAsString();
        }
        return null;
    }

    public static String getFullQualifiedName(CompilationUnit unit) {
        ClassOrInterfaceDeclaration clazz = Utils.getClazz(unit);
        return unit.getPackageDeclaration().get().getNameAsString() + "." + clazz.getNameAsString();

    }

    public static boolean isWrapperOrPrimitiveOrDate(FieldDeclaration f) {
        VariableDeclarator var = f.getVariables().stream().findFirst().get();

        boolean isWrapper = false;

        String type = var.getTypeAsString();
        if (type.contains("<")) {
            type = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
        }

        try {
            type = (type.startsWith("java.lang")) ? type : "java.lang." + type;
            CustomTypeSolver.get().solveType(type);
            isWrapper = true;
        } catch (RuntimeException e) {
        }

        boolean isDate = false;
        try {
            CustomTypeSolver.get().solveType(type);
            if (type.startsWith("java.util")) {
                isDate = true;
            }
        } catch (RuntimeException e) {
        }

        return f.getCommonType().isPrimitiveType() || isWrapper || isDate;
    }

    public static boolean isCollection(final ResolvedType type) {
        if (type.isReferenceType()) {
            final ResolvedReferenceTypeDeclaration typeDeclaration = type.asReferenceType().getTypeDeclaration();

            if (typeDeclaration.isAssignableBy(CustomTypeSolver.get().solveType("java.util.Collection"))) {
                return true;
            }
        }

        return false;
    }
}
