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
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.TypeParameter;
import org.tomitribe.common.Configuration;
import org.tomitribe.common.ImportManager;
import org.tomitribe.common.Operation;
import org.tomitribe.common.Utils;
import org.tomitribe.exception.GeneratorException;
import org.tomitribe.util.Strings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.tomitribe.common.ImportManager.getImport;
import static org.tomitribe.common.Utils.isRootResource;

public class MethodGenerator {

    private static final String CREATE = "Create";
    private static final String UPDATE = "Update";
    private static String ID_PARAM = "id";


    public static String apply(CompilationUnit resourceUnit, CompilationUnit rootClassUnit) {
        ClassOrInterfaceDeclaration resourceClass = Utils.getClazz(resourceUnit);
        ClassOrInterfaceDeclaration rootClass = Utils.getClazz(rootClassUnit);
        final String rootClassName = Utils.getRootName(rootClass);
        final String rootClassPackage = rootClassUnit.getPackageDeclaration().get().getName().toString();
        ID_PARAM = Utils.getIdName(rootClass);

        removeGeneratedMethods(resourceClass);
//        removeCrudAndBulkMethods(resourceClass);
        generateMethods(resourceUnit, resourceClass, rootClass, rootClassName, rootClassPackage);
        return resourceUnit.toString();
    }

    private static void generateMethods(CompilationUnit resourceUnit, ClassOrInterfaceDeclaration resourceClass, ClassOrInterfaceDeclaration rootClass, String rootClassName, String rootClassPackage) {
        List<String> classOperations = Utils.getClassOperations(rootClass);
        if (isRootResource(rootClassName, resourceClass.getName().toString())) {
            createRootResourceMethods(resourceUnit, resourceClass, rootClass, rootClassName, rootClassPackage, classOperations);

        } else {
            createListResourceMethods(resourceUnit, resourceClass, rootClass, rootClassName, rootClassPackage, classOperations);
        }
    }

    private static void createListResourceMethods(CompilationUnit resourceUnit, ClassOrInterfaceDeclaration resourceClass, ClassOrInterfaceDeclaration rootClass, String rootClassName, String rootClassPackage, List<String> classOperations) {
        if (classOperations != null && classOperations.contains(Operation.BULK_CREATE)) {
            bulkCreate(rootClassName, rootClassPackage, resourceClass, resourceUnit);
        }

        if (classOperations != null && classOperations.contains(Operation.BULK_UPDATE)) {
            bulkUpdate(rootClassName, rootClassPackage, resourceClass, resourceUnit);
        }

        if (classOperations != null && classOperations.contains(Operation.BULK_DELETE)) {
            bulkDelete(rootClassName, rootClass, resourceClass, resourceUnit);
        }

        if (classOperations != null && classOperations.contains(Operation.READ_ALL)) {
            readAll(rootClassName, rootClass, resourceClass, resourceUnit);
        }
    }

    private static void createRootResourceMethods(CompilationUnit resourceUnit, ClassOrInterfaceDeclaration resourceClass, ClassOrInterfaceDeclaration rootClass, String rootClassName, String rootClassPackage, List<String> classOperations) {
        if (classOperations == null || classOperations.contains(Operation.CREATE)) {
            create(rootClassName, rootClassPackage, resourceClass, resourceUnit);
        }

        if (classOperations == null || classOperations.contains(Operation.UPDATE)) {
            update(rootClassName, rootClass, rootClassPackage, resourceClass, resourceUnit);
        }

        if (classOperations == null || classOperations.contains(Operation.READ)) {
            read(rootClassName, rootClass, resourceClass, resourceUnit);
        }

        if (classOperations == null || classOperations.contains(Operation.DELETE)) {
            delete(rootClassName, rootClass, resourceClass, resourceUnit);
        }
    }

    private static void removeGeneratedMethods(ClassOrInterfaceDeclaration resourceClass) {
        resourceClass.getMethods().forEach(method -> {
            Optional<AnnotationExpr> generated = method.getAnnotationByName("Generated");
            if (generated.isPresent()) {
                resourceClass.remove(method);
            }
        });
    }

    // TODO: Remove when all methods have @Generated
    private static void removeCrudAndBulkMethods(ClassOrInterfaceDeclaration resourceClass) {
        resourceClass.getMethods().forEach(method -> {
            boolean remove =
                    Utils.isMethodCreate(method) ||
                            Utils.isMethodUpdate(method, ID_PARAM) ||
                            Utils.isMethodDelete(method, ID_PARAM) ||
                            Utils.isMethodRead(method, ID_PARAM) ||
                            Utils.isMethodBulkCreate(method) ||
                            Utils.isMethodBulkUpdate(method) ||
                            Utils.isMethodBulkDelete(method);
            if (remove) {
                resourceClass.remove(method);
            }
        });
    }

    private static MethodDeclaration createBaseMethod(final String methodName, final String verb, final boolean path,
                                                      ClassOrInterfaceDeclaration clazz, CompilationUnit unit, AnnotationExpr operation) {
        unit.addImport(getImport(verb));

        MethodDeclaration method = clazz.addMethod(methodName);
        method.removeBody();

        method.setType("Response");
        unit.addImport(getImport("Response"));

        method.addMarkerAnnotation(verb);
        if (path) {
            unit.addImport(getImport("Path"));
            NormalAnnotationExpr pathAnnotation = new NormalAnnotationExpr();
            pathAnnotation.setName("Path");
            pathAnnotation.addPair("value", "\"{" + ID_PARAM + "}\"");
            method.addAnnotation(pathAnnotation);
        }

        Utils.addGeneratedAnnotation(unit, null, method);

        if (operation != null) {
            method.addAnnotation(operation);
            unit.addImport(getImport("Operation"));
        }

        return method;
    }

    private static void createParameter(final String paramType,
                                        final String paramName,
                                        final boolean pathParam,
                                        final List<String> imports,
                                        final String description,
                                        final String example,
                                        final MethodDeclaration baseMethod,
                                        CompilationUnit unit) {
        TypeParameter paramClazzType = new TypeParameter(paramType);
        Parameter parameter = new Parameter(paramClazzType, paramName);
        baseMethod.addParameter(parameter);

        Optional.ofNullable(imports).ifPresent(values -> {
            values.stream().forEach(imp -> {
                unit.addImport(imp);
            });
        });

        NormalAnnotationExpr paramAnnotation = new NormalAnnotationExpr();
        paramAnnotation.setName("Parameter");
        paramAnnotation.addPair("description", "\"" + description + "\"");
        if (example != null) {
            paramAnnotation.addPair("example", "\"" + example + "\"");
        }
        paramAnnotation.addPair("required", "true");
        parameter.addAnnotation(paramAnnotation);

        if (pathParam) {
            unit.addImport("javax.ws.rs.PathParam");
            NormalAnnotationExpr pathParamAnnotation = new NormalAnnotationExpr();
            pathParamAnnotation.setName("PathParam");
            pathParamAnnotation.addPair("value", "\"" + paramName + "\"");
            parameter.addAnnotation(pathParamAnnotation);
        }
        unit.addImport("io.swagger.v3.oas.annotations.Parameter");
    }

    private static void createRequestBody(final String paramType,
                                          final String paramName,
                                          final String paramPackage,
                                          final String description,
                                          final String example,
                                          final MethodDeclaration baseMethod,
                                          CompilationUnit unit) {
        TypeParameter paramClazzType = new TypeParameter(paramType);
        Parameter parameter = new Parameter(paramClazzType, paramName);
        baseMethod.addParameter(parameter);
        unit.addImport(paramPackage + "." + paramType);

        NormalAnnotationExpr paramAnnotation = new NormalAnnotationExpr();
        paramAnnotation.setName("RequestBody");
        paramAnnotation.addPair("description", "\"" + description + "\"");
        if (example != null) {
            paramAnnotation.addPair("example", "\"" + example + "\"");
        }
        paramAnnotation.addPair("required", "true");
        parameter.addAnnotation(paramAnnotation);
        unit.addImport("io.swagger.v3.oas.annotations.parameters.RequestBody");
    }

    private static void create(String rootClassName, String rootClassPackage, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        if (Utils.hasMethodInClass(clazz, Utils::isMethodCreate)) {
            return;
        }

        final String paramType = CREATE + rootClassName;
        final String description = "The new " + rootClassName;
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Create a new " + rootClassName + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod(CREATE.toLowerCase(), "POST", false, clazz, unit, operation);
        createRequestBody(paramType, rootClassName.toLowerCase(), rootClassPackage, description, null, baseMethod, unit);
    }


    private static void update(String rootClassName, ClassOrInterfaceDeclaration rootClass, String rootClassPackage, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        boolean isMethodPresent = clazz.getMethods().stream()
                .filter(m -> Utils.isMethodUpdate(m, ID_PARAM))
                .findFirst()
                .isPresent();
        if (isMethodPresent) {
            return;
        }

        final String paramType = UPDATE + rootClassName;
        final String idDescription = "The " + rootClassName + " " + ID_PARAM;
        final String idExample = Utils.getExample(getId(rootClass));
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Update " + rootClassName + " by " + ID_PARAM + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod(UPDATE.toLowerCase(), "PUT", true, clazz, unit, operation);
        createParameter("String", ID_PARAM, true, null, idDescription, idExample, baseMethod, unit);
        final String requestBodyDescription = "The updated data for the existing " + rootClassName;
        createRequestBody(paramType, rootClassName.toLowerCase(), rootClassPackage, requestBodyDescription, null, baseMethod, unit);
    }

    private static FieldDeclaration getId(ClassOrInterfaceDeclaration rootClass) {
        Optional<FieldDeclaration> id = rootClass.getFieldByName(ID_PARAM);
        if (!id.isPresent()) {
            throw new GeneratorException("Class " + rootClass.getNameAsString() + ": Id was not found. Add to a field id = true in @Model.");
        }
        return id.get();
    }

    private static void read(String rootClassName, ClassOrInterfaceDeclaration rootClass, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        boolean isMethodPresent = clazz.getMethods().stream()
                .filter(m -> Utils.isMethodRead(m, ID_PARAM))
                .findFirst()
                .isPresent();
        if (isMethodPresent) {
            return;
        }

        final String description = "The " + rootClassName + " " + ID_PARAM;
        final String example = Utils.getExample(getId(rootClass));
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Read " + rootClassName + " by " + ID_PARAM + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod("read", "GET", true, clazz, unit, operation);
        createParameter("String", ID_PARAM, true, null, description, example, baseMethod, unit);

    }

    private static void delete(String rootClassName, ClassOrInterfaceDeclaration rootClass, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        boolean isMethodPresent = clazz.getMethods().stream()
                .filter(m -> Utils.isMethodDelete(m, ID_PARAM))
                .findFirst()
                .isPresent();
        if (isMethodPresent) {
            return;
        }

        final String idDescription = "The " + rootClassName + " " + ID_PARAM;
        final String example = Utils.getExample(getId(rootClass));
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Delete by " + ID_PARAM + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod("delete", "DELETE", true, clazz, unit, operation);
        createParameter("String", ID_PARAM, true, null, idDescription, example, baseMethod, unit);
    }

    private static void bulkDelete(String rootClassName, ClassOrInterfaceDeclaration rootClass, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        if (Utils.hasMethodInClass(clazz, Utils::isMethodBulkDelete)) {
            return;
        }

        final String paramName = Utils.toPlural(ID_PARAM);
        final String idsDescription = "Set of " + rootClassName + " " + paramName + " to delete";
        final String idExample = Utils.getExample(rootClass.getFieldByName(ID_PARAM).get());
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Bulk delete " + Utils.toPlural(rootClassName).toLowerCase() + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod("bulkDelete", "DELETE", false, clazz, unit, operation);
        createParameter("List<String>", paramName, false, Arrays.asList("java.util.List"), idsDescription, idExample, baseMethod, unit);
    }


    private static void bulkCreate(final String rootClassName, String rootClassPackage, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        if (Utils.hasMethodInClass(clazz, Utils::isMethodBulkCreate)) {
            return;
        }
        final String paramName = CREATE + rootClassName;
        final String description = "Set of " + paramName + " to create";
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Bulk create " + Utils.toPlural(rootClassName).toLowerCase() + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod("bulkCreate", "POST", false, clazz, unit, operation);
        final String rootClassImport = rootClassPackage + "." + paramName;
        final List<String> imports = Arrays.asList("java.util.List", rootClassImport);
        createParameter("List<" + paramName + ">", Strings.lcfirst(rootClassName + "s"), false, imports, description, null, baseMethod, unit);
    }

    private static void bulkUpdate(final String rootClassName, String rootClassPackage, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        if (Utils.hasMethodInClass(clazz, Utils::isMethodBulkUpdate)) {
            return;
        }
        final String paramName = UPDATE + rootClassName;
        final String description = "Set of " + paramName + " to update";
        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Bulk update " + Utils.toPlural(rootClassName).toLowerCase() + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod("bulkUpdate", "PUT", false, clazz, unit, operation);
        final String rootClassImport = rootClassPackage + "." + paramName;
        final List<String> imports = Arrays.asList("java.util.List", rootClassImport);
        createParameter("List<" + paramName + ">", Strings.lcfirst(rootClassName + "s"), false, imports, description, null, baseMethod, unit);
    }


    private static void readAll(String rootClassName, ClassOrInterfaceDeclaration rootClass, ClassOrInterfaceDeclaration clazz, CompilationUnit unit) {
        boolean isMethodPresent = clazz.getMethods().stream()
                .filter(m -> Utils.isMethodReadAll(m))
                .findFirst()
                .isPresent();
        if (isMethodPresent) {
            return;
        }

        AnnotationExpr operation = JavaParser.parseAnnotation("@Operation(summary = \"Read all " + Utils.toPlural(rootClassName) + ".\")");
        final MethodDeclaration baseMethod = createBaseMethod("readAll", "GET", false, clazz, unit, operation);

        String modelPackage = rootClass.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString();

        File filterFile = new File(
                Configuration.MODEL_SOURCES + File.separator +
                        Utils.transformPackageToPath(modelPackage) + File.separator + rootClassName + "Filter.java");

        if (filterFile.exists()) {
            ClassOrInterfaceDeclaration filterClazz = null;
            CompilationUnit filterUnit = null;
            try {
                filterUnit = JavaParser.parse(filterFile);
                filterClazz = Utils.getClazz(filterUnit);
            } catch (FileNotFoundException e) {
                //This will never happend as we are checking if it exists()
            }

            List<FieldDeclaration> filterClazzFields = filterClazz.getFields();

            for (FieldDeclaration filterClazzField : filterClazzFields) {
                VariableDeclarator var = filterClazzField.getVariables().stream().findFirst().get();
                Parameter parameter = new Parameter(var.getType(), var.getNameAsString());
                parameter.addSingleMemberAnnotation("QueryParam", "\"" + var.getNameAsString() + "\"");
                baseMethod.addParameter(parameter);
                unit.addImport(ImportManager.getImport("QueryParam"));
            }

            Utils.addImports(filterUnit, unit);
        }

        rootClass.addMember(baseMethod);
    }
}
