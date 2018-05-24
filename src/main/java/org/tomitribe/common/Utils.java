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
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    private static HashMap<String, String> importMap = new HashMap<>();

    static {
        importMap.put("Date", "java.util.Date");
        importMap.put("Builder", "lombok.Builder");
        importMap.put("Collection", "java.util.Collection");
        importMap.put("Operation", "io.swagger.v3.oas.annotations.Operation");
        importMap.put("Generated", "javax.annotation.Generated");
        importMap.put("Path", "javax.ws.rs.Path");
        importMap.put("Response", "javax.ws.rs.core.Response");
        importMap.put("POST", "javax.ws.rs.POST");
        importMap.put("GET", "javax.ws.rs.GET");
        importMap.put("PUT", "javax.ws.rs.PUT");
        importMap.put("DELETE", "javax.ws.rs.DELETE");
        importMap.put("EqualsAndHashCode", "lombok.EqualsAndHashCode");
        importMap.put("AllArgsConstructor", "lombok.AllArgsConstructor");
        importMap.put("Value", "lombok.Value");
        importMap.put("Schema", "io.swagger.v3.oas.annotations.media.Schema");
        importMap.put("List", "java.util.List");
        importMap.put("Data", "lombok.Data");
        importMap.put("Produces", "javax.ws.rs.Produces");
        importMap.put("Consumes", "javax.ws.rs.Consumes");
        importMap.put("MediaType", "javax.ws.rs.core.MediaType");
    }

    private Utils() {
    }

    public static String getImport(String className) {
        return importMap.get(className);
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

    public static NormalAnnotationExpr getApi(final MethodDeclaration method) {
        return getAnnotation(method, "Api");
    }

    public static NormalAnnotationExpr getApiParam(final MethodDeclaration method) {
        return getAnnotation(method, "ApiParam");
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

    public static Function<MethodDeclaration, NormalAnnotationExpr> getAnnotation(final String annotationName) {
        return method -> getAnnotation(method, annotationName);
    }

    // TODO move to a Nodes static class
    public static <N extends Node> void sortNodes(final Supplier<NodeList<N>> listSupplier, final Function<N, String> classifier, final String... patterns) {
        sortNodes(listSupplier.get(), classifier, patterns);
    }

    public static <N extends Node> void sortNodes(final NodeList<N> ns, final Function<N, String> classifier, final String... patterns) {
        ns.sort(Comparator.comparing(annotation -> sort(classifier.apply(annotation),
                patterns
        )));
    }

    public static int sort(final String name, final String... patterns) {
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

        final String RESPONSE = "@ApiResponse(responseCode = \"" + code + "\", description = \"" + message + "\"" + headerString + ")";

        if (apiResponses == null) {
            method.addAnnotation(JavaParser.parseAnnotation("@ApiResponses( value = {" + RESPONSE + "})"));
            return;
        }

        final MemberValuePair value = pairs(apiResponses).get("value");
        if (value == null) {
            apiResponses.addPair("value", "{" + RESPONSE + "}");
            return;
        }

        final NodeList<NormalAnnotationExpr> annotations = arrayValue(value.getValue());

        final boolean has409 = annotations.stream()
                .filter(has("responseCode", "\"" + code + "\""))
                .findFirst()
                .isPresent();

        if (!has409) {
            annotations.add((NormalAnnotationExpr) JavaParser.parseAnnotation(RESPONSE));
            final ArrayInitializerExpr value1 = asArray(annotations);
            value.setValue(value1);
        }
    }

    private static CompilationUnit getCompilationUnit(final Node node) {
        if (node instanceof CompilationUnit) {
            return (CompilationUnit) node;
        }
        return getCompilationUnit(node.findRootNode());
    }

    public static ArrayInitializerExpr asArray(NodeList<? extends Expression> annotations) {
        final NodeList<Expression> expressions = new NodeList<>();
        for (final Expression annotation : annotations) {
            expressions.add(annotation);
        }
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

    public static Predicate<? super MethodDeclaration> hasOperation(final String code, final String value) {
        return methodDeclaration -> {
            final NormalAnnotationExpr operation = getAnnotation(methodDeclaration, "Operation");
            return has(operation, code, value);
        };
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

    public static boolean hasAnnotation(final FieldDeclaration field, final String annotation) {
        return field.isAnnotationPresent(annotation);
    }

    public static boolean isMethodFindAll(final MethodDeclaration method) {
        AnnotationExpr pathAnnotation = Utils.getAnnotation(method, "Path");
        boolean hasPathAnnotation = pathAnnotation != null;
        boolean isMethodFindAll = Utils.isGET(method) && !hasPathAnnotation;
        if (isMethodFindAll) {
            return true;
        }
        return false;
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
        boolean isMethodCreate = Utils.isPOST(method) && !hasPathAnnotation && !isBulkMethod(method);
        if (isMethodCreate) {
            return true;
        }
        return false;
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
        boolean isMethodBulkCreate = Utils.isPOST(method) && Utils.isBulkMethod(method);
        if (isMethodBulkCreate) {
            return true;
        }
        return false;
    }

    public static boolean isMethodBulkDelete(MethodDeclaration method) {
        boolean isMethodBulkDelete = Utils.isDELETE(method) && Utils.isBulkMethod(method);
        if (isMethodBulkDelete) {
            return true;
        }
        return false;
    }

    public static boolean isMethodBulkUpdate(MethodDeclaration method) {
        boolean isMethodBulkUpdate = Utils.isPUT(method) && Utils.isBulkMethod(method);
        if (isMethodBulkUpdate) {
            return true;
        }
        return false;
    }

    public static boolean isBulkMethod(MethodDeclaration m) {
        return m.getName().toString().startsWith("bulk");
    }

    public static boolean hasMethodInClass(ClassOrInterfaceDeclaration clazz, Predicate<MethodDeclaration> methodPredicate) {
        return clazz.getMethods().stream().filter(methodPredicate).findFirst().isPresent();
    }

    public static ClassOrInterfaceDeclaration getExtendedClass(CompilationUnit classUnit, String extendedClassName) throws IOException {
        Optional<ImportDeclaration> classImport = classUnit.getImports().stream().filter(i -> i.getNameAsString().endsWith("." + extendedClassName)).findFirst();
        String extendedClassPath;
        if (classImport.isPresent()) {
            extendedClassPath = Configuration.SOURCES + "/" + classImport.get().getNameAsString().replaceAll("\\.", "/");
            extendedClassPath += ".java";

        } else {
            extendedClassPath = Configuration.SOURCES + "/" + classUnit.getPackageDeclaration().get().getNameAsString().replaceAll("\\.", "/");
            extendedClassPath += "/" + extendedClassName + ".java";
        }
        return getClazz(extendedClassPath);
    }

    public static ClassOrInterfaceDeclaration getClazz(String filePath) throws IOException {
        final String source = IO.slurp(new File(filePath));
        final CompilationUnit classUnit = JavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = getClazz(classUnit);
        return clazz;
    }

    public static List<NormalAnnotationExpr> getExtensions(NormalAnnotationExpr schema) {
        Map<String, MemberValuePair> pairs = Utils.pairs(schema);

        MemberValuePair extensions = pairs.get("extensions");

        if (extensions != null) {
            ArrayInitializerExpr arrayInitializerExpr = extensions.getValue().asArrayInitializerExpr();
            NodeList<Expression> extValues = arrayInitializerExpr.getValues();
            NormalAnnotationExpr extension = extValues.stream()
                    .filter(e -> e.asNormalAnnotationExpr().toString().contains("generator"))
                    .findFirst().get().asNormalAnnotationExpr();
            MemberValuePair properties = Utils.pairs(extension).get("properties");
            ArrayInitializerExpr propValues = properties.getValue().asArrayInitializerExpr();
            NodeList<Expression> values = propValues.getValues();
            return values.stream()
                    .map(v -> v.asNormalAnnotationExpr())
                    .collect(Collectors.toList());
        }
        return null;
    }

    public static String getExtensionValueByName(final NormalAnnotationExpr schema, final String name) {
        List<NormalAnnotationExpr> extensions = getExtensions(schema);
        if (extensions != null) {
            for (NormalAnnotationExpr e : extensions) {
                Map<String, MemberValuePair> pairs = Utils.pairs(e);
                MemberValuePair valuePair = pairs.get("name");
                if (valuePair != null && valuePair.getValue().toString().equals("\"" + name + "\"")) {
                    return pairs.get("value").getValue().toString().replaceAll("\"", "");
                }
            }
        }
        return null;
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
        if (id == null) {
            return false;
        }
        return id.getValue().asBooleanLiteralExpr().getValue();
    }

    public static String getRootName(ClassOrInterfaceDeclaration rootClass) {
        return rootClass.getName().toString().replace("Model", "");
    }

    public static boolean isRootResource(final String rootClassName, final String resourceName) {
        final String expectedSingularResource = rootClassName + "Resource";
        if (expectedSingularResource.equals(resourceName)) {
            return true;
        }
        return false;
    }

    public static List<String> getClassOperations(ClassOrInterfaceDeclaration rootClass) {
        List<String> classOperations = null;
        Optional<AnnotationExpr> classModel = rootClass.getAnnotationByName("Model");
        if (classModel.isPresent()) {
            NormalAnnotationExpr modelAnnotation = classModel.get().asNormalAnnotationExpr();
            NodeList<MemberValuePair> pairs = modelAnnotation.getPairs();
            Optional<MemberValuePair> pair = pairs.stream().filter(p -> p.getNameAsString().equals("operation")).findFirst();
            if (pair.isPresent()) {
                Expression value = pair.get().getValue();
                if (value.isArrayInitializerExpr()) {
                    ArrayInitializerExpr values = value.asArrayInitializerExpr();
                    classOperations = values.getValues().stream()
                            .map(a -> a.toString())
                            .collect(Collectors.toList());
                } else {
                    FieldAccessExpr field = value.asFieldAccessExpr();
                    classOperations = Arrays.asList(field.toString());
                }

            }
        }
        return classOperations;
    }

    public static List<File> getResources(final String modelClassName) {
        final File apiSourcesDir = new File(Configuration.SOURCES);
        final File sourceRootDir = new File(Configuration.GENERATED_SOURCES);
        List<File> src = Files.collect(apiSourcesDir, "(.*)Resource\\.java")
                .stream()
                .filter(f -> f.getName().contains(modelClassName + "Resource") ||
                        f.getName().contains(Utils.toPlural(modelClassName) + "Resource"))
                .collect(Collectors.toList());
        List<File> generatedSources = Files.collect(sourceRootDir, "(.*)Resource\\.java")
                .stream()
                .filter(f -> f.getName().contains(modelClassName + "Resource") ||
                        f.getName().contains(Utils.toPlural(modelClassName) + "Resource"))
                .collect(Collectors.toList());

        List<File> resources = Stream.concat(src.stream(), generatedSources.stream())
                .collect(Collectors.toList());

        return resources;
    }

    public static List<File> getModel() {
        if (Configuration.MODEL_PACKAGE != null) {
            final File apiSourcesDir =
                    new File(Configuration.SOURCES + "/" + Configuration.MODEL_PACKAGE.replaceAll("\\.", "/"));
            return Files.collect(apiSourcesDir, "(.*)Model\\.java");
        } else {
            return Collections.emptyList();
        }
    }

    public static String getId(ClassOrInterfaceDeclaration rootClass) {
        Optional<FieldDeclaration> idField = rootClass.getFields().stream().filter(f -> {
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

        if (idField.isPresent()) {
            return idField.get().getVariables().get(0).getName().asString();
        }

        return "id";
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
                return values.stream().map(v -> v.toString()).filter(v -> v.equals(operation)).findFirst().isPresent();
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
                .filter(a -> a.getName().toString().equals("operation")).findFirst().isPresent();
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

    public static void addGeneratedAnnotation(CompilationUnit unit, ClassOrInterfaceDeclaration clazz, MethodDeclaration method) {
        NormalAnnotationExpr generatedAnnotation = new NormalAnnotationExpr();
        generatedAnnotation.setName("Generated");
        generatedAnnotation.addPair("value", "\"" + Configuration.MAIN_CLASS + "\"");
        unit.addImport(Utils.getImport("Generated"));
        if (clazz != null) {
            clazz.addAnnotation(generatedAnnotation);
        } else if (method != null) {
            method.addAnnotation(generatedAnnotation);
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
        oldClassUnit.getImports().stream().forEach(i -> {
            newClassUnit.addImport(i);
        });
    }

    public static void save(String fileName, String pkg, String content) throws IOException {
        Path path = Paths.get(Configuration.GENERATED_SOURCES + "/" + transformPackageToPath(pkg));

        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectories(path);
        }
        File newFile = new File(path.toAbsolutePath().toString(), fileName);
        IO.copy(IO.read(content.toString()), newFile);
    }

    public static String transformPackageToPath(String pkg) {
        return pkg.replaceAll("\\.", "/");
    }

    public static void main(String[] args) {
        System.out.println(formatCamelCaseTo("LdapAccountSource", "/"));
        System.out.println(formatCamelCaseTo("LdapAccountSource", " "));
        System.out.println(toPlural("body"));
        System.out.println(toPlural("key"));
        System.out.println(toPlural("car"));
        System.out.println(toPlural("ball"));
        System.out.println(toPlural("entry"));
        System.out.println(toPlural("journey"));
        System.out.println(toPlural("tray"));
        System.out.println(toPlural("country"));
        System.out.println(toPlural("baby"));
        System.out.println(toPlural("sky"));
    }

}
