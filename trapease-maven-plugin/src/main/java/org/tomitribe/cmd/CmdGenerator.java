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
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.apache.commons.lang3.text.WordUtils;
import org.tomitribe.cmd.base.TrapeaseTemplates;
import org.tomitribe.common.Configuration;
import org.tomitribe.common.ImportManager;
import org.tomitribe.common.Reformat;
import org.tomitribe.common.RemoveDuplicateImports;
import org.tomitribe.common.TrapeaseTypeSolver;
import org.tomitribe.common.Utils;
import org.tomitribe.model.Operation;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.tomitribe.common.Utils.getClassOperations;
import static org.tomitribe.common.Utils.getClazz;
import static org.tomitribe.common.Utils.getModel;
import static org.tomitribe.common.Utils.getRootName;

public class CmdGenerator {

    static final String CREATE_PREFIX = "Create";
    static final String UPDATE_PREFIX = "Update";
    static final String READ_PREFIX = "Read";
    static final String DELETE_PREFIX = "Delete";
    static final String CMD_SUFFIX = "Cmd";

    public static void execute() throws IOException {
        final String outputBasePackage = Configuration.RESOURCE_PACKAGE + ".cmd.base";
        createBaseTrapeaseCmd(outputBasePackage);
        CompilationUnit trapeaseCliUnit = createBaseTrapeaseCli(outputBasePackage);
        ClassOrInterfaceDeclaration trapeaseCliClass = Utils.getClazz(trapeaseCliUnit);
        MethodDeclaration commands = new MethodDeclaration();
        commands.setName("commands");
        commands.setPublic(true);
        commands.setStatic(true);
        commands.addParameter(new Parameter(new TypeParameter("Cli.CliBuilder<Runnable>"), "trapease"));
        commands.setType("void");
        trapeaseCliClass.addMember(commands);

        List<File> modelFiles = getModel();
        for (File rootClassFile : modelFiles) {
            final String rootClassSource = IO.slurp(rootClassFile);
            final CompilationUnit rootClassUnit = JavaParser.parse(rootClassSource);
            ClassOrInterfaceDeclaration rootClass = getClazz(rootClassUnit);

            if (rootClass != null) {
                if (!rootClass.getAnnotationByName("Resource").isPresent()) {
                    continue;
                }

                final String rootClassName = getRootName(getClazz(rootClassUnit));
                if (!rootClassName.equalsIgnoreCase("Account")) {
                    continue;
                }
                StringBuilder cli = new StringBuilder();
                cli.append("trapease.withGroup(\"" + rootClassName.toLowerCase() + "\")");
                cli.append(".withDescription(\"Manages " + rootClassName + ".\")");
                cli.append(".withDefaultCommand(Help.class)");

                List<String> classOperations = getClassOperations(rootClass);

                CompilationUnit createUnit = null;
                CompilationUnit updateUnit = null;

                if (classOperations == null || classOperations.contains(Operation.CREATE)) {
                    createUnit = createClass(rootClassUnit, rootClass, rootClassName, Operation.CREATE, CREATE_PREFIX);
                    cli.append(".withCommand(" + CREATE_PREFIX + rootClassName + CMD_SUFFIX + ".class)");
                    trapeaseCliUnit.addImport(Utils.getFullQualifiedName(createUnit));
                    save(CREATE_PREFIX + rootClassName + CMD_SUFFIX, rootClassUnit, createUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.UPDATE)) {
                    updateUnit = createClass(rootClassUnit, rootClass, rootClassName, Operation.UPDATE, UPDATE_PREFIX);
                    cli.append(".withCommand(" + UPDATE_PREFIX + rootClassName + CMD_SUFFIX + ".class)");
                    trapeaseCliUnit.addImport(Utils.getFullQualifiedName(updateUnit));
                    save(UPDATE_PREFIX + rootClassName + CMD_SUFFIX, rootClassUnit, updateUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.READ)) {
                    CompilationUnit readUnit = createClass(rootClassUnit, rootClass, rootClassName, Operation.READ, READ_PREFIX);
                    cli.append(".withCommand(" + READ_PREFIX + rootClassName + CMD_SUFFIX + ".class)");
                    trapeaseCliUnit.addImport(Utils.getFullQualifiedName(readUnit));
                    save(READ_PREFIX + rootClassName + CMD_SUFFIX, rootClassUnit, readUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.DELETE)) {
                    CompilationUnit deleteUnit = createClass(rootClassUnit, rootClass, rootClassName, Operation.DELETE, DELETE_PREFIX);
                    cli.append(".withCommand(" + DELETE_PREFIX + rootClassName + CMD_SUFFIX + ".class)");
                    trapeaseCliUnit.addImport(Utils.getFullQualifiedName(deleteUnit));
                    save(DELETE_PREFIX + rootClassName + CMD_SUFFIX, rootClassUnit, deleteUnit);
                }

                cli.append(";");
                Statement commandGroup = JavaParser.parseStatement(cli.toString());
                commands.getBody().get().addStatement(commandGroup);

            }
        }
        Utils.save("TrapeaseCli.java", outputBasePackage, trapeaseCliUnit.toString());
    }

    private static CompilationUnit createBaseTrapeaseCli(String outputBasePackage) throws IOException {
        final CompilationUnit content = JavaParser.parse(TrapeaseTemplates.TRAPEASE_CLI);
        content.setPackageDeclaration(outputBasePackage);
        Utils.addGeneratedAnnotation(content, Utils.getClazz(content), null);
        return content;
    }

    private static void createBaseTrapeaseCmd(String outputBasePackage) throws IOException {
        final CompilationUnit content = JavaParser.parse(TrapeaseTemplates.TRAPEASE_COMMAND);
        content.setPackageDeclaration(outputBasePackage);
        Utils.addGeneratedAnnotation(content, Utils.getClazz(content), null);
        Utils.save("TrapeaseCommand.java", outputBasePackage, content.toString());
    }

    static CompilationUnit createClass(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass,
                                       String rootClassName, String operation, String classPrefix) throws IOException {
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(Configuration.RESOURCE_PACKAGE + ".cmd");
        final String className = classPrefix + rootClassName + CMD_SUFFIX;
        newClassCompilationUnit.addClass(className, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(className).get();
        NormalAnnotationExpr command = new NormalAnnotationExpr();
        command.setName("Command");
        command.addPair("name", "\"" + classPrefix.toLowerCase() + "\"");
        newClass.addAnnotation(command);
        newClassCompilationUnit.addImport(ImportManager.getImport("Command"));

        newClass.addExtendedType("TrapeaseCommand");
        newClassCompilationUnit.addImport(Configuration.RESOURCE_PACKAGE + ".cmd.base.TrapeaseCommand");

        Utils.addLicense(rootClassUnit, newClassCompilationUnit);
        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);

        handleExtendedClasses(rootClassUnit, rootClass, operation, newClass, classPrefix);
        Optional<FieldDeclaration> id = Utils.getId(rootClass);
        if (operation == Operation.DELETE || operation == Operation.READ) {
            if (id.isPresent()) {
                handleId(id.get(), rootClassUnit, newClass);
            }
        } else if (operation == Operation.CREATE || operation == Operation.UPDATE) {
            rootClass.getFields().stream().forEach(f -> {
                writeFieldOrHandleReference(rootClassUnit, operation, classPrefix, newClass, f, null, null);
            });

        }
        createRunMethod(rootClassUnit, newClassCompilationUnit, newClass, classPrefix, operation, id.get());
        Utils.addImports(rootClassUnit, newClassCompilationUnit);

        return newClassCompilationUnit;
    }

    private static void createRunMethod(CompilationUnit rootClassUnit, CompilationUnit newClassCompilationUnit, ClassOrInterfaceDeclaration newClass, String classPrefix, String operation, FieldDeclaration id) {
        MethodDeclaration method = new MethodDeclaration();
        method.setName("run");
        method.setPublic(true);
        method.setType(new TypeParameter("void"));
        method.addMarkerAnnotation("Override");
        method.addParameter(new TypeParameter("ClientConfiguration"), "config");
        newClass.addMember(method);

        Statement statement = JavaParser.parseStatement("final ResourceClient resourceClient = new ResourceClient(config);");
        method.getBody().get().asBlockStmt().addStatement(statement);

        String className = newClass.getNameAsString();
        className = className.substring(0, className.indexOf("Cmd"));
        String action = classPrefix.toLowerCase();
        StringBuilder builder = new StringBuilder();
        String modelName = Utils.getRootName(Utils.getClazz(rootClassUnit)).toLowerCase();

        if (operation == Operation.CREATE || operation == Operation.UPDATE) {
            builder.append("final " + className + " " + modelName + " = " + className + ".builder()");
            newClass.getFields().stream().filter(f -> !f.getAnnotationByName("TypeInfo").isPresent()).forEach(f -> {
                String varName = f.getVariables().stream().findFirst().get().getNameAsString();

                FieldDeclaration fieldId = Utils.getId(Utils.getClazz(rootClassUnit)).get();
                String idRootClass = fieldId.getVariables().stream().findFirst().get().getNameAsString();
                String newFieldId = f.getVariables().stream().findFirst().get().getNameAsString();
                boolean skipBuilder = operation == Operation.UPDATE && idRootClass.equals(newFieldId);
                if (!skipBuilder) {
                    builder.append("." + varName + "(this." + varName + ")");
                }
            });

            Map<String, List<FieldDeclaration>> fieldByClass = newClass.getFields().stream()
                    .filter(f -> f.getAnnotationByName("TypeInfo").isPresent())
                    .collect(groupingBy(f -> f.getAnnotationByName("TypeInfo").get()
                            .asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString()));

            Iterator<String> it = fieldByClass.keySet().iterator();
            while (it.hasNext()) {
                String fullPath = it.next();
                String clazz = fullPath.substring(fullPath.lastIndexOf(".") + 1, fullPath.length());
                newClassCompilationUnit.addImport(fullPath);
                List<FieldDeclaration> clazzFields = fieldByClass.get(fullPath);
                List<String> builderLines =
                        clazzFields.stream().map(f -> {
                            String currentFieldName = f.getVariables().stream().findFirst().get().getNameAsString();
                            String originalFieldName = currentFieldName.replace(clazz.toLowerCase(), "");
                            String line = WordUtils.uncapitalize(originalFieldName) + "(" + currentFieldName + ")";
                            return line;
                        }).collect(Collectors.toList());
                String params = String.join(".", builderLines);
                builder.append("." + clazz.toLowerCase() + "(" + fullPath + ".builder()." + params + ".build())");
            }

            builder.append(".build();");
            Statement builderStatement = JavaParser.parseStatement(builder.toString());
            method.getBody().get().asBlockStmt().addStatement(builderStatement);
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

    private static void writeFieldOrHandleReference(CompilationUnit rootClassUnit, String operation, String classPrefix, ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String objectName, String importPath) {
        if (isWrapperOrPrimitiveOrDate(f)) {
            writeField(operation, rootClassUnit, newClass, f, objectName, importPath);
        } else {
            handleReferenceFields(rootClassUnit, operation, classPrefix, newClass, f, objectName);
        }
    }

    private static boolean isWrapperOrPrimitiveOrDate(FieldDeclaration f) {
        VariableDeclarator var = f.getVariables().stream().findFirst().get();

        boolean isWrapper = false;

        String type = var.getTypeAsString();
        if (type.contains("<")) {
            type = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
        }

        try {
            TrapeaseTypeSolver.get().solveType("java.lang." + type);
            isWrapper = true;
        } catch (RuntimeException e) {
        }

        boolean isDate = false;
        try {
            TrapeaseTypeSolver.get().solveType("java.util." + type);
            isDate = true;
        } catch (RuntimeException e) {
        }

        if (f.getCommonType().isPrimitiveType() || isWrapper || isDate) {
            return true;
        }
        return false;
    }

    private static void handleReferenceFields(CompilationUnit rootClassUnit, String operation, String classPrefix,
                                              ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String objectName) {
        VariableDeclarator type = f.getVariables().stream().findFirst().get();
        ResolvedReferenceTypeDeclaration solvedType = null;
        try {
            solvedType = JavaParserFacade.get(TrapeaseTypeSolver.get())
                    .getType(type)
                    .asReferenceType()
                    .getTypeDeclaration();

        } catch (RuntimeException e) {
            return;  // TODO: Arrays with types - skip for now
        }

        String importPath = solvedType.getPackageName() + "." + solvedType.getClassName();
        if (solvedType.isEnum()) {
            writeField(operation, rootClassUnit, newClass, f, objectName, importPath);
        } else {
            expandClassFields(rootClassUnit, operation, classPrefix, newClass, type, solvedType, importPath);
        }
    }

    private static void expandClassFields(CompilationUnit rootClassUnit, String operation, String classPrefix, ClassOrInterfaceDeclaration newClass, VariableDeclarator type, ResolvedReferenceTypeDeclaration solvedType, String importPath) {
        List<ResolvedFieldDeclaration> allFields = solvedType.getAllFields();

        List<FieldDeclaration> fieldsToBeExpanded = allFields.stream()
                .filter(field -> field instanceof JavaParserFieldDeclaration)
                .filter(field -> ((JavaParserFieldDeclaration) field).getWrappedNode() != null)
                .map(field -> ((JavaParserFieldDeclaration) field).getWrappedNode())
                .collect(Collectors.toList());

        for (FieldDeclaration expandedTobeExpanded : fieldsToBeExpanded) {
            if (!expandedTobeExpanded.getAnnotationByName("Model").isPresent()
                    || !Utils.hasOperations(expandedTobeExpanded) ||
                    Utils.isOperationPresent(expandedTobeExpanded, operation)) {
                writeFieldOrHandleReference(rootClassUnit, operation, classPrefix, newClass, expandedTobeExpanded, type.getNameAsString(), importPath);
            }
        }
    }

    private static void handleExtendedClasses(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass, String operation, ClassOrInterfaceDeclaration newClass, String prefix) throws IOException {
        NodeList<ClassOrInterfaceType> extendedTypes = rootClass.getExtendedTypes();
        while (extendedTypes.size() > 0) {
            for (ClassOrInterfaceType et : extendedTypes) {
                ClassOrInterfaceDeclaration extendedClass =
                        Utils.getExtendedClass(rootClassUnit, et.getNameAsString());

                extendedClass.getFields().forEach(f -> {
                    writeField(operation, rootClassUnit, newClass, f, null, null);
                });
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
                                   ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String objectCommandName, String importPath) {
        FieldDeclaration newField = f.clone();
        if (!newField.getAnnotationByName("Model").isPresent()
                || !Utils.hasOperations(newField) ||
                Utils.isOperationPresent(newField, operation)) {
            CompilationUnit newClassCompilationUnit = newClass.findCompilationUnit().get();
            newClass.addMember(newField);
            newField.setAnnotations(new NodeList<>());
            newField.setFinal(false);
            if (objectCommandName != null) {
                VariableDeclarator var = newField.getVariables().stream().findFirst().get();
                var.setName(WordUtils.uncapitalize(objectCommandName) + WordUtils.capitalize(var.getNameAsString()));
                newField.addSingleMemberAnnotation("TypeInfo", "\"" + importPath + "\"");
                newClassCompilationUnit.addImport("org.tomitribe.api.TypeInfo");
            }
            addCommand(f, newField, rootClassUnit, false);
            if (importPath != null) {
                newClassCompilationUnit.addImport(importPath);
            }
        } else {
            //TODO: Refactor this
            FieldDeclaration fieldId = Utils.getId(Utils.getClazz(rootClassUnit)).get();
            String id = fieldId.getVariables().stream().findFirst().get().getNameAsString();
            String newFieldId = newField.getVariables().stream().findFirst().get().getNameAsString();
            if (id.equals(newFieldId)) {
                handleId(newField, rootClassUnit, newClass);
            }
        }
    }

    private static void addCommand(FieldDeclaration oldField, FieldDeclaration field, CompilationUnit unit, boolean id) {
        String fieldName = field.getVariables().stream().findFirst().get().getNameAsString();
        StringBuilder annotation = new StringBuilder();
        boolean required = getRequired(oldField);
        if (id) {
            field.addAnnotation(JavaParser.parseAnnotation("@Arguments(required = " + id + ")"));
            unit.addImport(ImportManager.getImport("Arguments"));
        } else {
            annotation.append("@Option(name = {\"--" + Utils.formatCamelCaseTo(fieldName, "-") + "\"}");
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
            Map<String, MemberValuePair> pairs = Utils.pairs(schema.get().asNormalAnnotationExpr());
            MemberValuePair valuePair = pairs.get("required");
            if(valuePair != null){
                return valuePair.getValue().asBooleanLiteralExpr().getValue();
            }
        }
        return false;
    }

    public static void save(String className, CompilationUnit rootClassUnit, CompilationUnit classToBeSaved) throws IOException {
        if (classToBeSaved == null) {
            return;
        }
        String modified = Stream.of(classToBeSaved.toString())
                .map(RemoveDuplicateImports::apply)
                .map(Reformat::apply)
                .map(RemoveUnusedImports::removeUnusedImports)
                .findFirst().get();

        Utils.save(className + ".java", Configuration.RESOURCE_PACKAGE + ".cmd", modified);
    }
}