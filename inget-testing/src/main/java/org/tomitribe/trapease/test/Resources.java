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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.trapease.test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.tomitribe.common.Utils;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Keep this one simple and representing the structure of the test setup
 * Put any actual asserts or real logic somewhere else.
 */
public class Resources {

    private final File base;
    private final File actual;
    private final File tempSource;
    private final File expected;
    private final File input;
    private final List<Runnable> cleanup = new ArrayList<>();

    public Resources(final String name) {
        final URL resource = Resources.class.getClassLoader().getResource("root.txt");
        assertNotNull(resource);
        final File file = Urls.toFile(resource);
        assertNotNull(file);
        this.base = new File(file.getParentFile(), name);
        this.expected = new File(base, "expected");
        this.input = new File(base, "input");
        this.actual = Files.tmpdir();
        this.tempSource = Files.tmpdir();

        this.cleanup.add(() -> saveResultsOnExit(name));
        this.cleanup.add(() -> CleanOnExit.delete(actual));
        this.cleanup.add(() -> CleanOnExit.delete(tempSource));
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    private void cleanup() {
        cleanup.stream().forEach(Runnable::run);
    }

    /**
     * Uncomment this cleanup step above as a quick way to update the
     * expected results with the currently generated code.
     */
    private void saveResultsOnExit(final String name) {
        if (!Boolean.getBoolean("saveResults")) return;
        try {
            Generation.saveResults(name, "expected", actual(".*"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Resources name(final String name) {
        return new Resources(name);
    }

    private Map<String, File> map(final File dir, final String regex) throws IOException {
        final Map<String, File> map = new LinkedHashMap<>();

        final List<File> files = Files.collect(dir, regex);
        for (final File file : files) {
            final String relativePath = file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);
            map.put(relativePath, file);
        }
        return map;
    }

    public File expected() {
        return expected;
    }

    public File tempSource() {
        return tempSource;
    }

    public Map<String, File> expected(final String regex) throws IOException {
        return map(expected(), regex);
    }

    public File actual() {
        return actual;
    }

    public Map<String, File> actual(final String regex) throws IOException {
        return map(actual(), regex);
    }

    public File input() {
        return input;
    }

    public Resources input(String sourceCode) {
        final CompilationUnit classUnit = JavaParser.parse(sourceCode);
        final PackageDeclaration packageDeclaration = classUnit.getPackageDeclaration().orElseThrow(IllegalStateException::new);
        final String path = packageDeclaration.getName().asString().replace(".", "/");

        final ClassOrInterfaceDeclaration clazz = Utils.getClazz(classUnit);
        final String className = clazz.getNameAsString();

        final File file = new File(input, path + "/" + className + ".java");
        Files.mkparent(file);

        try {
            IO.copy(IO.read(sourceCode), file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return this;
    }

    public static Resources here() {
        final StackTraceElement caller = new Exception().getStackTrace()[1];
        final String className = caller.getClassName().replaceAll(".*\\.", "");
        final String methodName = caller.getMethodName();

        return new Resources(className + "/" + methodName);
    }
}