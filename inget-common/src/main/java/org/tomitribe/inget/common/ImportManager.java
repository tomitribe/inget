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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Optional;

public class ImportManager {

    private ImportManager() {
        // no-op
    }

    private static final HashMap<String, String> IMPORT_MAP = new HashMap<>();

    static {
        // Java
        IMPORT_MAP.put("Date", "java.util.Date");
        IMPORT_MAP.put("Collection", "java.util.Collection");
        IMPORT_MAP.put("Generated", "javax.annotation.Generated");
        IMPORT_MAP.put("List", "java.util.List");

        // Lombok
        IMPORT_MAP.put("Builder", "lombok.Builder");
        IMPORT_MAP.put("ToString", "lombok.ToString");
        IMPORT_MAP.put("EqualsAndHashCode", "lombok.EqualsAndHashCode");
        IMPORT_MAP.put("AllArgsConstructor", "lombok.AllArgsConstructor");
        IMPORT_MAP.put("Value", "lombok.Value");
        IMPORT_MAP.put("Data", "lombok.Data");

        // Swagger
        IMPORT_MAP.put("Operation", "io.swagger.v3.oas.annotations.Operation");
        IMPORT_MAP.put("Schema", "io.swagger.v3.oas.annotations.media.Schema");

        // JAX-RS
        IMPORT_MAP.put("Path", "javax.ws.rs.Path");
        IMPORT_MAP.put("PathParam", "javax.ws.rs.PathParam");
        IMPORT_MAP.put("QueryParam", "javax.ws.rs.QueryParam");
        IMPORT_MAP.put("Response", "javax.ws.rs.core.Response");
        IMPORT_MAP.put("POST", "javax.ws.rs.POST");
        IMPORT_MAP.put("GET", "javax.ws.rs.GET");
        IMPORT_MAP.put("PUT", "javax.ws.rs.PUT");
        IMPORT_MAP.put("DELETE", "javax.ws.rs.DELETE");
        IMPORT_MAP.put("Produces", "javax.ws.rs.Produces");
        IMPORT_MAP.put("Consumes", "javax.ws.rs.Consumes");
        IMPORT_MAP.put("MediaType", "javax.ws.rs.core.MediaType");

        // Johnzon
        IMPORT_MAP.put("JohnzonProvider", "org.apache.johnzon.jaxrs.JohnzonProvider");

        // CXF - Microprofile
        IMPORT_MAP.put("RestClientBuilder", "org.eclipse.microprofile.rest.client.RestClientBuilder");
        IMPORT_MAP.put("OutInterceptors", "org.apache.cxf.interceptor.OutInterceptors");

        // Airlift
        IMPORT_MAP.put("Option", "io.airlift.airline.Option");
        IMPORT_MAP.put("Command", "io.airlift.airline.Command");
        IMPORT_MAP.put("Arguments", "io.airlift.airline.Arguments");
        IMPORT_MAP.put("Cli", "io.airlift.airline.Cli");
        IMPORT_MAP.put("Help", "io.airlift.airline.Help");

        // Inget
        IMPORT_MAP.put("ClientConfiguration", "org.tomitribe.inget.client.ClientConfiguration");
        IMPORT_MAP.put("BasicConfiguration", "org.tomitribe.inget.client.BasicConfiguration");
        IMPORT_MAP.put("SignatureConfiguration", "org.tomitribe.inget.client.SignatureConfiguration");
        IMPORT_MAP.put("SignatureAuthenticator", "org.tomitribe.inget.client.SignatureAuthenticator");
        IMPORT_MAP.put("BasicAuthenticator", "org.tomitribe.inget.client.BasicAuthenticator");
        IMPORT_MAP.put("LogClientRequestFilter", "org.tomitribe.inget.client.LogClientRequestFilter");
        IMPORT_MAP.put("LogClientResponseFilter", "org.tomitribe.inget.client.LogClientResponseFilter");
        IMPORT_MAP.put("NoOpInterceptor", "org.tomitribe.inget.client.NoOpInterceptor");
    }

    public static String getImport(String className) {
        return IMPORT_MAP.get(className);
    }

    public static String getImportFromClass(CompilationUnit rootClassUnit, Type commonType) {
        Optional<ImportDeclaration> imp = rootClassUnit.getImports().stream().filter(i -> i.getNameAsString().endsWith("." + commonType)).findFirst();

        if (imp.isPresent()) {
            return imp.get().getNameAsString();
        }
        return null;
    }
}
