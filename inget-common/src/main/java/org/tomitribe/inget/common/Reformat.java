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
package org.tomitribe.inget.common;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.util.Map;

public class Reformat {

    public static String apply(final String source) {
        final Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);

        // change the option to wrap each enum constant on a new line
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
                DefaultCodeFormatterConstants.createAlignmentValue(
                        true,
                        DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
                        DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION,
                DefaultCodeFormatterConstants.createAlignmentValue(
                        true,
                        DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
                        DefaultCodeFormatterConstants.INDENT_DEFAULT));

        options.put(
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_CONSTRUCTOR_DECLARATION,
                DefaultCodeFormatterConstants.createAlignmentValue(
                        true,
                        DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
                        DefaultCodeFormatterConstants.INDENT_DEFAULT));
        options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, 180);
        options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, JavaCore.INSERT);
        options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_METHOD, JavaCore.INSERT);
        options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
        // instantiate the default code formatter with the given options
        final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

        final TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, source, 0, // starting position
                source.length(), // length
                0, // initial indentation
                "\n" // line separator
        );

        IDocument document = new Document(source);
        try {
            edit.apply(document);
        } catch (MalformedTreeException | BadLocationException e) {
            throw new IllegalStateException(e);
        }

        // display the formatted string on the System out
        return document.get()
                .replaceAll("\t", "    ")
                .replaceAll("// //", "//")
                .replaceAll("([^)]),( *//.*)?\n", "$1,$2\n\n")
                .replaceAll("(// ----------------------------------------------------------------------------------------\n)", "$1\n");
    }
}
