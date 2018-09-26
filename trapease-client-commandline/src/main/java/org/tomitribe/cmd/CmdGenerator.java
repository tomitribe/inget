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
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
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
import org.tomitribe.util.Join;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.tomitribe.common.Utils.formatCamelCaseTo;

public class CmdGenerator {
    private static final String BASE_OUTPUT_PACKAGE = Configuration.RESOURCE_PACKAGE + ".cmd.base";

    public static void execute() throws IOException {
        final List<File> sourceClients = Utils.getClient();

        generateBaseCommand();
        JavaParser.setStaticConfiguration(
                new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(TrapeaseTypeSolver.get())));

        final Map<String, List<String>> groups = new HashMap<>();
        for (final File sourceClient : sourceClients) {
            final CompilationUnit client = JavaParser.parse(sourceClient);
            final ClassOrInterfaceDeclaration clientClass = Utils.getClazz(client);
            final String clientGroup =
                    clientClass.getNameAsString().replace(Configuration.RESOURCE_SUFFIX + "Client", "");

            final List<MethodDeclaration> methods = clientClass.getMethods();
            final List<String> commands =
                    methods.stream()
                            .map(methodDeclaration -> generateCommandFromClientMethod(methodDeclaration, clientGroup))
                            .collect(Collectors.toList());

            groups.put(clientGroup, commands);
        }

        generateCli(groups);
    }

    private static void generateBaseCommand() throws IOException {
        final CompilationUnit baseCommand = JavaParser.parse(TrapeaseTemplates.TRAPEASE_COMMAND);
        baseCommand.setPackageDeclaration(BASE_OUTPUT_PACKAGE);
        Utils.addGeneratedAnnotation(baseCommand, Utils.getClazz(baseCommand), null);
        Utils.save("TrapeaseCommand.java", BASE_OUTPUT_PACKAGE, baseCommand.toString());
    }

    private static String generateCommandFromClientMethod(final MethodDeclaration clientMethod,
                                                          final String clientGroup) {
        final CompilationUnit command = new CompilationUnit(Configuration.RESOURCE_PACKAGE + ".cmd");

        final String commandClassName = clientGroup + WordUtils.capitalize(clientMethod.getNameAsString()) + "Cmd";
        command.addClass(commandClassName);

        final ClassOrInterfaceDeclaration commandClass =
                command.getClassByName(commandClassName).orElseThrow(IllegalArgumentException::new);
        addCommandAnnotation(clientMethod.getNameAsString(), command, commandClass);
        extendCommandBaseClass(command, commandClass);
        addCommandFlags(clientMethod.getParameters(), command, commandClass, clientGroup, clientMethod);

        save(commandClassName, command);

        return commandClassName;
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
                                        final ClassOrInterfaceDeclaration commandClass,
                                        final String clientGroup,
                                        final MethodDeclaration clientMethod) {

        final List<Parameter> arguments =
                parameters.stream()
                        .filter(parameter -> parameter.isAnnotationPresent("PathParam"))
                        .collect(Collectors.toList());
        if (arguments.size() == 1) {
            final Parameter parameter = arguments.get(0);
            addArgumentFlag(parameter.getType().resolve().describe(), parameter.getNameAsString(), command, commandClass);
        }

        if (arguments.size() > 1) {
            addArgumentsFlag(command, commandClass);
        }

        final List<Parameter> options = new ArrayList<>(parameters);
        options.removeAll(arguments);

        for (final Parameter option : options) {
            ResolvedType resolvedType = option.getType().resolve();
            if (isPrimitiveOrValueOf(resolvedType) || isPrimitiveOrValueOfCollection(resolvedType)) {
                addOptionFlag(option.getType().resolve().describe(), option.getNameAsString(), command, commandClass);
            } else {
                ResolvedReferenceTypeDeclaration typeDeclaration = null;
                boolean isGeneric = option.toString().contains("<");
                if (isGeneric) {
                    typeDeclaration = JavaParserFacade.get(TrapeaseTypeSolver.get())
                            .convertToUsage(option.getType().asClassOrInterfaceType().getTypeArguments().get().iterator().next())
                            .asReferenceType()
                            .getTypeDeclaration();
                } else {
                    typeDeclaration =
                            JavaParserFacade.get(TrapeaseTypeSolver.get())
                                    .getType(option)
                                    .asReferenceType()
                                    .getTypeDeclaration();
                }
                expandParameterReference(typeDeclaration, null, command, commandClass);
                addBuilder(option.getNameAsString(), typeDeclaration, command, commandClass);
            }
        }
        addRunStatement(command, commandClass, clientGroup, clientMethod);
    }

    private static void expandParameterReference(final ResolvedReferenceTypeDeclaration parameter,
                                                 final String prefix,
                                                 final CompilationUnit command,
                                                 final ClassOrInterfaceDeclaration commandClass) {
        for (final ResolvedFieldDeclaration field : parameter.getAllFields()) {
            final ResolvedType type = field.getType();

            if (isPrimitiveOrValueOf(type) || isPrimitiveOrValueOfCollection(type)) {
                addOptionFlag(type.describe(),
                        isEmpty(prefix) ? field.getName() : prefix + capitalize(field.getName()),
                        command,
                        commandClass);
            } else if (type.isReferenceType()) {
                expandParameterReference(type.asReferenceType().getTypeDeclaration(), field.getName(), command, commandClass);
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

    private static void addArgumentsFlag(final CompilationUnit command,
                                         final ClassOrInterfaceDeclaration commandClass) {
        final FieldDeclaration flag = commandClass.addField("Collection<String>", "arguments", Modifier.PRIVATE);
        command.addImport(Collection.class);

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

    private static void addBuilder(String fieldName, final ResolvedReferenceTypeDeclaration parameter,
                                   final CompilationUnit command,
                                   final ClassOrInterfaceDeclaration commandClass) {
        final MethodDeclaration run =
                commandClass.getMethodsByName("run").stream().findFirst().orElseThrow(IllegalArgumentException::new);


        boolean isBuilderClass = Optional.of(parameter.isClass() &&
                parameter instanceof JavaParserClassDeclaration &&
                ((JavaParserClassDeclaration) parameter).getWrappedNode().getAnnotationByName("Builder") != null).orElse(false);

        if (isBuilderClass) {
            List<FieldDeclaration> fields = parameter.getAllFields().stream().map(f -> ((JavaParserFieldDeclaration) f).getWrappedNode()).collect(Collectors.toList());
            final String builder = "final " +
                    parameter.getQualifiedName() +
                    " " +
                    fieldName +
                    " = " +
                    parameter.getQualifiedName() +
                    ".builder()" +
                    readBuilderFields(command, fields, "") +
                    ".build();";
            run.getBody().get().asBlockStmt().addStatement(JavaParser.parseStatement(builder));
        }
    }

    private static void addRunStatement(CompilationUnit command, final ClassOrInterfaceDeclaration commandClass, String clientGroup, MethodDeclaration clientMethod) {
        command.addImport(Configuration.getClientPackage() + "." + Configuration.CLIENT_NAME);

        List<String> runParams = clientMethod.getParameters().stream().map(p ->  {
            if(p.getTypeAsString().startsWith("List")){
                Type type = p.getType().asClassOrInterfaceType().getTypeArguments().get().stream().findFirst().get();
                if(isPrimitiveOrValueOf(type.resolve())){
                    return p.getNameAsString();
                } else{
                    ClassOrInterfaceDeclaration clazz = ((JavaParserClassDeclaration) JavaParserFacade.get(TrapeaseTypeSolver.get())
                            .convertToUsage(type)
                            .asReferenceType().getTypeDeclaration())
                            .getWrappedNode();
                    boolean wasGenerated = clazz.getAnnotationByName("Generated") != null;
                    if(wasGenerated){
                        return "java.util.Arrays.asList("+p.getNameAsString()+")";
                    }
                    //TODO: What if the object was not generated and has a List?
                }
            }
            return p.getNameAsString();
        }).collect(Collectors.toList());

        final MethodDeclaration run =
                commandClass.getMethodsByName("run").stream().findFirst().orElseThrow(IllegalArgumentException::new);
        String runCommand = "new " + Configuration.CLIENT_NAME + "(clientConfiguration)." + clientGroup.toLowerCase() + "()."
                + clientMethod.getNameAsString() + "(" + Join.join(",", runParams) + ");";
        run.getBody().get().asBlockStmt().addStatement(JavaParser.parseStatement(runCommand));
    }

    private static boolean isPrimitiveOrValueOf(final ResolvedType type) {
        if (type.isPrimitive()) {
            return true;
        }

        if (type.isReferenceType()) {
            final ResolvedReferenceTypeDeclaration typeDeclaration = type.asReferenceType().getTypeDeclaration();

            if (typeDeclaration.isEnum()) {
                return true;
            } else if (typeDeclaration.getName().equals("String") || typeDeclaration.getName().contains("<String>")) {
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

    private static boolean isPrimitiveOrValueOfCollection(final ResolvedType type) {
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

    private static void generateCli(final Map<String, List<String>> groups) throws IOException {
        final CompilationUnit cli = new CompilationUnit(BASE_OUTPUT_PACKAGE);
        cli.setPackageDeclaration(BASE_OUTPUT_PACKAGE);
        cli.addClass("TrapeaseCli");

        final ClassOrInterfaceDeclaration cliClass =
                cli.getClassByName("TrapeaseCli").orElseThrow(IllegalArgumentException::new);
        cliClass.addConstructor(Modifier.PRIVATE);

        final MethodDeclaration main = new MethodDeclaration();
        main.setPublic(true);
        main.setStatic(true);
        main.setType("void");
        main.setName("main");
        main.addAndGetParameter(String.class, "args").setVarArgs(true);

        final BlockStmt block = new BlockStmt();
        cli.addImport(ImportManager.getImport("Cli"));
        block.addStatement("final Cli.CliBuilder<Runnable> cliBuilder = Cli.builder(\"trapease\");");
        cli.addImport(ImportManager.getImport("Help"));
        block.addStatement("cliBuilder.withDefaultCommand(Help.class);");
        block.addStatement("cliBuilder.withCommand(Help.class);");

        main.setBody(block);
        cliClass.addMember(main);

        for (final String group : groups.keySet()) {
            final StringBuilder groupCommand = new StringBuilder();
            groupCommand.append("cliBuilder.withGroup(\"").append(formatCamelCaseTo(group, "-")).append("\")");
            groupCommand.append(".withDefaultCommand(Help.class)");
            final List<String> commands = groups.get(group);
            for (final String command : commands) {
                groupCommand.append(".withCommand(").append(command).append(".class").append(")");
                cli.addImport(Configuration.RESOURCE_PACKAGE + ".cmd." + command);
            }
            groupCommand.append(";");
            block.addStatement(groupCommand.toString());
        }

        block.addStatement("final Cli<Runnable> cli = cliBuilder.build();");
        block.addStatement("cli.parse(args).run();");

        Utils.save("TrapeaseCli.java", BASE_OUTPUT_PACKAGE, cli.toString());
    }

    private static void save(final String className, final CompilationUnit classToBeSaved) {
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

    private static String readBuilderFields(final CompilationUnit rootClassUnit, final List<FieldDeclaration> fields, final String prefix) {
        return fields
                .stream()
                .map(fieldDeclaration -> readFieldOrUnflattenClass(rootClassUnit, fieldDeclaration, prefix))
                .collect(Collectors.joining());
    }

    private static String readFieldOrUnflattenClass(final CompilationUnit rootClassUnit, final FieldDeclaration field, final String prefix) {
        if (Utils.isWrapperOrPrimitiveOrDate(field)) {
            return readField(field, prefix);
        } else {
            return unFlattenClass(rootClassUnit, field, prefix);
        }
    }

    private static String unFlattenClass(final CompilationUnit rootClassUnit, final FieldDeclaration field, final String prefix) {
        final ResolvedReferenceTypeDeclaration resolvedType =
                JavaParserFacade.get(TrapeaseTypeSolver.get())
                        .getType(field.getVariables().get(0))
                        .asReferenceType()
                        .getTypeDeclaration();

        if (resolvedType.isEnum()) {
            return readField(field, prefix);
        } else {
            final List<ResolvedFieldDeclaration> allFields = resolvedType.getAllFields();

            final List<FieldDeclaration> fieldsToBeExpanded =
                    allFields.stream()
                            .filter(f -> f instanceof JavaParserFieldDeclaration)
                            .filter(f -> ((JavaParserFieldDeclaration) f).getWrappedNode() != null)
                            .map(f -> ((JavaParserFieldDeclaration) f).getWrappedNode())
                            .filter(f -> !f.getAnnotationByName("Model").isPresent() ||
                                    !Utils.hasOperations(f))
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
                // TODO Fix this
                objectClass = "CREATE".contains("CREATE") ?
                        "Create" + resolvedType.getName().substring(0, model) :
                        "Update" + resolvedType.getName().substring(0, model);
                rootClassUnit.addImport(resolvedType.getPackageName() + "." + objectClass);
            }

            return "." + field.getVariables().get(0) +
                    "(" +
                    objectClass +
                    ".builder()" +
                    readBuilderFields(rootClassUnit, fieldsToBeExpanded, field.getVariables().get(0).getNameAsString()) +
                    ".build()" +
                    ")";
        }
    }

    private static String readField(final FieldDeclaration field, final String prefix) {
        FieldDeclaration newField = field.clone();
        if (!newField.getAnnotationByName("Model").isPresent() ||
                !Utils.hasOperations(newField)) {
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
            if (ann.isNormalAnnotationExpr()) {
                Map<String, MemberValuePair> pairs = Utils.pairs(schema.get().asNormalAnnotationExpr());
                MemberValuePair valuePair = pairs.get("required");
                if (valuePair != null) {
                    return valuePair.getValue().asBooleanLiteralExpr().getValue();
                }
            }
        }
        return false;
    }
}
