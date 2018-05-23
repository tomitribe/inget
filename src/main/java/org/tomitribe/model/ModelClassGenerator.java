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

package org.tomitribe.model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import org.tomitribe.common.Utils;
import org.tomitribe.util.Join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModelClassGenerator {

    static CompilationUnit createClass(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass,
                                       String rootClassName, String operation, String classPrefix) throws IOException {
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(rootClassUnit.getPackageDeclaration().get().getName().toString());
        final String className = classPrefix + rootClassName;
        newClassCompilationUnit.addClass(className, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(className).get();

        newClass.addMarkerAnnotation("Value");
        newClassCompilationUnit.addImport(Utils.getImport("Value"));

        final String builderClassName = (classPrefix.length() != 0) ? classPrefix : "Read";
        final NormalAnnotationExpr builderAnnotation = new NormalAnnotationExpr();
        builderAnnotation.setName("Builder");
        builderAnnotation.addPair("builderClassName", "\"" + builderClassName + "\"");
        builderAnnotation.addPair("toBuilder", "true");
        newClass.addAnnotation(builderAnnotation);
        newClassCompilationUnit.addImport(Utils.getImport("Builder"));

        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);

        final Optional<AnnotationExpr> schema = rootClass.getAnnotationByName("Schema");
        if (schema.isPresent()) {
            newClass.addAnnotation(schema.get());
        }

        handleExtendedClasses(rootClassUnit, rootClass, operation, newClass, classPrefix);

        rootClass.getFields().stream().forEach(f -> {
            handleField(operation, rootClassUnit, newClass, f, classPrefix);
        });

        Utils.addImports(rootClassUnit,  newClassCompilationUnit);

        return newClassCompilationUnit;
    }

    private static void handleExtendedClasses(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass, String operation, ClassOrInterfaceDeclaration newClass, String prefix) throws IOException {
        NodeList<ClassOrInterfaceType> extendedTypes = rootClass.getExtendedTypes();
        while (extendedTypes.size() > 0) {
            for (ClassOrInterfaceType et : extendedTypes) {
                ClassOrInterfaceDeclaration extendedClass =
                        Utils.getExtendedClass(rootClassUnit, et.getNameAsString());

                extendedClass.getFields().forEach(f -> {
                    handleField(operation, rootClassUnit, newClass, f, prefix);
                });
                Utils.addImports(extendedClass.findCompilationUnit().get(), newClass.findCompilationUnit().get());
                extendedTypes = extendedClass.getExtendedTypes();
            }
        }
    }

    private static void handleField(String operation, CompilationUnit unit, ClassOrInterfaceDeclaration newClass, FieldDeclaration f, String prefix) {
        if (!f.getAnnotationByName("Model").isPresent()) {
            FieldDeclaration newField = f.clone();
            handleExpandableField(newField, prefix, unit);
            newClass.addMember(newField);
        } else if (!Utils.hasOperations(f) || Utils.isOperationPresent(f, operation)) {
            FieldDeclaration newField = f.clone();
            handleExpandableField(newField, prefix, unit);
            AnnotationExpr modelAnnotation = newField.getAnnotationByName("Model").get();
            newClass.addMember(newField);
            newField.remove(modelAnnotation);
        }
    }


    private static void handleExpandableField(FieldDeclaration field, String prefix, CompilationUnit unit) {
        VariableDeclarator variable = field.getVariables().stream().findFirst().get();
        boolean isExpandable = variable.getTypeAsString().contains("Model");
        if (isExpandable) {
            String end = variable.getTypeAsString();
            String entityBefore = variable.getTypeAsString();
            String entityAfter;
            if (variable.getTypeAsString().contains("<")) {
                entityBefore = end.substring(end.indexOf("<") + 1, end.indexOf(">"));
                entityAfter = prefix + entityBefore.replace("Model", "");
                end = end.replace(entityBefore, entityAfter);
            } else {
                end = prefix + end.replace("Model", "");
                entityAfter = end;
            }
            variable.setType(end);

            final String finalEntityBefore = entityBefore;
            Optional<ImportDeclaration> imp = unit.getImports().stream()
                    .filter(i -> i.getNameAsString().contains("." + finalEntityBefore))
                    .findFirst();
            if (imp.isPresent()) {
                String importDeclaration = imp.get().getNameAsString();
                importDeclaration = importDeclaration.replace(entityBefore, entityAfter);
                unit.addImport(importDeclaration);
            }
        }
    }

    static CompilationUnit createListClass(CompilationUnit rootClassUnit, ClassOrInterfaceDeclaration rootClass,
                                           String rootClassName, CompilationUnit filterClassUnit,
                                           CompilationUnit summaryClassUnit, String listClassName) throws IOException {
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(rootClassUnit.getPackageDeclaration().get().getName().toString());
        newClassCompilationUnit.addClass(listClassName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(listClassName).get();

        newClass.addMarkerAnnotation("Value");
        newClassCompilationUnit.addImport(Utils.getImport("Value"));
        NormalAnnotationExpr equalsAndHashCode = new NormalAnnotationExpr();
        equalsAndHashCode.setName("EqualsAndHashCode");
        equalsAndHashCode.addPair("callSuper", "true");
        newClassCompilationUnit.addImport(Utils.getImport("EqualsAndHashCode"));
        newClass.addAnnotation(equalsAndHashCode);

        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);

        NormalAnnotationExpr schema = new NormalAnnotationExpr();
        schema.setName("Schema");
        schema.addPair("description", "\"The list of " + listClassName.toLowerCase() + " available for a given search request with associated metadata.\"");
        newClass.addAnnotation(schema);
        newClassCompilationUnit.addImport(Utils.getImport("Schema"));
        newClassCompilationUnit.addImport(Utils.getImport("Page"));

        rootClass.getFields().stream().filter(f -> Utils.isOperationPresent(f, Operation.LIST)).forEach(f -> {
            if (f.getAnnotationByName("JohnzonConverter") != null) {
                newClassCompilationUnit.addImport(Utils.getImport("JohnzonConverter"));
                newClassCompilationUnit.addImport(Utils.getImport("JsDateConverter"));
            }
            String type = f.getVariables().stream().findFirst().get().getTypeAsString();
            String importType = Utils.getImport(type);
            if (importType != null) {
                newClassCompilationUnit.addImport(importType);
            }
            FieldDeclaration newField = f.clone();
            newField.remove(newField.getAnnotationByName("Model").get());
            newClass.addMember(newField);
        });

        String summaryClassValue = rootClassName;
        if (summaryClassUnit != null) {
            summaryClassValue = Utils.getClazz(summaryClassUnit).getNameAsString();
        }

        String filterName = "";
        if (filterClassUnit == null) {
            filterName = "DefaultFilter";
            newClassCompilationUnit.addImport(Utils.getImport(filterName));
        } else {
            filterName = Utils.getClazz(filterClassUnit).getNameAsString();
        }

        newClass.addExtendedType("Page<" + summaryClassValue + "," + filterName + ">");

        final StringBuilder constructor = new StringBuilder();
        constructor.append("@Builder public %classname(");
        constructor.append("final Collection<%itemsType> items, final %filter filter, final String pagingState, final Long total");
        newClass.getFields().stream().forEach(f -> {
            VariableDeclarator field = f.getVariables().stream().findFirst().get();
            String fieldName = field.getNameAsString();
            String fieldType = field.getTypeAsString();
            constructor.append(", final " + fieldType + " " + fieldName);
        });
        constructor.append(") {");
        constructor.append("super(items, filter, pagingState, total);");
        newClass.getFields().stream().forEach(f -> {
            String fieldName = f.getVariables().stream().findFirst().get().getNameAsString();
            constructor.append("this." + fieldName + " = " + fieldName + ";");
        });
        constructor.append(" }");
        String result = constructor.toString()
                .replaceAll("%classname", listClassName)
                .replaceAll("%filter", filterName)
                .replaceAll("%itemsType", summaryClassValue);

        ConstructorDeclaration constructorDeclaration = JavaParser.parseBodyDeclaration(result).asConstructorDeclaration();
        newClass.addMember(constructorDeclaration);
        newClassCompilationUnit.addImport(Utils.getImport("Builder"));
        newClassCompilationUnit.addImport(Utils.getImport("Collection"));

        return newClassCompilationUnit;
    }

    static CompilationUnit createFilterClass(ClassOrInterfaceDeclaration rootClass, CompilationUnit rootClassUnit, String filterClassName) {
        List<FieldDeclaration> filterFields = rootClass.getFields().stream()
                .filter(f -> {
                    Optional<AnnotationExpr> modelAnnotationOptional = f.getAnnotationByName("Model");
                    if (!modelAnnotationOptional.isPresent()) {
                        return false;
                    }
                    NormalAnnotationExpr model = modelAnnotationOptional.get().asNormalAnnotationExpr();
                    Map<String, MemberValuePair> pairs = Utils.pairs(model);
                    MemberValuePair filterValuePair = pairs.get("filter");
                    if (filterValuePair != null) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        if (filterFields.size() == 0) {
            return null;
        }

        final CompilationUnit filterClassCompilationUnit = new CompilationUnit(rootClassUnit.getPackageDeclaration().get().getName().toString());
        filterClassCompilationUnit.addClass(filterClassName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration filterClass = filterClassCompilationUnit.getClassByName(filterClassName).get();
        filterClass.addExtendedType("DefaultFilter");
        filterClassCompilationUnit.addImport(Utils.getImport("DefaultFilter"));
        filterClass.addMarkerAnnotation("Value");
        filterClassCompilationUnit.addImport(Utils.getImport("Value"));

        final List<String> constructorArgs = new ArrayList<String>();
        final List<String> constructorThis = new ArrayList<String>();
        filterFields.stream().forEach(f -> {
            NormalAnnotationExpr model = f.getAnnotationByName("Model").get().asNormalAnnotationExpr();
            Map<String, MemberValuePair> modelPairs = Utils.pairs(model);
            NormalAnnotationExpr filter = modelPairs.get("filter").getValue().asNormalAnnotationExpr();
            Map<String, MemberValuePair> pairs = Utils.pairs(filter);
            MemberValuePair nameValuePair = pairs.get("name");
            String name = nameValuePair.getValue().asStringLiteralExpr().getValue();
            if (name.equals("")) {
                name = f.getVariables().stream().findFirst().get().getNameAsString();
            }
            MemberValuePair multipleValuePair = pairs.get("multiple");
            Type type = new TypeParameter("String");
            boolean multiple = false;
            if (multipleValuePair != null) {
                multiple = multipleValuePair.getValue().asBooleanLiteralExpr().getValue();
                if (multiple) {
                    type = new TypeParameter("Collection<String>");
                    filterClassCompilationUnit.addImport(Utils.getImport("Collection"));
                }
            }

            FieldDeclaration newField = filterClass.addField(type, name, Modifier.PUBLIC);
            NormalAnnotationExpr schema = new NormalAnnotationExpr();
            schema.setName("Schema");
            if (multiple) {
                schema.addPair("description", "\"The set of unique " + name + " in all returned items.\"");
            } else {
                schema.addPair("description", "\"The " + name + " in all returned items.\"");
            }
            newField.addAnnotation(schema);
            filterClassCompilationUnit.addImport(Utils.getImport("Schema"));
            constructorArgs.add("final " + type + " " + name);
            constructorThis.add("this." + name + " = " + name + ";");
        });

        StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("public " + filterClassName + "(");
        constructorBuilder.append("final Collection<String> labels, ");
        constructorBuilder.append(Join.join(",", constructorArgs));
        constructorBuilder.append(") {");
        constructorBuilder.append("super(labels);");
        constructorThis.stream().forEach(v -> {
            constructorBuilder.append(v);
        });
        constructorBuilder.append("}");
        BodyDeclaration<?> constructor = JavaParser.parseBodyDeclaration(constructorBuilder.toString());
        filterClass.addMember(constructor);

        return filterClassCompilationUnit;
    }

    static CompilationUnit createBulkClass(CompilationUnit rootClassUnit, String rootClassName, String bulkClassName) throws IOException {
        final CompilationUnit newClassCompilationUnit = new CompilationUnit(rootClassUnit.getPackageDeclaration().get().getName().toString());
        newClassCompilationUnit.addClass(bulkClassName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration newClass = newClassCompilationUnit.getClassByName(bulkClassName).get();

        newClass.addMarkerAnnotation("Value");
        newClassCompilationUnit.addImport(Utils.getImport("Value"));
        newClass.addMarkerAnnotation("EqualsAndHashCode");
        newClassCompilationUnit.addImport(Utils.getImport("EqualsAndHashCode"));

        Utils.addGeneratedAnnotation(newClassCompilationUnit, newClass, null);

        NormalAnnotationExpr schema = new NormalAnnotationExpr();
        schema.setName("Schema");
        schema.addPair("description", "\"The result of the bulk operation.\"");
        newClass.addAnnotation(schema);
        newClassCompilationUnit.addImport(Utils.getImport("Schema"));

        String paramName = Utils.toPlural(rootClassName.toLowerCase());
        NormalAnnotationExpr fieldSchema = new NormalAnnotationExpr();
        fieldSchema.setName("Schema");
        fieldSchema.addPair("description", "\"The " + paramName + " that failed in the bulk operation.\"");
        FieldDeclaration fieldDeclaration = newClass.addField(new TypeParameter("List<Failure>"), paramName, Modifier.PRIVATE);
        fieldDeclaration.addAnnotation(fieldSchema);
        newClassCompilationUnit.addImport(Utils.getImport("Failure"));
        newClassCompilationUnit.addImport(Utils.getImport("List"));

        return newClassCompilationUnit;
    }

    public static CompilationUnit createSummaryClass(ClassOrInterfaceDeclaration rootClass, CompilationUnit rootClassUnit, String summaryClassName) {
        List<FieldDeclaration> summaryFields = rootClass.getFields().stream()
                .filter(f -> {
                    Optional<AnnotationExpr> modelAnnotationOptional = f.getAnnotationByName("Model");
                    if (!modelAnnotationOptional.isPresent()) {
                        return false;
                    }
                    NormalAnnotationExpr model = modelAnnotationOptional.get().asNormalAnnotationExpr();
                    Map<String, MemberValuePair> pairs = Utils.pairs(model);
                    MemberValuePair filterValuePair = pairs.get("summary");
                    if (filterValuePair != null) {
                        return filterValuePair.getValue().asBooleanLiteralExpr().getValue();
                    }
                    return false;
                }).collect(Collectors.toList());

        if (summaryFields.size() == 0) {
            return null;
        }

        final CompilationUnit summaryUnit = new CompilationUnit(rootClassUnit.getPackageDeclaration().get().getName().toString());
        summaryUnit.addClass(summaryClassName, Modifier.PUBLIC);
        final ClassOrInterfaceDeclaration summaryClass = summaryUnit.getClassByName(summaryClassName).get();
        summaryClass.addMarkerAnnotation("Data");
        summaryUnit.addImport(Utils.getImport("Data"));
        summaryClass.addMarkerAnnotation("EqualsAndHashCode");
        summaryUnit.addImport(Utils.getImport("EqualsAndHashCode"));
        summaryClass.addMarkerAnnotation("AllArgsConstructor");
        summaryUnit.addImport(Utils.getImport("AllArgsConstructor"));

        String rootClassName = Utils.getRootName(Utils.getClazz(rootClassUnit));
        String schemaDescription = "Summary of the search for " + Utils.toPlural(rootClassName);
        NormalAnnotationExpr classSchema = new NormalAnnotationExpr();
        classSchema.setName("Schema");
        classSchema.addPair("description", "\"" + schemaDescription + "\"");
        summaryClass.addAnnotation(classSchema);

        summaryFields.stream().forEach(f -> {
            VariableDeclarator fieldVariable = f.getVariables().stream().findFirst().get();
            FieldDeclaration newField = summaryClass.addField(fieldVariable.getType(), fieldVariable.getNameAsString(), Modifier.PRIVATE);
            Optional<AnnotationExpr> fieldSchema = f.getAnnotationByName("Schema");
            if (fieldSchema.isPresent()) {
                newField.addAnnotation(fieldSchema.get());
                summaryUnit.addImport(Utils.getImport("Schema"));
            }
        });

        return summaryUnit;
    }
}