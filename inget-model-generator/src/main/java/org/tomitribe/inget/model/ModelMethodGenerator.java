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

package org.tomitribe.inget.model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.tomitribe.inget.common.Operation;
import org.tomitribe.inget.common.Utils;

import java.util.List;
import java.util.Optional;

public class ModelMethodGenerator {

    static CompilationUnit createMethods(String rootClassName,
                                         CompilationUnit rootUnit,
                                         CompilationUnit createUnit,
                                         CompilationUnit updateUnit,
                                         CompilationUnit readUnit,
                                         List<String> classOperations) {


        ClassOrInterfaceDeclaration readClass = readUnit.getClassByName(rootClassName).get();

        if (createUnit != null) {
            addConversionMethod(createUnit, readClass, ModelGenerator.CREATE_PREFIX);
            addBuilderMethods(createUnit, readClass, ModelGenerator.CREATE_PREFIX);
        }

        if (updateUnit != null) {
            addConversionMethod(updateUnit, readClass, ModelGenerator.UPDATE_PREFIX);
            addBuilderMethods(updateUnit, readClass, ModelGenerator.UPDATE_PREFIX);
        }


        if (classOperations == null || classOperations.contains(Operation.DELETE)) {
            addDeleteMethod(rootUnit, readClass);
        }

        return readUnit;
    }

    static void addDeleteMethod(CompilationUnit rootUnit, ClassOrInterfaceDeclaration readClass) {
        ClassOrInterfaceDeclaration rootClass = Utils.getClazz(rootUnit);
        List<FieldDeclaration> fields = rootClass.getFields();
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("public String toDelete() {");
        methodBuilder.append("return this.%field;");
        methodBuilder.append("}");

        Optional<FieldDeclaration> id = fields.stream().filter(f -> Utils.isId(f)).findFirst();
        if (id.isPresent()) {
            final String result = methodBuilder.toString()
                    .replaceAll("%field", id.get().getVariables().stream().findFirst().get().getNameAsString());
            MethodDeclaration methodDeclaration = JavaParser.parseBodyDeclaration(result).asMethodDeclaration();
            readClass.addMember(methodDeclaration);
        }
    }

    static void addBuilderMethods(CompilationUnit classToBeBuiltUnit, ClassOrInterfaceDeclaration readClass, String prefix) {
        ClassOrInterfaceDeclaration classToBeBuilt = Utils.getClazz(classToBeBuiltUnit);
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("public static %classname.%prefix %methodname(){");
        methodBuilder.append("return %classname.builder();");
        methodBuilder.append("}");
        final String result = methodBuilder.toString()
                .replaceAll("%classname", classToBeBuilt.getNameAsString())
                .replaceAll("%methodname", prefix.toLowerCase())
                .replaceAll("%prefix", prefix);
        MethodDeclaration methodDeclaration = JavaParser.parseBodyDeclaration(result).asMethodDeclaration();
        readClass.addMember(methodDeclaration);
    }

    static void addConversionMethod(CompilationUnit classToBeConvertedUnit, ClassOrInterfaceDeclaration readClass, String prefix) {
        final StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("public %classname.%prefix to%prefix() {");
        methodBuilder.append("return %classname.builder()");
        ClassOrInterfaceDeclaration classToBeConverted = Utils.getClazz(classToBeConvertedUnit);
        classToBeConverted.getFields().stream().forEach(f -> {
            VariableDeclarator fieldClassToBeConverted = f.getVariables().stream().findFirst().get();
            String propertyName = fieldClassToBeConverted.getNameAsString();
            String typeName = fieldClassToBeConverted.getTypeAsString();
            Optional<FieldDeclaration> readField = readClass.getFieldByName(propertyName);
            if (readField.isPresent() && readField.get().getCommonType().asString().equals(typeName)) {
                methodBuilder.append(".%property(this.%property)".replaceAll("%property", propertyName));
            }
        });
        methodBuilder.append(";");
        methodBuilder.append("\n}");
        final String result = methodBuilder.toString()
                .replaceAll("%classname", classToBeConverted.getNameAsString())
                .replaceAll("%prefix", prefix);
        MethodDeclaration methodDeclaration = JavaParser.parseBodyDeclaration(result).asMethodDeclaration();
        readClass.addMember(methodDeclaration);
    }
}