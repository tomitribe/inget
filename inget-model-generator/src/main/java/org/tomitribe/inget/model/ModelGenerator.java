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