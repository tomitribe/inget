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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import org.tomitribe.common.Configuration;
import org.tomitribe.common.Reformat;
import org.tomitribe.common.RemoveDuplicateImports;
import org.tomitribe.common.Utils;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.tomitribe.common.Utils.getClassOperations;
import static org.tomitribe.common.Utils.getClazz;
import static org.tomitribe.common.Utils.getModel;
import static org.tomitribe.common.Utils.getRootName;

public class ModelGenerator {
    static final String CREATE_PREFIX = "Create";
    static final String UPDATE_PREFIX = "Update";
    static final String READ_PREFIX = "";

    public static void execute() throws IOException {
        List<File> modelFiles = getModel();

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

                if (classOperations == null || classOperations.contains(Operation.LIST)) {
                    String listClassName = Utils.toPlural(rootClassName);
                    CompilationUnit listUnit = ModelClassGenerator.createListClass(rootClassUnit, rootClass, rootClassName, filterUnit, summaryUnit, listClassName);
                    save(listClassName, rootClassUnit, listUnit);

                    String bulkClassName = "Bulk" + rootClassName + "Result";
                    CompilationUnit bulkUnit = ModelClassGenerator.createBulkClass(rootClassUnit, rootClassName, bulkClassName);
                    save(bulkClassName, rootClassUnit, bulkUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.CREATE)) {
                    createUnit = ModelClassGenerator.createClass(rootClassUnit, rootClass, rootClassName, Operation.CREATE, CREATE_PREFIX);
                    save(CREATE_PREFIX + rootClassName, rootClassUnit, createUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.UPDATE)) {
                    updateUnit = ModelClassGenerator.createClass(rootClassUnit, rootClass, rootClassName, Operation.UPDATE, UPDATE_PREFIX);
                    save(UPDATE_PREFIX + rootClassName, rootClassUnit, updateUnit);
                }

                if (classOperations == null || classOperations.contains(Operation.READ)) {
                    CompilationUnit readUnit = ModelClassGenerator.createClass(rootClassUnit, rootClass, rootClassName, Operation.READ, READ_PREFIX);
                    readUnit = ModelMethodGenerator.createMethods(rootClassName, rootClassUnit, createUnit, updateUnit, readUnit, classOperations);
                    save(READ_PREFIX + rootClassName, rootClassUnit, readUnit);
                }
            }

        }
    }

    public static void save(String className, CompilationUnit rootClassUnit, CompilationUnit classToBeSaved) throws IOException {
        if (classToBeSaved == null) {
            return;
        }
        String sourceFolder = Configuration.GENERATED_SOURCES;
        String modelFolder = rootClassUnit.getPackageDeclaration().get().getName().toString().replaceAll("\\.", "/");
        sourceFolder += "/" + modelFolder;
        Path path = Paths.get(sourceFolder);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        File newFile = new File(sourceFolder, className + ".java");

        String modified = Stream.of(classToBeSaved.toString())
                .map(RemoveDuplicateImports::apply)
                .map(Reformat::apply)
                .map(AddLicenceHeader::apply)
                .map(RemoveUnusedImports::removeUnusedImports)
                .findFirst().get();

        IO.copy(IO.read(modified), newFile);
    }
}