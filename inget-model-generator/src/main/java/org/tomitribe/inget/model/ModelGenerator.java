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
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.tomitribe.inget.common.Operation;
import org.tomitribe.inget.common.Reformat;
import org.tomitribe.inget.common.RemoveDuplicateImports;
import org.tomitribe.inget.common.Utils;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.tomitribe.inget.common.Utils.getClassOperations;
import static org.tomitribe.inget.common.Utils.getClazz;
import static org.tomitribe.inget.common.Utils.getModel;
import static org.tomitribe.inget.common.Utils.getRootName;

public class ModelGenerator {

    private ModelGenerator() {
        // no-op
    }

    static final String CREATE_PREFIX = "Create";
    static final String UPDATE_PREFIX = "Update";
    static final String READ_PREFIX = "";

    public static void execute() throws IOException {
        List<File> modelFiles = getModel();

        ModelClassGenerator.createBaseClasses();

        for (File rootClassFile : modelFiles) {
            final String rootClassSource = IO.slurp(rootClassFile);
            final CompilationUnit rootClassUnit = JavaParser.parse(rootClassSource);
            ClassOrInterfaceDeclaration rootClass = getClazz(rootClassUnit);
            if (rootClass != null) {
                final String rootClassName = getRootName(getClazz(rootClassUnit));
                List<String> classOperations = getClassOperations(rootClass);

                CompilationUnit createUnit = null;
                CompilationUnit updateUnit = null;

                String summaryClassName = rootClassName + "Summary";
                CompilationUnit summaryUnit = ModelClassGenerator.createSummaryClass(rootClass, rootClassUnit, summaryClassName);
                save(summaryClassName, rootClassUnit, summaryUnit);

                String filterClassName = rootClassName + "Filter";
                CompilationUnit filterUnit = ModelClassGenerator.createFilterClass(rootClass, rootClassUnit, filterClassName);
                save(filterClassName, rootClassUnit, filterUnit);

                if (classOperations == null || classOperations.contains(Operation.READ_ALL)) {
                    String listClassName = Utils.toPlural(rootClassName);
                    CompilationUnit listUnit = ModelClassGenerator.createListClass(rootClassUnit, rootClass, rootClassName, filterUnit, summaryUnit, listClassName);
                    if (listUnit != null) {
                        save(rootClassName + "Result", rootClassUnit, listUnit);
                    }
                }

                if(classOperations == null ||
                        classOperations.contains(Operation.BULK_CREATE) ||
                        classOperations.contains(Operation.BULK_UPDATE) ||
                        classOperations.contains(Operation.BULK_DELETE)){
                    String bulkClassName = "Bulk" + rootClassName + "Result";
                    CompilationUnit bulkUnit = ModelClassGenerator.createBulkClass(rootClassUnit, rootClass, rootClassName, bulkClassName);
                    if (bulkUnit != null) {
                        save(bulkClassName, rootClassUnit, bulkUnit);
                    }
                }

                if (classOperations == null || classOperations.contains(Operation.CREATE)) {
                    createUnit = ModelClassGenerator.createClass(rootClassUnit, rootClass, rootClassName, Operation.CREATE, CREATE_PREFIX);
                    save(CREATE_PREFIX + rootClassName, rootClassUnit, createUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.UPDATE)) {
                    updateUnit = ModelClassGenerator.createClass(rootClassUnit, rootClass, rootClassName, Operation.UPDATE, UPDATE_PREFIX);
                    save(UPDATE_PREFIX + rootClassName, rootClassUnit, updateUnit);
                }

                CompilationUnit readUnit = ModelClassGenerator.createClass(rootClassUnit, rootClass, rootClassName, Operation.READ, READ_PREFIX);
                readUnit = ModelMethodGenerator.createMethods(rootClassName, rootClassUnit, createUnit, updateUnit, readUnit, classOperations);
                save(READ_PREFIX + rootClassName, rootClassUnit, readUnit);
            }

        }
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

        Utils.save(className + ".java", rootClassUnit.getPackageDeclaration().get().getName().toString(), modified);
    }
}