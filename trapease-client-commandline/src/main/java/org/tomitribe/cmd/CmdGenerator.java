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
package org.tomitribe.cmd;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.apache.commons.lang3.text.WordUtils;
import org.tomitribe.cmd.base.TrapeaseTemplates;
import org.tomitribe.common.Configuration;
import org.tomitribe.common.ImportManager;
import org.tomitribe.common.Operation;
import org.tomitribe.common.Reformat;
import org.tomitribe.common.RemoveDuplicateImports;
import org.tomitribe.common.TrapeaseTypeSolver;
import org.tomitribe.common.Utils;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tomitribe.common.Utils.formatCamelCaseTo;
import static org.tomitribe.common.Utils.sortNodes;

public class CmdGenerator {
    private static final String CMD_SUFFIX = "Cmd";
    private static final String BASE_OUTPUT_PACKAGE = Configuration.RESOURCE_PACKAGE + ".cmd.base";

    public static void execute() throws IOException {
        final List<File> sourceClients = Utils.getClient();

        generateBaseCommand();
        JavaParser.setStaticConfiguration(
                new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(TrapeaseTypeSolver.get())));

        for (final File sourceClient : sourceClients) {
            final CompilationUnit client = JavaParser.parse(sourceClient);
            final ClassOrInterfaceDeclaration clientClass = Utils.getClazz(client);
            final String clientGroup =
                    clientClass.getNameAsString().replace(Configuration.RESOURCE_SUFFIX + "Client", "");
            final List<MethodDeclaration> methods = clientClass.getMethods();
            methods.forEach(methodDeclaration -> generateCommandFromClientMethod(methodDeclaration, clientGroup));
        }
    }

    private static void generateBaseCommand() throws IOException {
        final CompilationUnit baseCommand = JavaParser.parse(TrapeaseTemplates.TRAPEASE_COMMAND);
        baseCommand.setPackageDeclaration(BASE_OUTPUT_PACKAGE);
        Utils.addGeneratedAnnotation(baseCommand, Utils.getClazz(baseCommand), null);
        Utils.save("TrapeaseCommand.java", BASE_OUTPUT_PACKAGE, baseCommand.toString());
    }

    private static void generateCommandFromClientMethod(final MethodDeclaration clientMethod,
                                                        final String clientGroup) {
        final CompilationUnit command = new CompilationUnit(BASE_OUTPUT_PACKAGE);

        final String commandClassName = clientGroup + WordUtils.capitalize(clientMethod.getNameAsString()) + "Cmd";
        command.addClass(commandClassName);

        final ClassOrInterfaceDeclaration commandClass =
                command.getClassByName(commandClassName).orElseThrow(IllegalArgumentException::new);
        addCommandAnnotation(clientMethod.getNameAsString(), command, commandClass);
        extendCommandBaseClass(command, commandClass);
        addCommandFlags(clientMethod.getParameters(), command, commandClass);

        saveCommand(commandClassName, command);
    }

    private static void addCommandAnnotation(final String clientMethodName,
                                             final CompilationUnit command,
                                             final ClassOrInterfaceDeclaration commandClass) {
        final NormalAnnotationExpr commandAnnotation = new NormalAnnotationExpr();
        commandAnnotation.setName("Command");
        commandAnnotation.addPair("name", "\"" + formatCamelCaseTo(clientMethodName, "-") + "\"");
        commandClass.addAnnotation(commandAnnotation);
        command.addImport(ImportManager.getImport("Command"));
    }

    private static void extendCommandBaseClass(final CompilationUnit command,
                                               final ClassOrInterfaceDeclaration commandClass) {
        commandClass.addExtendedType("TrapeaseCommand");
        command.addImport(BASE_OUTPUT_PACKAGE + ".TrapeaseCommand");

        final MethodDeclaration method = new MethodDeclaration();
        method.setName("run");
        method.setPublic(true);
        method.setType(new VoidType());
        method.addMarkerAnnotation(Override.class);
        method.addParameter(
                new Parameter(EnumSet.of(Modifier.FINAL),
                              new TypeParameter("ClientConfiguration"),
                              new SimpleName("clientConfiguration")));
        commandClass.addMember(method);
        command.addImport(Configuration.RESOURCE_PACKAGE + ".client.base.ClientConfiguration");
    }

    private static void addCommandFlags(final NodeList<Parameter> parameters,
                                        final CompilationUnit command,
                                        final ClassOrInterfaceDeclaration commandClass) {
        for (final Parameter parameter : parameters) {
            if (isPrimitiveOrValueOf(parameter.getType().resolve())) {
                addCommandFlag(parameter, command, commandClass);
            } else {
                expandParameterReference(JavaParserFacade.get(TrapeaseTypeSolver.get())
                                                         .getType(parameter)
                                                         .asReferenceType()
                                                         .getTypeDeclaration(),
                                         command,
                                         commandClass);
            }
        }
    }

    private static void addCommandFlag(final Parameter parameter,
                                       final CompilationUnit command,
                                       final ClassOrInterfaceDeclaration commandClass) {
        if (parameter.isAnnotationPresent("PathParam")) {
            addArgumentFlag(parameter.getType().asString(), parameter.getNameAsString(), command, commandClass);
        } else {
            addOptionFlag(parameter.getType().asString(), parameter.getNameAsString(), command, commandClass);
        }
    }

    private static void expandParameterReference(final ResolvedReferenceTypeDeclaration parameter,
                                                 final CompilationUnit command,
                                                 final ClassOrInterfaceDeclaration commandClass) {
        for (final ResolvedFieldDeclaration field : parameter.getAllFields()) {
            final ResolvedType type = field.getType();

            if (isPrimitiveOrValueOf(type) || isCollection(type)) {
                addOptionFlag(type.describe(), field.getName(), command, commandClass);
            } else if (type.isReferenceType()) {
                //expandParameterReference(type.asReferenceType().getTypeDeclaration(), command, commandClass);
            }
        }
    }

    private static void addArgumentFlag(final String type,
                                        final String name,
                                        final CompilationUnit command,
                                        final ClassOrInterfaceDeclaration commandClass) {
        final FieldDeclaration flag = commandClass.addField(type, name, Modifier.PRIVATE);

        final NormalAnnotationExpr argumentsAnnotation = new NormalAnnotationExpr();
        argumentsAnnotation.setName("Arguments");
        argumentsAnnotation.addPair("required", "true");
        command.addImport(ImportManager.getImport("Arguments"));
        flag.addAnnotation(argumentsAnnotation);
    }

    private static void addOptionFlag(final String type,
                                      final String name,
                                      final CompilationUnit command,
                                      final ClassOrInterfaceDeclaration commandClass) {
        final FieldDeclaration flag = commandClass.addField(type, name, Modifier.PRIVATE);

        final NormalAnnotationExpr argumentsAnnotation = new NormalAnnotationExpr();
        argumentsAnnotation.setName("Option");
        argumentsAnnotation.addPair("name", "\"--" + formatCamelCaseTo(name, "-") + "\"");
        command.addImport(ImportManager.getImport("Option"));
        flag.addAnnotation(argumentsAnnotation);
    }

    private static boolean isPrimitiveOrValueOf(final ResolvedType type) {
        if (type.isPrimitive()) {
            return true;
        }

        if (type.isReferenceType()) {
            final ResolvedReferenceTypeDeclaration typeDeclaration = type.asReferenceType().getTypeDeclaration();

            if (typeDeclaration.isEnum()) {
                return false; // TODO - change
            } else if (typeDeclaration.getName().equals("String")) {
                return true;
            } else if (typeDeclaration.getDeclaredMethods()
                                      .stream()
                                      .filter(method -> method.getName().equals("valueOf"))
                                      .anyMatch(method -> method.getNumberOfParams() == 1)) {
                return true;
            } else if (typeDeclaration.isClass()) {
                return typeDeclaration.asClass().getConstructors()
                                      .stream()
                                      .filter(constructor -> constructor.getNumberOfParams() == 1)
                                      .anyMatch(constructor -> constructor.getParam(0).describeType().contains("String"));
            }
        }

        return false;
    }

    private static boolean isCollection(final ResolvedType type) {
        if (type.isReferenceType()) {
            final ResolvedReferenceTypeDeclaration typeDeclaration = type.asReferenceType().getTypeDeclaration();

            if (typeDeclaration.canBeAssignedTo(TrapeaseTypeSolver.get().solveType("java.util.Collection"))) {
                final List<ResolvedType> collectionParameters = type.asReferenceType().typeParametersValues();
                if (collectionParameters.size() == 1) {
                    return isPrimitiveOrValueOf(collectionParameters.get(0));
                }
            }
        }

        return false;
    }

    private static void saveCommand(final String className, final CompilationUnit classToBeSaved) {
        if (classToBeSaved == null) {
            return;
        }

        final String modified =
                Stream.of(classToBeSaved.toString())
                      .map(RemoveDuplicateImports::apply)
                      .map(Reformat::apply)
                      .map(RemoveUnusedImports::removeUnusedImports)
                      .findFirst()
                      .orElseThrow(IllegalStateException::new);

        try {
            Utils.save(className + ".java", Configuration.RESOURCE_PACKAGE + ".cmd", modified);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static MethodDeclaration getCommandMethod(ClassOrInterfaceDeclaration trapeaseCliClass) {
        MethodDeclaration commands = new MethodDeclaration();
        commands.setName("commands");
        commands.setPublic(true);
        commands.setStatic(true);
        commands.addParameter(new Parameter(new TypeParameter("Cli.CliBuilder<Runnable>"), "trapease"));
        commands.setType("void");
        trapeaseCliClass.addMember(commands);
        return commands;
    }

    private static CompilationUnit createBaseTrapeaseCli() throws IOException {
        final CompilationUnit content = JavaParser.parse(TrapeaseTemplates.TRAPEASE_CLI);
        content.setPackageDeclaration(BASE_OUTPUT_PACKAGE);
        Utils.addGeneratedAnnotation(content, Utils.getClazz(content), null);
        return content;
    }

    private static CompilationUnit createClass(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass,
                                               String rootClassName, String operation, String classPrefix)
            throws IOException {

        final CompilationUnit newClassCompilationUnit = new CompilationUnit(Configuration.CMD_PACKAGE);
        final String className = classPrefix + rootClassName + CMD_SUFFIX;
        newClassCompilationUnit.addClass(className, Modifier.PUBLIC);

        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(className).get();
        addCommandAnnotation(classPrefix, newClassCompilationUnit, newClass);

        newClass.addExtendedType("TrapeaseCommand");
        newClassCompilationUnit.addImport(Configuration.RESOURCE_PACKAGE + ".cmd.base.TrapeaseCommand");

        Utils.addLicense(rootClassUnit, newClassCompilationUnit);
        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);

        handleExtendedClasses(rootClassUnit, rootClass, operation, newClass);
        Optional<FieldDeclaration> id = Utils.getId(rootClass);

        if (!id.isPresent()) {
            throw new RuntimeException(rootClass.getNameAsString() + " must have one field annotated with '@Model(id = true).'");
        }

        // For DELETE and  UPDATE only ID is needed
        if (Objects.equals(operation, Operation.DELETE) || Objects.equals(operation, Operation.READ)) {
            handleId(id.get(), rootClassUnit, newClass);
        } else if (Objects.equals(operation, Operation.CREATE) || Objects.equals(operation, Operation.UPDATE)) {
            // Other fields must be written or flattened
            rootClass.getFields().forEach(f -> writeFieldOrFlattenClass(rootClassUnit, operation, classPrefix, newClass, f, null, null, null));
        }
        createRunMethod(rootClassUnit, newClassCompilationUnit, rootClass, newClass, classPrefix, operation, id.get());
        Utils.addImports(rootClassUnit, newClassCompilationUnit);

        return newClassCompilationUnit;
    }

    private static void createRunMethod(CompilationUnit rootClassUnit,
                                        CompilationUnit newClassCompilationUnit,
                                        ClassOrInterfaceDeclaration rootClass,
                                        ClassOrInterfaceDeclaration newClass,
                                        String classPrefix,
                                        String operation,
                                        FieldDeclaration id) {
        MethodDeclaration method = new MethodDeclaration();
        method.setName("run");
        method.setPublic(true);
        method.setType(new TypeParameter("void"));
        method.addMarkerAnnotation("Override");
        method.addParameter(new TypeParameter("ClientConfiguration"), "config");
        newClass.addMember(method);

        newClassCompilationUnit.addImport(Configuration.RESOURCE_PACKAGE + ".client." + Configuration.CLIENT_NAME);
        Statement statement = JavaParser.parseStatement("final " +
                                                        Configuration.CLIENT_NAME + " resourceClient = new " +
                                                        Configuration.CLIENT_NAME + "(config);");
        method.getBody().get().asBlockStmt().addStatement(statement);

        String className = newClass.getNameAsString();
        className = className.substring(0, className.indexOf("Cmd"));
        String action = classPrefix.toLowerCase();
        String modelName = Utils.getRootName(Utils.getClazz(rootClassUnit)).toLowerCase();
        if (Objects.equals(operation, Operation.CREATE) || Objects.equals(operation, Operation.UPDATE)) {

            addBuilder(rootClassUnit, rootClass, method, className, modelName, operation);

            Statement actionStatement = null;
            if (action.equals("create")) {
                actionStatement = JavaParser.parseStatement("resourceClient." + modelName + "()." + action + "(" + modelName + ");");
            } else if (action.equals("update")) {
                actionStatement = JavaParser.parseStatement("resourceClient." + modelName + "()." + action + "(" + id.getVariables().stream().findFirst().get() + "," + modelName + ");");
            }
            method.getBody().get().asBlockStmt().addStatement(actionStatement);
        } else {
            Statement actionStatement = JavaParser.parseStatement("resourceClient." + modelName + "()." + action + "(" + id.getVariables().stream().findFirst().get() + ");");
            method.getBody().get().asBlockStmt().addStatement(actionStatement);
        }

        newClassCompilationUnit.addImport(rootClassUnit.getPackageDeclaration().get().getNameAsString() + "." + className);
        newClassCompilationUnit.addImport(Configuration.RESOURCE_PACKAGE + ".client.base.ClientConfiguration");
        newClassCompilationUnit.addImport(Configuration.RESOURCE_PACKAGE + ".client.ResourceClient");
    }

    private static void addBuilder(final CompilationUnit rootClassUnit,
                                   final ClassOrInterfaceDeclaration rootClass,
                                   final MethodDeclaration method,
                                   final String className,
                                   final String modelName,
                                   final String operation) {

        final String builder = "final " +
                               className +
                               " " +
                               modelName +
                               " = " +
                               className +
                               ".builder()" +
                               readBuilderFields(rootClassUnit, rootClass.getFields(), operation, "") +
                               ".build();";

        try {
            method.getBody().get().asBlockStmt().addStatement(JavaParser.parseStatement(builder));
        } catch (Exception e) {

        }
    }

    private static String readBuilderFields(final CompilationUnit rootClassUnit, final List<FieldDeclaration> fields,
                                            final String operation, final String prefix) {
        return fields
                .stream()
                .map(fieldDeclaration -> readFieldOrUnflattenClass(rootClassUnit, fieldDeclaration, operation, prefix))
                .collect(Collectors.joining());
    }

    private static String readFieldOrUnflattenClass(final CompilationUnit rootClassUnit, final FieldDeclaration field,
                                                    final String operation, final String prefix) {
        if (Utils.isWrapperOrPrimitiveOrDate(field)) {
            return readField(field, operation, prefix);
        } else {
            return unFlattenClass(rootClassUnit, field, operation, prefix);
        }
    }

    private static String unFlattenClass(final CompilationUnit rootClassUnit, final FieldDeclaration field,
                                         final String operation, final String prefix) {
        final ResolvedReferenceTypeDeclaration resolvedType =
                JavaParserFacade.get(TrapeaseTypeSolver.get())
                                .getType(field.getVariables().get(0))
                                .asReferenceType()
                                .getTypeDeclaration();

        if (resolvedType.isEnum()) {
            return readField(field, operation, prefix);
        } else {
            final List<ResolvedFieldDeclaration> allFields = resolvedType.getAllFields();

            final List<FieldDeclaration> fieldsToBeExpanded =
                    allFields.stream()
                             .filter(f -> f instanceof JavaParserFieldDeclaration)
                             .filter(f -> ((JavaParserFieldDeclaration) f).getWrappedNode() != null)
                             .map(f -> ((JavaParserFieldDeclaration) f).getWrappedNode())
                             .filter(f -> !f.getAnnotationByName("Model").isPresent() ||
                                          !Utils.hasOperations(f) ||
                                          Utils.isOperationPresent(f, operation))
                             .collect(Collectors.toList());

            if (fieldsToBeExpanded.isEmpty()) {
                return "";
            }

            final String objectClass;
            final int model = resolvedType.getName().indexOf("Model");
            if (model == -1) {
                objectClass = resolvedType.getName();
                rootClassUnit.addImport(resolvedType.getQualifiedName());
            } else {
                objectClass = operation.contains("CREATE") ?
                              "Create" + resolvedType.getName().substring(0, model) :
                              "Update" + resolvedType.getName().substring(0, model);
                rootClassUnit.addImport(resolvedType.getPackageName() + "." + objectClass);
            }

            return "." + field.getVariables().get(0) +
                   "(" +
                   objectClass +
                   ".builder()" +
                   readBuilderFields(rootClassUnit, fieldsToBeExpanded, operation, field.getVariables().get(0).getNameAsString()) +
                   ".build()" +
                   ")";
        }
    }

    private static String readField(final FieldDeclaration field, final String operation, final String prefix) {
        FieldDeclaration newField = field.clone();
        if (!newField.getAnnotationByName("Model").isPresent() ||
            !Utils.hasOperations(newField) ||
            Utils.isOperationPresent(newField, operation)) {
            final String fieldName = field.getVariables().get(0).getNameAsString();
            final String readFieldName = "".equals(prefix) ? fieldName : prefix + WordUtils.capitalize(fieldName);
            return "." + fieldName + "( " + readFieldName + ")";
        }

        return "";
    }

    private static void writeFieldOrFlattenClass(CompilationUnit rootClassUnit, String operation, String classPrefix,
                                                 ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String objectName,
                                                 String pkg, String clazzName) {
        if (Utils.isWrapperOrPrimitiveOrDate(f)) {
            writeField(operation, rootClassUnit, newClass, f, objectName, pkg, clazzName, false);
        } else {
            resolveFieldClass(rootClassUnit, operation, classPrefix, newClass, f, objectName);
        }
    }

    private static void resolveFieldClass(CompilationUnit rootClassUnit, String operation, String classPrefix,
                                          ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String objectName) {
        VariableDeclarator type = f.getVariables().stream().findFirst().get();
        ResolvedReferenceTypeDeclaration solvedType;
        try {
            solvedType = JavaParserFacade.get(TrapeaseTypeSolver.get())
                    .getType(type)
                    .asReferenceType()
                    .getTypeDeclaration();

        } catch (RuntimeException e) {
            return;  // TODO: Arrays with types - skip for now
        }

        if (solvedType.isEnum()) {
            writeField(operation, rootClassUnit, newClass, f, objectName, solvedType.getPackageName(), solvedType.getClassName(), true);
        } else {
            flattenClassFields(rootClassUnit, operation, classPrefix, newClass, type, solvedType, solvedType.getPackageName(), solvedType.getClassName());
        }
    }

    private static void flattenClassFields(CompilationUnit rootClassUnit, String operation, String classPrefix,
                                           ClassOrInterfaceDeclaration newClass, VariableDeclarator type,
                                           ResolvedReferenceTypeDeclaration solvedType, String pkg, String clazzName) {
        List<ResolvedFieldDeclaration> allFields = solvedType.getAllFields();

        List<FieldDeclaration> fieldsToBeExpanded = allFields.stream()
                .filter(field -> field instanceof JavaParserFieldDeclaration)
                .filter(field -> ((JavaParserFieldDeclaration) field).getWrappedNode() != null)
                .map(field -> ((JavaParserFieldDeclaration) field).getWrappedNode())
                .collect(Collectors.toList());

        fieldsToBeExpanded.forEach(field -> {
            if (!field.getAnnotationByName("Model").isPresent()
                    || !Utils.hasOperations(field) ||
                    Utils.isOperationPresent(field, operation)) {
                writeFieldOrFlattenClass(rootClassUnit, operation, classPrefix, newClass, field, type.getNameAsString(), pkg, clazzName);
            }
        });
    }

    private static void handleExtendedClasses(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass,
                                              String operation, ClassOrInterfaceDeclaration newClass) throws IOException {
        NodeList<ClassOrInterfaceType> extendedTypes = rootClass.getExtendedTypes();
        while (extendedTypes.size() > 0) {
            for (ClassOrInterfaceType et : extendedTypes) {
                ClassOrInterfaceDeclaration extendedClass =
                        Utils.getExtendedClass(rootClassUnit, et.getNameAsString());

                extendedClass.getFields().forEach(f -> writeField(operation, rootClassUnit, newClass, f, null, null, null, false));
                Utils.addImports(extendedClass.findCompilationUnit().get(), newClass.findCompilationUnit().get());
                extendedTypes = extendedClass.getExtendedTypes();
            }
        }
    }

    private static void handleId(FieldDeclaration f, CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration newClass) {
        FieldDeclaration newField = f.clone();
        newClass.addMember(newField);
        newField.setAnnotations(new NodeList<>());
        newField.setFinal(false);
        addCommand(f, newField, rootClassUnit, true);
    }

    private static void writeField(String operation, CompilationUnit rootClassUnit,
                                   ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String objectCommandName,
                                   String pkg, String clazzName, boolean isEnum) {
        FieldDeclaration newField = f.clone();
        if (!newField.getAnnotationByName("Model").isPresent()
                || !Utils.hasOperations(newField) ||
                Utils.isOperationPresent(newField, operation)) {
            CompilationUnit newClassCompilationUnit = newClass.findCompilationUnit().get();
            newClass.addMember(newField);
            newField.setAnnotations(new NodeList<>());
            newField.setFinal(false);

            if (objectCommandName != null) {
                VariableDeclarator varNewField = newField.getVariables().stream().findFirst().get();
                // Change name to have the object before the field name
                varNewField.setName(WordUtils.uncapitalize(objectCommandName) + WordUtils.capitalize(varNewField.getNameAsString()));
                addTypeInfoAnnotation(pkg, clazzName, isEnum, newField, newClassCompilationUnit);
            }

            addCommand(f, newField, rootClassUnit, false);
            if (pkg != null && clazzName != null) {
                newClassCompilationUnit.addImport(pkg + "." + clazzName);
            }
        } else if (Utils.isId(newField) && Objects.equals(operation, Operation.UPDATE)) {
            handleId(newField, rootClassUnit, newClass);
        }
    }

    private static void addTypeInfoAnnotation(String pkg, String clazzName, boolean isEnum, FieldDeclaration newField, CompilationUnit newClassCompilationUnit) {
        NormalAnnotationExpr typeInfo = new NormalAnnotationExpr();
        typeInfo.setName("TypeInfo");
        typeInfo.addPair("value", "\"" + pkg + "-" + clazzName + "\"");
        if (isEnum) {
            typeInfo.addPair("isEnum", "true");
        }
        newClassCompilationUnit.addImport("org.tomitribe.api.TypeInfo");
        newField.addAnnotation(typeInfo);
    }

    private static void addCommand(FieldDeclaration oldField, FieldDeclaration field, CompilationUnit unit, boolean id) {
        String fieldName = field.getVariables().stream().findFirst().get().getNameAsString();
        StringBuilder annotation = new StringBuilder();
        boolean required = getRequired(oldField);
        if (id) {
            field.addAnnotation(JavaParser.parseAnnotation("@Arguments(required = " + id + ")"));
            unit.addImport(ImportManager.getImport("Arguments"));
        } else {
            annotation.append("@Option(name = {\"--").append(formatCamelCaseTo(fieldName, "-")).append("\"}");
            if (required) {
                annotation.append(", required = true ");
            }
            annotation.append(")");
            field.addAnnotation(JavaParser.parseAnnotation(annotation.toString()));
            unit.addImport(ImportManager.getImport("Option"));
        }
    }

    private static boolean getRequired(FieldDeclaration field) {
        Optional<AnnotationExpr> schema = field.getAnnotationByName("Schema");
        if (schema.isPresent()) {
            AnnotationExpr ann = schema.get();
            if(ann.isNormalAnnotationExpr()){
                Map<String, MemberValuePair> pairs = Utils.pairs(schema.get().asNormalAnnotationExpr());
                MemberValuePair valuePair = pairs.get("required");
                if (valuePair != null) {
                    return valuePair.getValue().asBooleanLiteralExpr().getValue();
                }
            }
        }
        return false;
    }

    private static void addClassCommandAnnotation(String classPrefix, CompilationUnit newClassCompilationUnit, ClassOrInterfaceDeclaration newClass) {
        NormalAnnotationExpr command = new NormalAnnotationExpr();
        command.setName("Command");
        command.addPair("name", "\"" + classPrefix.toLowerCase() + "\"");
        newClass.addAnnotation(command);
        newClassCompilationUnit.addImport(ImportManager.getImport("Command"));
    }

}
